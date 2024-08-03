/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.cardav

import android.Manifest
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import de.domjos.cloudapp2.database.model.Authentication

import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith
import java.util.Properties

import de.domjos.cloudapp2.cardav.test.R
import de.domjos.cloudapp2.cardav.utils.Converter
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.Locale

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class CarDavTest {
    private lateinit var carDav: CarDav

    @Rule
    @JvmField
    var runtimePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)

    companion object {
        private var context: Context? = null
        @JvmStatic
        private var authentication: Authentication? = null
        @JvmStatic
        private var props: Properties? = null

        /**
         * Read connection
         * and initialize authentication
         */
        @JvmStatic
        @BeforeClass
        fun before() {
            this.context = InstrumentationRegistry.getInstrumentation().targetContext
            val stream = this.context!!.resources.openRawResource(R.raw.example)
            props = Properties()
            props?.load(stream)
            stream.close()

            authentication = Authentication(
                1L, "N28", props!!["url"].toString(),
                props!!["user"].toString(), props!!["pwd"].toString(),
                true, "", null
            )
        }
    }

    @Before
    fun init() {
        this.carDav = CarDav(authentication)
    }

    @Test
    fun getAddressBooksTest() {
        val addressBooks = this.carDav.getAddressBooks()
        assertEquals(3, addressBooks.size)
    }

    @Test
    fun getContactsTest() {
        val contacts = loadAll()
        assertNotEquals(0, contacts.size)
    }

    @Test
    fun insertAndDeleteMinContactTest() {
        // check contacts
        val contacts = loadAll()
        assertNotEquals(0, contacts.size)

        // insert contact
        val contact = Contact(
            givenName = "test",
            addressBook = "test-1",
            authId = authentication?.id!!
        )
        contact.path = this.carDav.insertContact(contact)
        assertNotEquals(contacts.size, this.loadAll().size)

        // delete the contacts
        this.carDav.deleteContact(contact)
        assertEquals(contacts.size, this.loadAll().size)
    }

    @Test
    fun updateMinContactTest() {
        // check contacts
        val contacts = loadAll()
        assertNotEquals(0, contacts.size)

        // insert contact
        val contact = Contact(
            givenName = "test",
            addressBook = "test-1",
            authId = authentication?.id!!
        )
        contact.path = this.carDav.insertContact(contact)
        assertNotEquals(contacts.size, this.loadAll().size)

        contact.givenName = "test-2"
        this.carDav.updateContact(contact)

        val newContacts = this.loadAll()
        val item = newContacts.find { it.givenName == contact.givenName }
        assertNotNull(item)

        // delete the contacts
        this.carDav.deleteContact(contact)
        assertEquals(contacts.size, this.loadAll().size)
    }

    @Test
    fun insertAndDeleteMaxContactTest() {
        // check contacts
        val contacts = loadAll()
        assertNotEquals(0, contacts.size)

        // insert contact
        val contact = this.buildContact()
        contact.path = this.carDav.insertContact(contact)
        val items = this.loadAll()
        assertNotEquals(contacts.size, items.size)

        val insertedContact = items.find { it.givenName == contact.givenName && it.familyName == contact.familyName }!!
        assertNotNull(insertedContact)

        assertEquals(Converter.dateToString(contact.birthDay!!), Converter.dateToString(insertedContact.birthDay!!))
        assertEquals(contact.prefix, insertedContact.prefix)
        assertEquals(contact.phoneNumbers.size, insertedContact.phoneNumbers.size)
        assertEquals(contact.emailAddresses.size, insertedContact.emailAddresses.size)
        assertEquals(contact.addresses.size, insertedContact.addresses.size)

        // delete the contacts
        this.carDav.deleteContact(contact)
        assertEquals(contacts.size, this.loadAll().size)
    }

    private fun loadAll(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        this.carDav.getAddressBooks().forEach { addressBook ->
            contacts.addAll(this.carDav.getContacts(addressBook))
        }
        return contacts
    }

    private fun buildContact(): Contact {
        val contact = Contact(
            familyName = "Beck",
            givenName = "Margery",
            prefix = "M.",
            addressBook = "test-1",
            authId = authentication?.id!!
        )
        val birthDay = Calendar.getInstance(Locale.getDefault())
        birthDay.set(Calendar.YEAR, 1967)
        birthDay.set(Calendar.MONTH, Calendar.DECEMBER)
        birthDay.set(Calendar.DAY_OF_MONTH, 16)
        contact.birthDay = birthDay.time

        val address = Address(street = "805 Francis Mine", postalCode = "96137", locality = "Lake Almanor")
        contact.addresses.add(address)

        val email = Email(value = "MargeryMBeck@jourrapide.com")
        contact.emailAddresses.add(email)

        val phone = Phone(value = "530-259-4753")
        contact.phoneNumbers.add(phone)

        return contact
    }
}