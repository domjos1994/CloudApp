package de.domjos.cloudapp2.cardav

import android.os.Build
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp2.cardav.utils.Converter
import de.domjos.cloudapp2.cardav.model.AddressBook
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.AddressType
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone
import de.domjos.cloudapp2.database.model.contacts.PhoneType
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.parameter.ImageType
import ezvcard.parameter.TelephoneType
import ezvcard.property.Birthday
import ezvcard.property.Categories
import ezvcard.property.Organization
import ezvcard.property.Revision
import ezvcard.property.StructuredName
import ezvcard.property.Telephone
import ezvcard.property.Uid
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.StringReader
import java.io.StringWriter
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.jvm.Throws

class CarDav(private val authentication: Authentication?) {
    private var sardine: OkHttpSardine? = null
    private var basePath = ""

    init {
        if(authentication != null) {
            val client = OkHttpClient.Builder()
            client.callTimeout(Duration.ofMinutes(3))
            client.readTimeout(Duration.ofMinutes(3))
            client.writeTimeout(Duration.ofMinutes(5))
            client.connectTimeout(Duration.ofMinutes(3))
            this.sardine = OkHttpSardine(client.build())
            this.sardine?.setCredentials(authentication.userName, authentication.password)
            this.basePath = "${authentication.url}/remote.php/dav/addressbooks/users/${authentication.userName}"
        }
    }

    @Throws(Exception::class)
    fun getAddressBooks() : List<AddressBook> {
        var addressBooks = mutableListOf<AddressBook>()
        if(this.sardine != null) {
            val lst = this.sardine?.list(this.basePath)?.drop(1)
            lst?.forEach { resource ->
                val name = resource.name
                val label = resource.displayName
                val path = "${authentication?.url}${resource.path}"
                addressBooks.add(AddressBook(name, label, path))

            }
            addressBooks = getLabels(this.basePath, addressBooks)
        }
        return addressBooks
    }

    private fun getLabels(path: String, lst: MutableList<AddressBook>): MutableList<AddressBook> {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.newDocument()


            val propFindElement = document.createElementNS("DAV:", "propfind")
            propFindElement.prefix = "d"
            document.appendChild(propFindElement)

            val propElement = document.createElement("d:prop")
            propFindElement.appendChild(propElement)

            val displayNameElement = document.createElement("d:displayname")
            propElement.appendChild(displayNameElement)

            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.ENCODING, "yes")
            val writer = StringWriter()
            transformer.transform(DOMSource(document), StreamResult(writer))
            val output = writer.buffer.toString()

