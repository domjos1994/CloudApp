/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.cardav

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import java.util.Date
import java.util.LinkedList
import java.util.UUID

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ContactLoaderTest : BasicTest() {
    @Test
    fun testLoadAddressBooks() {
        val loader = ContactLoader(authentication)
        assertNotEquals(0, loader.getAddressBooks().size)
    }

    @Test
    fun testLoadAddressBook() {
        val loader = ContactLoader(authentication)
        val addressBooks = loader.getAddressBooks()
        assertNotEquals(0, addressBooks.size)

        val contacts = loader.loadAddressBook(addressBooks[1])
        assertNotEquals(0, contacts.size)
    }

    @Test
    fun testInsertContacts() {
        val loader = ContactLoader(authentication)
        val addressBooks = loader.getAddressBooks()
        assertNotEquals(0, addressBooks.size)
        val uid = UUID.randomUUID().toString()

        val contact = Contact(0L, uid,
            "/admin", "", "", "Joas", "Dominic2", "",
            Date(), "", null, addressBooks[0], "",
            0L, 0L, 0L
        )
        contact.categories = LinkedList<String>()
        contact.addresses = LinkedList<Address>()
        contact.phoneNumbers = LinkedList<Phone>()
        contact.emailAddresses = LinkedList<Email>()

        loader.insertContact(contact)
        var contacts = loader.loadAddressBook(addressBooks[1])

        var id = ""
        contacts.forEach { item ->
            if(item.givenName=="Dominic2") {
                id = item.uid
            }
        }
        assertNotNull(id)

        loader.deleteContact(contact)
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
        val loader = ContactLoader(authentication)
        val addressBooks = loader.getAddressBooks()
        assertNotEquals(0, addressBooks.size)
        val uid = UUID.randomUUID().toString()
        val path = "/remote.php/dav/addressbooks/users/${authentication?.userName}/${addressBooks[0]}"

        val contact = Contact(
            0L, uid, path, "", "","Joas", "Dominic2", "",
            Date(), "", null, addressBooks[0], "",
            0L, 0L, 0L
        )
        contact.categories = LinkedList<String>()
        contact.addresses = LinkedList<Address>()
        contact.phoneNumbers = LinkedList<Phone>()
        contact.emailAddresses = LinkedList<Email>()
        loader.insertContact(contact)
        var contacts = loader.loadAddressBook(addressBooks[0])

        var c: Contact? = null
        contacts.forEach { item ->
            if(item.givenName=="Dominic2") {
                c = item
            }
        }
        assertNotNull(c)

        c?.givenName = "Dominic3"
        loader.insertContact(c!!)

        contacts = loader.loadAddressBook(addressBooks[1])

        c = null
        contacts.forEach { item ->
            if(item.givenName=="Dominic3") {
                c = item
            }
        }
        assertNotNull(c)

        loader.deleteContact(contact)
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