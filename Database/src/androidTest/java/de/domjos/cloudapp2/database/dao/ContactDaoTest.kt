/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.BaseTest
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.AddressType
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone
import de.domjos.cloudapp2.database.model.contacts.PhoneType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.LinkedList
import java.util.UUID

/**
 * Testing the contact dao
 * @see de.domjos.cloudapp2.database.dao.ContactDAO
 * @author Dominic Joas
 */
@RunWith(AndroidJUnit4::class)
class ContactDaoTest : BaseTest() {
    private lateinit var contactDAO: ContactDAO
    private lateinit var dummy: Contact
    private lateinit var dummyAddress: Address
    private lateinit var dummyMail: Email
    private lateinit var dummyPhone: Phone

    /**
     * Initialize Tests
     */
    @Before
    fun before() {
        super.init()
        contactDAO = super.db.contactDao()

        dummy = Contact(
            0L, UUID.randomUUID().toString(), "", "", "",
            "Doe", "John", "", null, "", null,
            "Test", "", 0L, 0L, 0L
        )

        val lst = LinkedList<AddressType>()
        lst.add(AddressType.home)
        dummyAddress = Address(
            0L, this.dummy.uid, lst, "", "",
            "Test-Street 1", "Test-City", "", "12345", ""
        )
        dummyMail = Email(0L, this.dummy.uid, "test@test.de")
        val ph = LinkedList<PhoneType>()
        ph.add(PhoneType.HOME)
        dummyPhone = Phone(0L, this.dummy.uid, "12345 67890", ph)
    }

    /**
     * Testing Inserting and deleting contact
     */
    @Test
    fun testInsertingAndDeletingContact() {
        this.testInsert()

        this.testDelete()
    }

    /**
     * Testing updating and deleting contact
     */
    @Test
    fun testUpdatingAndDeletingContact() {
        this.testInsert()

        // update and check data
        dummy.familyName = "Test2"
        this.contactDAO.updateContact(dummy)
        dummy = dummy.uid?.let { this.contactDAO.getAll(0L, it) }!!
        assertEquals("Test2", dummy.familyName)

        this.testDelete()
    }

    /**
     * Testing Inserting and deleting address
     */
    @Test
    fun testInsertingAndDeletingAddress() {
        this.testInsert()

        // insert address
        this.contactDAO.insertAddress(this.dummyAddress)
        var lst = this.contactDAO.getAllWithAddresses(0L)
        assertEquals(1, lst.size)
        assertEquals(1, lst[0].addresses.size)
        assertEquals(this.dummyAddress.street, lst[0].addresses[0].street)
        this.dummyAddress = lst[0].addresses[0]

        // delete address
        this.contactDAO.deleteAddress(this.dummyAddress)
        lst = this.contactDAO.getAllWithAddresses(0L)
        assertEquals(1, lst.size)
        assertEquals(0, lst[0].addresses.size)

        this.testDelete()
    }

    /**
     * Testing updating and deleting address
     */
    @Test
    fun testUpdatingAndDeletingAddress() {
        this.testInsert()

        // insert address
        this.contactDAO.insertAddress(this.dummyAddress)
        var lst = this.contactDAO.getAllWithAddresses(0L)
        assertEquals(1, lst.size)
        assertEquals(1, lst[0].addresses.size)
        assertEquals(this.dummyAddress.street, lst[0].addresses[0].street)
        this.dummyAddress = lst[0].addresses[0]

        this.dummyAddress.street = "Test"
        this.contactDAO.updateAddress(this.dummyAddress)
        assertEquals(this.dummyAddress.street, this.contactDAO.getAllWithAddresses(0L)[0].addresses[0].street)

        // delete address
        this.contactDAO.deleteAddress(this.dummyAddress)
        lst = this.contactDAO.getAllWithAddresses(0L)
        assertEquals(1, lst.size)
        assertEquals(0, lst[0].addresses.size)

        this.testDelete()
    }

