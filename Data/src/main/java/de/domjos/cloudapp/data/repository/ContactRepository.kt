package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.cardav.ContactLoader
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.dao.CalendarEventDAO
import de.domjos.cloudapp.database.dao.ContactDAO
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.database.model.contacts.Contact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.LinkedList
import javax.inject.Inject

interface ContactRepository {
    val contacts: List<Contact>

    suspend fun loadAddressBooks(): List<String>
    fun loadContacts(addressBook: String = ""): List<Contact>
    fun importContacts(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit)
    fun insertOrUpdateContact(hasInternet: Boolean, contact: Contact)
    fun deleteContact(hasInternet: Boolean, contact: Contact)
}

class DefaultContactRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO,
    private val contactDAO: ContactDAO
) : ContactRepository {
    private val loader = ContactLoader(authenticationDAO.getSelectedItem()!!)
    private var addressBook: String = ""
    override var contacts = contactDAO.getAll(authenticationDAO.getSelectedItem()!!.id)

    override suspend fun loadAddressBooks(): List<String> {
        return this.contactDAO.getAddressBooks(authenticationDAO.getSelectedItem()!!.id)
    }

    override fun importContacts(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit) {
        // delete all
        updateProgress(0.0f, "Delete")
        contactDAO.deleteAllEmails()
        contactDAO.deleteAllPhones()
        contactDAO.deleteAllAddresses()
        contactDAO.deleteAllContacts()

        val lst = this.loader.getAddressBooks()
        updateProgress(0.0f, "Insert")
        lst.forEach { item ->
            // contacts
            val contacts = this.loader.loadAddressBook(item)
            var items = 0
            val factor = 1.0f / contacts.size

            contacts.forEach { contact ->
                val uid = contact.uid
                contactDAO.insertContact(contact)
                for(i in 0..<contact.addresses?.size!!) {
                    contact.addresses!![i].contactId = uid
                    contact.addresses!![i].id = contactDAO.insertAddress(contact.addresses!![i])
                }
                for(i in 0..<contact.phoneNumbers?.size!!) {
                    contact.phoneNumbers!![i].contactId = uid
                    contact.phoneNumbers!![i].id = contactDAO.insertPhone(contact.phoneNumbers!![i])
                }
                for(i in 0..<contact.emailAddresses?.size!!) {
                    contact.emailAddresses!![i].contactId = uid
                    contact.emailAddresses!![i].id = contactDAO.insertEmail(contact.emailAddresses!![i])
                }

                items++
                updateProgress(factor * items, item)
            }
        }

        onFinish()
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
                    contact.phoneNumbers!!.addAll(it.phones)
                }
            }

            addresses.forEach {
                if(contact.uid==it.contact.uid) {
                    contact.addresses = LinkedList()
                    contact.addresses!!.addAll(it.addresses)
                }
            }

            emails.forEach {
                if(contact.uid==it.contact.uid) {
                    contact.emailAddresses = LinkedList()
                    contact.emailAddresses!!.addAll(it.emails)
                }
            }
        }

        return this.contacts
    }

    override fun insertOrUpdateContact(hasInternet: Boolean, contact: Contact) {
        contact.authId = authenticationDAO.getSelectedItem()!!.id
        this.loader.insertContact(this.addressBook, contact)
    }

    override fun deleteContact(hasInternet: Boolean, contact: Contact) {
        this.loader.deleteContact(this.addressBook, contact)
    }
}