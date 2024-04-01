package de.domjos.cloudapp.cardav

import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.database.model.contacts.Address
import de.domjos.cloudapp.database.model.contacts.Contact
import de.domjos.cloudapp.database.model.contacts.Email
import de.domjos.cloudapp.database.model.contacts.Phone
import org.junit.Test

import org.junit.Assert.*
import java.util.Date
import java.util.LinkedList
import java.util.UUID

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ContactLoaderTest {
    @Test
    fun testLoadAddressBooks() {
        val authentication = Authentication(
            0L, "cloud", "https://cloud.dojodev.de", "domjos",
            "wRsesbg2M9D7CW3Uc68E", true, null, null
        )

        val loader = ContactLoader(authentication)
        assertNotEquals(0, loader.getAddressBooks().size)
    }

    @Test
    fun testLoadAddressBook() {
        val authentication = Authentication(
            0L, "cloud", "https://cloud.dojodev.de", "domjos",
            "wRsesbg2M9D7CW3Uc68E", true, null, null
        )

        val loader = ContactLoader(authentication)
        val addressBooks = loader.getAddressBooks()
        assertNotEquals(0, addressBooks.size)

        val contacts = loader.loadAddressBook(addressBooks[1])
        assertNotEquals(0, contacts.size)
    }

    @Test
    fun testInsertContacts() {
        val authentication = Authentication(
            0L, "cloud", "https://cloud.dojodev.de", "domjos",
            "wRsesbg2M9D7CW3Uc68E", true, null, null
        )

        val loader = ContactLoader(authentication)
        val addressBooks = loader.getAddressBooks()
        assertNotEquals(0, addressBooks.size)
        val uid = UUID.randomUUID().toString()

        val contact = Contact(
            uid,
            "", "", "Joas", "Dominic2", "",
            Date(), "", null, addressBooks[0]
        )
        contact.categories = LinkedList<String>()
        contact.addresses = LinkedList<Address>()
        contact.phoneNumbers = LinkedList<Phone>()
        contact.emailAddresses = LinkedList<Email>()

        loader.insertContact(addressBooks[1], contact)
        var contacts = loader.loadAddressBook(addressBooks[1])

        var id = ""
        contacts.forEach { item ->
            if(item.givenName=="Dominic2") {
                id = item.uid
            }
        }
        assertNotNull(id)

        loader.deleteContact(addressBooks[1], contact)
        contacts = loader.loadAddressBook(addressBooks[1])

        id = ""
        contacts.forEach { item ->
            if(item.givenName=="Dominic2") {
                id = item.uid
            }
        }
        assertEquals("", id)
    }

    @Test
    fun testUpdateContact() {
        val authentication = Authentication(
            0L, "cloud", "https://cloud.dojodev.de", "domjos",
            "wRsesbg2M9D7CW3Uc68E", true, null, null
        )

        val loader = ContactLoader(authentication)
        val addressBooks = loader.getAddressBooks()
        assertNotEquals(0, addressBooks.size)
        val uid = UUID.randomUUID().toString()

        val contact = Contact(
            uid,
            "", "", "Joas", "Dominic2", "",
            Date(), "", null, addressBooks[0]
        )
        contact.categories = LinkedList<String>()
        contact.addresses = LinkedList<Address>()
        contact.phoneNumbers = LinkedList<Phone>()
        contact.emailAddresses = LinkedList<Email>()
        loader.insertContact(addressBooks[1], contact)
        var contacts = loader.loadAddressBook(addressBooks[1])

        var c: Contact? = null
        contacts.forEach { item ->
            if(item.givenName=="Dominic2") {
                c = item
            }
        }
        assertNotNull(c)

        c?.givenName = "Dominic3"
        loader.insertContact(addressBooks[1], c!!)

        contacts = loader.loadAddressBook(addressBooks[1])

        c = null
        contacts.forEach { item ->
            if(item.givenName=="Dominic3") {
                c = item
            }
        }
        assertNotNull(c)

        loader.deleteContact(addressBooks[1], contact)
        contacts = loader.loadAddressBook(addressBooks[1])

        c = null
        contacts.forEach { item ->
            if(item.givenName=="Dominic3") {
                c = item
            }
        }
        assertNull(c)
    }
}