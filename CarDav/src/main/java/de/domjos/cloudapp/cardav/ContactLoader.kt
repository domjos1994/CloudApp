package de.domjos.cloudapp.cardav

import android.os.Build
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.database.model.contacts.Address
import de.domjos.cloudapp.database.model.contacts.AddressType
import de.domjos.cloudapp.database.model.contacts.Contact
import de.domjos.cloudapp.database.model.contacts.Email
import de.domjos.cloudapp.database.model.contacts.Phone
import de.domjos.cloudapp.database.model.contacts.PhoneType
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.parameter.ImageType
import ezvcard.parameter.TelephoneType
import ezvcard.property.Birthday
import ezvcard.property.Categories
import ezvcard.property.Organization
import ezvcard.property.StructuredName
import ezvcard.property.Telephone
import ezvcard.property.Uid
import okhttp3.Credentials
import okhttp3.internal.notify
import java.io.ByteArrayInputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.time.temporal.TemporalField
import java.util.Date
import java.util.LinkedList
import java.util.Locale

class ContactLoader(private val authentication: Authentication) {
    private val sardine: OkHttpSardine = OkHttpSardine()
    private val basePath = "${authentication.url}/remote.php/dav/addressbooks/users/${authentication.userName}"

    init {
        this.sardine.setCredentials(authentication.userName, authentication.password)
    }

    fun getAddressBooks() : List<String> {
        val addressBooks = LinkedList<String>()

        var header = true
        this.sardine.list(this.basePath).forEach { davResource ->
            if(header) {
                header = false
            } else {
                addressBooks.add(this.getName(davResource.path))
            }
        }
        return addressBooks
    }

    fun loadAddressBook(name: String): LinkedList<Contact> {
        val lst = LinkedList<Contact>()
        val path = "$basePath/$name"

        try {
            var header = true
            this.sardine.list(path).forEach {  davResource ->
                if(header) {
                    header = false
                } else {
                    try {
                        val cardPath = "${authentication.url}${davResource.path}"
                        val headers = this.buildHeaders()

                        val inputStream = this.sardine.get(cardPath, headers)
                        val tmp = Ezvcard.parse(inputStream).all()
                        tmp.forEach { vCard ->
                            lst.add(this.vcardToContact(vCard, name))
                        }
                    } catch (ex: Exception) {
                        println(ex.message)
                    }
                }
            }
        } catch (_: Exception) {}

        return lst
    }

    fun insertContact(name: String, contact: Contact) {
        val cardPath = "${basePath}/$name/${contact.uid}.vcf"
        this.sardine.put(cardPath, Ezvcard.write(this.contactToVCard(contact)).go().toByteArray())
    }

    fun deleteContact(name: String, contact: Contact) {
        val cardPath = "${basePath}/$name/${contact.uid}.vcf"
        this.sardine.delete(cardPath)
    }

    private fun getName(path: String): String {
        var name = path
        if(name.endsWith("/")) {
            name = name.substring(0, path.length - 1)
        }

        return name.split("/")[name.split("/").size - 1]
    }

    private fun buildHeaders(): Map<String, String> {
        val headers = LinkedHashMap<String, String>()
        val auth = Credentials.basic(this.authentication.userName, this.authentication.password)
        headers["Authorization"] = auth
        return headers
    }

    private fun vcardToContact(vCard: VCard, name: String): Contact {
        val addresses = LinkedList<Address>()
        vCard.addresses.forEach { address ->
            addresses.add(this.propertyToAddress(address, vCard.uid.value))
        }

        val mails = LinkedList<Email>()
        vCard.emails.forEach { mail ->
            mails.add(this.propertyToMail(mail, vCard.uid.value))
        }

        val phones = LinkedList<Phone>()
        vCard.telephoneNumbers.forEach { phone ->
            phones.add(this.propertyToPhone(phone, vCard.uid.value))
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val birthday = if(vCard.birthday != null ) {
            var year: Int? = null
            var month: Int? = null
            var day: Int? = null
            if(vCard.birthday.date is LocalDate) {
                year = (vCard.birthday.date as LocalDate).year
                month = (vCard.birthday.date as LocalDate).monthValue
                day = (vCard.birthday.date as LocalDate).dayOfMonth
            }
            if(vCard.birthday.date is Instant) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    year = LocalDate.ofInstant(vCard.birthday.date as Instant, ZoneId.systemDefault())?.year
                    month = LocalDate.ofInstant(vCard.birthday.date as Instant, ZoneId.systemDefault())?.monthValue
                    day = LocalDate.ofInstant(vCard.birthday.date as Instant, ZoneId.systemDefault())?.dayOfMonth
                } else {
                    year = LocalDate.from(vCard.birthday.date).year
                    month = LocalDate.from(vCard.birthday.date).monthValue
                    year = LocalDate.from(vCard.birthday.date).dayOfMonth
                }
            }
            sdf.parse("$day.$month.$year")
        } else null

        val lst = LinkedList<String>()
        vCard.categoriesList.forEach { item ->
            lst.add(item.values.joinToString(","))
        }

        var photo: ByteArray? = null
        if(vCard.photos.size > 0) {
            photo = vCard.photos[0].data
        }

        val suffix =
            if(vCard.structuredName.suffixes!=null) vCard.structuredName.suffixes.joinToString(",") else ""
        val prefix =
            if(vCard.structuredName.prefixes!=null) vCard.structuredName.prefixes.joinToString(",") else ""
        val additional =
            if(vCard.structuredName.additionalNames!=null) vCard.structuredName.additionalNames.joinToString(",") else ""
        val organization =
            if(vCard.organization!=null) vCard.organization.values.joinToString(",") else ""

        val contact = Contact(
            vCard.uid.value, suffix, prefix,
            vCard.structuredName.family,
            vCard.structuredName.given, additional,
            birthday, organization, photo, name, authentication.id)
        contact.categories = lst
        contact.addresses = addresses
        contact.phoneNumbers = phones
        contact.emailAddresses = mails
        return contact
    }

    private fun contactToVCard(contact: Contact): VCard {
        val addresses = LinkedList<ezvcard.property.Address>()
        contact.addresses?.forEach { address ->
            addresses.add(this.addressToProperty(address))
        }

        val mails = LinkedList<ezvcard.property.Email>()
        contact.emailAddresses?.forEach { mail ->
            mails.add(this.mailToProperty(mail))
        }

        val phones = LinkedList<Telephone>()
        contact.phoneNumbers?.forEach { phone ->
            phones.add(this.phoneToProperty(phone))
        }

        val categories = LinkedList<Categories>()
        contact.categories?.forEach {
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
                Birthday(
                    LocalDate.of(contact.birthDay?.year!!, contact.birthDay?.month!!, contact.birthDay?.day!!)
                )
            }
        }
        val photo = ezvcard.property.Photo(contact.photo, ImageType.PNG)
        val organization = Organization()
        organization.values.addAll(contact.organization.split(",").toList())

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
            0L, id, lst, if(property.poBox!=null) property.poBox else "",
            property.extendedAddress,
            property.streetAddress,
            property.locality, property.region, property.postalCode, property.country
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
        return Email(0L, id, property.value)
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

        return Phone(0L, id, property.text, lst)
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