    /**
     * Testing Inserting and deleting phone
     */
    @Test
    fun testInsertingAndDeletingPhone() {
        this.testInsert()

        // insert phone
        this.contactDAO.insertPhone(this.dummyPhone)
        var lst = this.contactDAO.getAllWithPhones(0L)
        assertEquals(1, lst.size)
        assertEquals(1, lst[0].phones.size)
        assertEquals(this.dummyPhone.value, lst[0].phones[0].value)
        this.dummyPhone = lst[0].phones[0]

        // delete phone
        this.contactDAO.deletePhone(this.dummyPhone)
        lst = this.contactDAO.getAllWithPhones(0L)
        assertEquals(1, lst.size)
        assertEquals(0, lst[0].phones.size)

        this.testDelete()
    }

    /**
     * Testing updating and deleting phone
     */
    @Test
    fun testUpdatingAndDeletingPhone() {
        this.testInsert()

        // insert phone
        this.contactDAO.insertPhone(this.dummyPhone)
        var lst = this.contactDAO.getAllWithPhones(0L)
        assertEquals(1, lst.size)
        assertEquals(1, lst[0].phones.size)
        assertEquals(this.dummyPhone.value, lst[0].phones[0].value)
        this.dummyPhone = lst[0].phones[0]

        this.dummyPhone.value = "Test"
        this.contactDAO.updatePhone(this.dummyPhone)
        assertEquals(this.dummyPhone.value, this.contactDAO.getAllWithPhones(0L)[0].phones[0].value)

        // delete phone
        this.contactDAO.deletePhone(this.dummyPhone)
        lst = this.contactDAO.getAllWithPhones(0L)
        assertEquals(1, lst.size)
        assertEquals(0, lst[0].phones.size)

        this.testDelete()
    }

    /**
     * Testing Inserting and deleting mail
     */
    @Test
    fun testInsertingAndDeletingMail() {
        this.testInsert()

        // insert email
        this.contactDAO.insertEmail(this.dummyMail)
        var lst = this.contactDAO.getAllWithEmails(0L)
        assertEquals(1, lst.size)
        assertEquals(1, lst[0].emails.size)
        assertEquals(this.dummyMail.value, lst[0].emails[0].value)
        this.dummyMail = lst[0].emails[0]

        // delete email
        this.contactDAO.deleteEmail(this.dummyMail)
        lst = this.contactDAO.getAllWithEmails(0L)
        assertEquals(1, lst.size)
        assertEquals(0, lst[0].emails.size)

        this.testDelete()
    }

    /**
     * Testing updating and deleting mail
     */
    @Test
    fun testUpdatingAndDeletingMail() {
        this.testInsert()

        // insert email
        this.contactDAO.insertEmail(this.dummyMail)
        var lst = this.contactDAO.getAllWithEmails(0L)
        assertEquals(1, lst.size)
        assertEquals(1, lst[0].emails.size)
        assertEquals(this.dummyMail.value, lst[0].emails[0].value)
        this.dummyMail = lst[0].emails[0]

        this.dummyMail.value = "Test"
        this.contactDAO.updateEmail(this.dummyMail)
        assertEquals(this.dummyMail.value, this.contactDAO.getAllWithEmails(0L)[0].emails[0].value)

        // delete email
        this.contactDAO.deleteEmail(this.dummyMail)
        lst = this.contactDAO.getAllWithEmails(0L)
        assertEquals(1, lst.size)
        assertEquals(0, lst[0].emails.size)

        this.testDelete()
    }

    /**
     * Test inserting contact
     */
    private fun testInsert() {
        // check contact doesn't exist
        var contacts = contactDAO.getAll(0L)
        assertEquals(0, contacts.size)

        // insert contact
        contactDAO.insertContact(dummy)
        dummy = dummy.uid?.let { contactDAO.getAll(0L, it) }!!

        // check contact exist
        contacts = contactDAO.getAll(0L)
        assertNotEquals(0, contacts.size)
    }

    /**
     * Test deleting contact
     */
    private fun testDelete() {
        // check contact doesn't exist
        var contacts = contactDAO.getAll(0L)
        assertNotEquals(0, contacts.size)

        // insert contact
        contactDAO.deleteContact(dummy)

        // check contact exist
        contacts = contactDAO.getAll(0L)
        assertEquals(0, contacts.size)
    }
}