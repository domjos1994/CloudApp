package de.domjos.cloudapp2.data.repository

import android.content.Context
import de.domjos.cloudapp2.cardav.CarDav
import de.domjos.cloudapp2.cardav.model.AddressBook
import de.domjos.cloudapp2.data.syncer.ContactSync
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.rest.model.room.RoomInput
import de.domjos.cloudapp2.rest.model.room.Type
import de.domjos.cloudapp2.rest.model.shares.Types
import de.domjos.cloudapp2.rest.requests.AutocompleteRequest
import de.domjos.cloudapp2.rest.requests.RoomRequest
import kotlinx.coroutines.flow.first
import java.util.Date
import java.util.LinkedList
import java.util.UUID
import javax.inject.Inject

interface ContactRepository {
    val contacts: List<Contact>

    suspend fun loadAddressBooks(hasInternet: Boolean): List<AddressBook>
    suspend fun loadContacts(addressBook: String = ""): List<Contact>
    suspend fun getOrCreateChat(contact: Contact): String
    fun importContacts(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit, loadingLabel: String, deleteLabel: String, insertLabel: String, importLabel: String, context: Context)
    fun insertOrUpdateContact(hasInternet: Boolean, contact: Contact)
    fun deleteContact(hasInternet: Boolean, contact: Contact)
    fun hasAuthentications(): Boolean
}

class DefaultContactRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO,
    private val contactDAO: ContactDAO
) : ContactRepository {
    private val loader = CarDav(authenticationDAO.getSelectedItem())
    private var addressBook: String = ""
    override var contacts = if(authenticationDAO.getSelectedItem()!=null) {
        contactDAO.getAll(authenticationDAO.getSelectedItem()!!.id)
    } else {
        listOf()
    }

    override suspend fun loadAddressBooks(hasInternet: Boolean): List<AddressBook> {
        if(hasInternet) {
            return this.loader.getAddressBooks()
        } else {
            val items = mutableListOf<AddressBook>()
            this.contactDAO.getAddressBooks(this.authenticationDAO.getSelectedItem()?.id!!).forEach { item ->
                items.add(AddressBook(item, item, ""))
            }
            return items
        }
    }

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
    }

    override fun importContacts(
        updateProgress: (Float, String) -> Unit,
        onFinish: ()->Unit,
        loadingLabel: String,
        deleteLabel: String,
        insertLabel: String,
        importLabel: String,
        context: Context) {

        val sync = ContactSync(contactDAO, authenticationDAO)
        sync.sync(updateProgress, onFinish, loadingLabel, deleteLabel, insertLabel, importLabel, context)
    }

    override suspend fun loadContacts(addressBook: String): List<Contact> {
        this.addressBook = addressBook
        val items = try {
            val auth = this.authenticationDAO.getSelectedItem()
            val auto = AutocompleteRequest(auth)
            auto.getItem(Types.User, "").first()
        } catch (_: Exception) {
            listOf()
        }
        this.contacts = contactDAO.getAddressBook(addressBook, authenticationDAO.getSelectedItem()!!.id)
        val phones = contactDAO.getAddressBookWithPhones(addressBook, authenticationDAO.getSelectedItem()!!.id)
        val addresses = contactDAO.getAddressBookWithAddresses(addressBook, authenticationDAO.getSelectedItem()!!.id)
        val emails = contactDAO.getAddressBookWithEmails(addressBook, authenticationDAO.getSelectedItem()!!.id)

        this.contacts.forEach { contact ->
            phones.forEach {
                if(contact.uid==it.contact.uid) {
                    contact.phoneNumbers = LinkedList()
                    contact.phoneNumbers.addAll(it.phones)
                }
            }

            addresses.forEach {
                if(contact.uid==it.contact.uid) {
                    contact.addresses = LinkedList()
                    contact.addresses.addAll(it.addresses)
                }
            }

            emails.forEach {
                if(contact.uid==it.contact.uid) {
                    contact.emailAddresses = LinkedList()
                    contact.emailAddresses.addAll(it.emails)
                }
            }


            contact.contactToChat = items.any { it == contact.familyName }
        }

        return this.contacts
    }

    override suspend fun getOrCreateChat(contact: Contact): String {
        try {
            val request = RoomRequest(this.authenticationDAO.getSelectedItem())
            val rooms = request.getRooms().first()
            var fRooms = rooms.filter {
                it.type == Type.OneToOne.value &&
                        it.name == contact.familyName

            }
            if(fRooms.isNotEmpty()) {
                return fRooms[0].token
            } else {
                val roomInput = RoomInput(
                    roomType = Types.User.value,
                    invite = contact.familyName ?: "",
                    roomName = contact.familyName
                )
                request.addRoom(roomInput)
                fRooms = request.getRooms().first().filter {
                    it.type == Types.toInt(Types.User) &&
                            it.name == contact.familyName

                }
                return if(fRooms.isNotEmpty()) {
                    fRooms[0].token
                } else {
                    ""
                }
            }
        } catch (_: Exception) {
            return ""
        }
    }

    override fun insertOrUpdateContact(hasInternet: Boolean, contact: Contact) {
        contact.authId = authenticationDAO.getSelectedItem()!!.id
        if(contact.uid == "") {
            contact.uid = UUID.randomUUID().toString()
        }
        contact.addressBook = this.addressBook
        contact.lastUpdatedContactApp = Date().time

        val uid = contact.uid!!
        try {
            if(this.authenticationDAO.getSelectedItem() != null) {
                val tmp = this.contactDAO.getAll(this.authenticationDAO.getSelectedItem()!!.id, uid)
                if(tmp != null) {
                    contact.contactId = tmp.contactId
                    contact.lastUpdatedContactPhone = tmp.lastUpdatedContactPhone
                }
            }
            contactDAO.deleteAddresses(uid)
            contactDAO.deleteEmails(uid)
            contactDAO.deletePhones(uid)
            contactDAO.deleteContact(uid)
        } catch (_: Exception) {}

        this.contactDAO.insertContact(contact)
        for(i in 0..<contact.addresses.size) {
            contact.addresses[i].contactId = uid
            contact.addresses[i].id = contactDAO.insertAddress(contact.addresses[i])
        }
        for(i in 0..<contact.phoneNumbers.size) {
            contact.phoneNumbers[i].contactId = uid
            contact.phoneNumbers[i].id = contactDAO.insertPhone(contact.phoneNumbers[i])
        }
        for(i in 0..<contact.emailAddresses.size) {
            contact.emailAddresses[i].contactId = uid
            contact.emailAddresses[i].id = contactDAO.insertEmail(contact.emailAddresses[i])
        }
    }

    override fun deleteContact(hasInternet: Boolean, contact: Contact) {
        this.contactDAO.deleteContact(contact.id)
    }
}