package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.cardav.CarDav
import de.domjos.cloudapp2.cardav.model.AddressBook
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.model.contacts.Contact
import java.util.LinkedList
import java.util.UUID
import javax.inject.Inject

interface ContactRepository {
    val contacts: List<Contact>

    suspend fun loadAddressBooks(hasInternet: Boolean): List<AddressBook>
    fun loadContacts(addressBook: String = ""): List<Contact>
    fun importContacts(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit, deleteLabel: String, insertLabel: String, importLabel: String)
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
        deleteLabel: String,
        insertLabel: String,
        importLabel: String) {
        try {
            // delete all
            updateProgress(0.0f, deleteLabel)
            this.contactDAO.getAll(this.authenticationDAO.getSelectedItem()?.id!!)
                .forEach { this.contactDAO.deleteContact(it) }

            val lst = this.loader.getAddressBooks()
            updateProgress(0.0f, insertLabel)
            lst.forEach { item ->
                // contacts
                val contacts = this.loader.getContacts(item)
                var items = 0
                val factor = 1.0f / contacts.size

                contacts.forEach { contact ->
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

                    contactDAO.insertContact(contact)
                    for(i in 0..<contact.addresses.size) {
                        if(contact.addresses[i].street != "") {
                            contact.addresses[i].contactId = uid
                            contact.addresses[i].id = contactDAO.insertAddress(contact.addresses[i])
                        }
                    }
                    for(i in 0..<contact.phoneNumbers.size) {
                        contact.phoneNumbers[i].contactId = uid
                        contact.phoneNumbers[i].id = contactDAO.insertPhone(contact.phoneNumbers[i])
                    }
                    for(i in 0..<contact.emailAddresses.size) {
                        contact.emailAddresses[i].contactId = uid
                        contact.emailAddresses[i].id = contactDAO.insertEmail(contact.emailAddresses[i])
                    }

                    items++
                    updateProgress(factor * items, String.format(importLabel, if(item.label!=null) item.label!! else item.name))
                }
            }
        } finally {
            onFinish()
        }
    }

    override fun loadContacts(addressBook: String): List<Contact> {
        this.addressBook = addressBook
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
        }

        return this.contacts
    }

    override fun insertOrUpdateContact(hasInternet: Boolean, contact: Contact) {
        contact.authId = authenticationDAO.getSelectedItem()!!.id
        var newItem = false
        if(contact.uid == "") {
            contact.uid = UUID.randomUUID().toString()
            newItem = true
        }
        if(hasInternet) {
            if(newItem) {
                contact.path = this.loader.insertContact(contact)
            } else {
                this.loader.updateContact(contact)
            }
        }
        contact.addressBook = this.addressBook

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
        this.loader.deleteContact(contact)
        this.contactDAO.deleteContact(contact)
    }
}