            val okHttp = OkHttpClient()
            val request = Request.Builder()
                .url(path)
                .addHeader("Authorization", Credentials.basic(authentication!!.userName, authentication.password))
                .method("PROPFIND", output.toRequestBody())
                .build()
            val response = okHttp.newCall(request).execute()
            if(response.code < 400) {
                val content = response.body?.string()
                val resultDoc = builder.parse(InputSource(StringReader(content)))
                if(resultDoc.hasChildNodes()) {
                    for(i in 0..<resultDoc.childNodes.length) {
                        val node = resultDoc.childNodes.item(i)
                        if(node.hasChildNodes()) {
                            for(j in 0..<node.childNodes.length) {
                                val subNode = node.childNodes.item(j)
                                if(subNode.childNodes.length == 2) {
                                    val tempPath = subNode.childNodes.item(0).childNodes.item(0).textContent
                                    val baseLblNode = subNode.childNodes.item(1)
                                    var label = ""
                                    if(baseLblNode.hasChildNodes()) {
                                        val child = baseLblNode.childNodes.item(0)
                                        if(child.hasChildNodes()) {
                                            val grandChild = child.childNodes.item(0)
                                            if(grandChild.hasChildNodes()) {
                                                label = grandChild.childNodes.item(0).textContent
                                            }
                                        }
                                    }
                                    val found = lst.find { it.path.endsWith(tempPath) }
                                    if(found != null) {
                                        found.label = label
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(_: Exception) {}
        return lst
    }

    @Throws(Exception::class)
    fun getContacts(addressBook: AddressBook): LinkedList<Contact> {
        val lst = LinkedList<Contact>()

        if(this.sardine != null) {
            try {
                this.sardine?.list(addressBook.path)?.drop(1)?.forEach {  davResource ->
                    try {
                        val cardPath = "${authentication?.url}${davResource.path}"
                        val headers = this.buildHeaders()

                        val inputStream = this.sardine?.get(cardPath, headers)
                        val tmp = Ezvcard.parse(inputStream).all()
                        tmp.forEach { vCard ->
                            lst.add(this.vcardToContact(vCard, cardPath, addressBook))
                        }
                    } catch (ex: Exception) {
                        println(ex.message)
                    }
                }
            } catch (_: Exception) {}
        }

        return lst
    }

    @Throws(Exception::class)
    fun insertContact(contact: Contact): String {
        val cardPath = "${basePath}/${contact.addressBook}/${UUID.randomUUID()}.vcf"
        this.uploadData(cardPath, contact)
        return cardPath
    }

    @Throws(Exception::class)
    fun updateContact(contact: Contact) {
        this.uploadData(contact.path!!, contact)
    }

    @Throws(Exception::class)
    fun deleteContact(contact: Contact) {
        if(this.sardine != null) {
            this.sardine?.delete(contact.path)
        }
    }

    fun fileToModels(data: ByteArray, cm: AddressBook): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val baStream = ByteArrayInputStream(data)
        var vCards: List<VCard>
        baStream.use { stream ->
            vCards = Ezvcard.parse(stream).all()
        }
        vCards.forEach { vCard ->
            contacts.add(vcardToContact(vCard, "", cm))
        }
        return contacts
    }

    @Throws(Exception::class)
    private fun uploadData(path: String, contact: Contact) {
        if(this.sardine != null) {
            val data = Ezvcard.write(this.contactToVCard(contact)).go().toByteArray()
            this.sardine?.put(path, data)
        }
    }

    private fun buildHeaders(): Map<String, String> {
        val headers = LinkedHashMap<String, String>()
        val auth = Credentials.basic(this.authentication?.userName!!, this.authentication.password)
        headers["Authorization"] = auth
        return headers
    }

    private fun vcardToContact(vCard: VCard, path: String, addressBook: AddressBook): Contact {
        val addresses = LinkedList<Address>()
        if(vCard.addresses != null) {
            vCard.addresses.forEach { address ->
                addresses.add(this.propertyToAddress(address, vCard.uid.value))
            }
        }

        val mails = LinkedList<Email>()
        if(vCard.emails != null) {
            vCard.emails.forEach { mail ->
                mails.add(this.propertyToMail(mail, vCard.uid.value))
            }
        }

        val phones = LinkedList<Phone>()
        if(vCard.telephoneNumbers != null) {
            vCard.telephoneNumbers.forEach { phone ->
                phones.add(this.propertyToPhone(phone, vCard.uid.value))
            }
        }

        val birthday = if(vCard.birthday != null ) Converter.temporalToDate(vCard.birthday.date) else null

        val lst = LinkedList<String>()
        vCard.categoriesList.forEach { item ->
            lst.add(item.values.joinToString(","))
        }

        var photo: ByteArray? = null
        if(vCard.photos.size > 0) {
            photo = vCard.photos[0].data
        }

        var suffix = ""
        var prefix = ""
        var additional = ""
        var organization = ""
        var given = ""
        var family = ""
        if(vCard.structuredName != null) {
            suffix =
                if(vCard.structuredName.suffixes!=null) vCard.structuredName.suffixes.joinToString(",") else ""
            prefix =
                if(vCard.structuredName.prefixes!=null) vCard.structuredName.prefixes.joinToString(",") else ""
            additional =
                if(vCard.structuredName.additionalNames!=null) vCard.structuredName.additionalNames.joinToString(",") else ""
            organization =
                if(vCard.organization!=null) vCard.organization.values.joinToString(",") else ""
            given =
                if(vCard.structuredName.given!=null) vCard.structuredName.given else ""
            family =
                if(vCard.structuredName.family!=null) vCard.structuredName.family else ""
        } else if(vCard.formattedName != null) {
            val value = vCard.formattedName.value
            if(value.contains(" ")) {
                given = value.split(" ")[0].trim()
                family = value.replace(given, "").trim()
            } else {
                given = value
            }
        }

        var uid = vCard.uid.value
        if(uid.trim().lowercase().startsWith("http")) {
            val lastPart = uid.split("/")[uid.split("/").size - 1]
            uid = lastPart.replace(".vcf", "")
        }

        val contact = Contact(0L,
            uid, path, suffix, prefix,
            family, given, additional,
            birthday, organization, photo, addressBook.name, "", 0L, Date().time, authentication?.id!!)
        contact.categories = lst
        contact.addresses = addresses
        contact.phoneNumbers = phones
        contact.emailAddresses = mails
        var lastUpdated = -1L
        if(vCard.revision != null) {
            val rev = Converter.temporalToDate(vCard.revision.value)
            if(rev != null) {
                lastUpdated = rev.time
            }
        }
        contact.lastUpdatedContactServer = lastUpdated
        return contact
    }

    private fun contactToVCard(contact: Contact): VCard {
        val addresses = LinkedList<ezvcard.property.Address>()
        contact.addresses.forEach { address ->
            addresses.add(this.addressToProperty(address))
        }

        val mails = LinkedList<ezvcard.property.Email>()
        contact.emailAddresses.forEach { mail ->
            mails.add(this.mailToProperty(mail))
        }

        val phones = LinkedList<Telephone>()
        contact.phoneNumbers.forEach { phone ->
            phones.add(this.phoneToProperty(phone))
        }

        val categories = LinkedList<Categories>()
        contact.categories.forEach {
            val category = Categories()
            it.split(",").forEach { item ->
                category.values.add(item)
            }
            categories.add(category)
        }

        var birthday: Birthday? = null
        if(contact.birthDay!=null) {
            birthday = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                Birthday(LocalDate.ofInstant(contact.birthDay?.toInstant(), ZoneId.systemDefault()))
            } else {
                val cal = Calendar.Builder().setInstant(contact.birthDay!!).build()
                Birthday(
                    Converter.calendarToLocalDate(cal)
                )
            }
        }
        val photo = ezvcard.property.Photo(contact.photo, ImageType.PNG)
        val organization = Organization()
        contact.organization?.split(",")?.toList()?.let { organization.values.addAll(it) }

        val vCard = VCard()
        vCard.uid = Uid(contact.uid)
        vCard.birthday = birthday
        vCard.addresses.addAll(addresses)
        vCard.telephoneNumbers.addAll(phones)
        vCard.emails.addAll(mails)
        vCard.categoriesList.addAll(categories)
        vCard.photos.add(photo)
        vCard.structuredName = StructuredName()
        vCard.structuredName.suffixes.addAll(contact.suffix?.split(",")?.toList()!!)
        vCard.structuredName.prefixes.addAll(contact.prefix?.split(",")?.toList()!!)
        vCard.structuredName.family = contact.familyName
        vCard.structuredName.given = contact.givenName
        vCard.structuredName.additionalNames.addAll(contact.additional?.split(",")?.toList()!!)
        vCard.organization = organization
        vCard.revision = Revision(Date().toInstant())
        return vCard
    }

    private fun propertyToAddress(property: ezvcard.property.Address, id: String) : Address {
        val lst = LinkedList<AddressType>()
        property.types.forEach { type ->
            lst.add(when(type) {
                ezvcard.parameter.AddressType.DOM -> AddressType.domestic
                ezvcard.parameter.AddressType.PARCEL -> AddressType.parcel
                ezvcard.parameter.AddressType.WORK -> AddressType.work
                ezvcard.parameter.AddressType.POSTAL -> AddressType.postal
                ezvcard.parameter.AddressType.INTL -> AddressType.international
                else -> AddressType.home
            })
        }

        return Address(
            0L, id, lst,
            if(property.poBox!=null) property.poBox else "",
            if(property.extendedAddress!=null) property.extendedAddress else "",
            if(property.streetAddress!=null) property.streetAddress else "",
            if(property.locality!=null) property.locality else "",
            if(property.region!=null) property.region else "",
            if(property.postalCode!=null) property.postalCode else "",
            if(property.country!=null) property.country else ""
        )
    }

    private fun addressToProperty(property: Address) : ezvcard.property.Address {
        val lst = LinkedList<ezvcard.parameter.AddressType>()
        property.types.forEach { type ->
            lst.add(when(type) {
                AddressType.domestic -> ezvcard.parameter.AddressType.DOM
                AddressType.parcel -> ezvcard.parameter.AddressType.PARCEL
                AddressType.work -> ezvcard.parameter.AddressType.WORK
                AddressType.postal -> ezvcard.parameter.AddressType.POSTAL
                AddressType.international -> ezvcard.parameter.AddressType.INTL
                else -> ezvcard.parameter.AddressType.HOME
            })
        }


        val address = ezvcard.property.Address()
        address.poBox = property.postOfficeAddress
        address.extendedAddress = property.extendedAddress
        address.streetAddress = property.street
        address.locality = property.locality
        address.region = property.region
        address.postalCode = property.postalCode
        address.country = property.country
        return address
    }

    private fun propertyToMail(property: ezvcard.property.Email, id: String): Email {
        return Email(0L, id, if(property.value!=null) property.value else "")
    }

    private fun mailToProperty(property: Email): ezvcard.property.Email {
        return ezvcard.property.Email(property.value)
    }

    private fun propertyToPhone(property: Telephone, id: String): Phone {
        val lst = LinkedList<PhoneType>()
        property.types.forEach { type ->
            lst.add(when(type.value) {
                TelephoneType.CELL.toString() -> PhoneType.CELL
                TelephoneType.FAX.toString() -> PhoneType.FAX
                TelephoneType.VOICE.toString() -> PhoneType.VOICE
                TelephoneType.WORK.toString() -> PhoneType.WORK
                TelephoneType.MSG.toString() -> PhoneType.MSG
                TelephoneType.PREF.toString() -> PhoneType.PREF
                else -> PhoneType.HOME
            })
        }

        return Phone(0L, id, if(property.text!=null) property.text else "", lst)
    }

    private fun phoneToProperty(phone: Phone): Telephone {
        val lst = LinkedList<TelephoneType>()
        phone.types.forEach { type ->
            lst.add(when(type) {
                PhoneType.CELL -> TelephoneType.CELL
                PhoneType.FAX -> TelephoneType.FAX
                PhoneType.VOICE -> TelephoneType.VOICE
                PhoneType.WORK -> TelephoneType.WORK
                PhoneType.MSG -> TelephoneType.MSG
                PhoneType.PREF -> TelephoneType.PREF
                else -> TelephoneType.HOME
            })
        }

        return Telephone(phone.value)
    }
}