/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.ContactWithAddresses
import de.domjos.cloudapp2.database.model.contacts.ContactWithEmails
import de.domjos.cloudapp2.database.model.contacts.ContactWithPhones
import de.domjos.cloudapp2.database.model.contacts.Email
import de.domjos.cloudapp2.database.model.contacts.Phone

/**
 * Getting, inserting, updating, deleting contacts
 * Getting:
 * @property getAll
 * @property getAddressBooks
 * @property getAddressBook
 * inserting:
 * @property insertContact
 * updating:
 * @property updateContactSync
 * @property updateContact
 * deleting:
 * @property deleteContact
 *
 * addresses:
 * @property getAllWithAddresses
 * @property getAddressBookWithAddresses
 * @property insertAddress
 * @property updateAddress
 * @property deleteAddresses
 *
 * phone:
 * @property getAllWithPhones
 * @property getAddressBookWithPhones
 * @property insertPhone
 * @property updatePhone
 * @property deletePhone
 *
 * email-addresses:
 * @property getAllWithEmails
 * @property getAddressBookWithEmails
 * @property insertEmail
 * @property updateEmail
 * @property deleteEmail
 */
@Dao
interface ContactDAO {

    /**
     * Get all contacts
     * @param authId id of authentication
     * @return list of contacts
     */
    @Query("SELECT * FROM contacts WHERE authId=:authId AND deleted=0")
    fun getAll(authId: Long): List<Contact>

    @Query("SELECT * FROM contacts WHERE authId=:authId AND deleted=1")
    fun getDeletedItems(authId: Long): List<Contact>

    /**
     * Get contact
     * @param authId id of authentication
     * @param uid id of contact
     * @return contact
     */
    @Query("SELECT * FROM contacts WHERE authId=:authId and uid=:uid AND deleted=0")
    fun getAll(authId: Long, uid: String): Contact?

    /**
     * Get all contacts with addresses
     * @param authId id of authentication
     * @return list of contacts with addresses
     */
    @Query("SELECT * FROM contacts WHERE authId=:authId AND deleted=0")
    @Transaction
    fun getAllWithAddresses(authId: Long): List<ContactWithAddresses>

    /**
     * Get all contacts with emails
     * @param authId id of authentication
     * @return list of contacts with emails
     */
    @Query("SELECT * FROM contacts WHERE authId=:authId AND deleted=0")
    @Transaction
    fun getAllWithEmails(authId: Long): List<ContactWithEmails>

    /**
     * Get all contacts with phones
     * @param authId id of authentication
     * @return list of contacts with phones
     */
    @Query("SELECT * FROM contacts WHERE authId=:authId AND deleted=0")
    @Transaction
    fun getAllWithPhones(authId: Long): List<ContactWithPhones>

    /**
     * Get all address-books
     * @param authId id of authentication
     * @return list of address-books
     */
    @Query("SELECT DISTINCT addressBook FROM contacts WHERE authId=:authId AND deleted=0")
    fun getAddressBooks(authId: Long): List<String>

    /**
     * Get all contacts
     * @param authId id of authentication
     * @param addressBook address-book
     * @return list of contacts
     */
    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId AND deleted=0")
    fun getAddressBook(addressBook: String, authId: Long): List<Contact>

    /**
     * Get all contacts and addresses
     * @param authId id of authentication
     * @param addressBook address-book
     * @return list of contacts and addresses
     */
    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId AND deleted=0")
    @Transaction
    fun getAddressBookWithAddresses(addressBook: String, authId: Long): List<ContactWithAddresses>

    /**
     * Get all contacts and emails
     * @param authId id of authentication
     * @param addressBook address-book
     * @return list of contacts and emails
     */
    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId AND deleted=0")
    @Transaction
    fun getAddressBookWithEmails(addressBook: String, authId: Long): List<ContactWithEmails>

    /**
     * Get all contacts and phones
     * @param authId id of authentication
     * @param addressBook address-book
     * @return list of contacts and phones
     */
    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId AND deleted=0")
    @Transaction
    fun getAddressBookWithPhones(addressBook: String, authId: Long): List<ContactWithPhones>

    /**
     * Update synced contacts
     * @param contactId id of contact
     * @param lastUpdated timestamp
     * @param id id
     */
    @Query("UPDATE contacts SET contactId=:contactId, lastUpdatedContactPhone=:lastUpdated WHERE id=:id AND deleted=0")
    fun updateContactSync(contactId: String, lastUpdated: Long, id: Long)

    /**
     * Insert a contact
     * @param contact the contact
     */
    @Insert
    fun insertContact(contact: Contact): Long

    /**
     * Update a contact
     * @param contact the contact
     */
    @Update
    fun updateContact(contact: Contact)

    /**
     * Delete a contact
     * @param contact the contact
     */
    @Delete
    fun deleteContact(contact: Contact)

    @Query("UPDATE contacts SET deleted=1 WHERE id=:id")
    fun deleteContact(id: Long)

    /**
     * Delete a contact by uid
     * @param uid the contact
     */
    @Query("UPDATE contacts SET deleted=1 WHERE uid=:uid")
    fun deleteContact(uid: String)

    /**
     * Insert an address
     * @param address the address
     */
    @Insert
    fun insertAddress(address: Address): Long

    /**
     * Update an address
     * @param address the address
     */
    @Update
    fun updateAddress(address: Address)

    /**
     * Delete an address
     * @param address the address
     */
    @Delete
    fun deleteAddress(address: Address)

    /**
     * Delete an address by uid
     * @param uid the id
     */
    @Query("DELETE FROM addresses where contactId=:uid")
    fun deleteAddresses(uid: String)

    /**
     * Insert an email
     * @param email the email
     */
    @Insert
    fun insertEmail(email: Email): Long

    /**
     * Update an email
     * @param email the email
     */
    @Update
    fun updateEmail(email: Email)

    /**
     * Delete an email
     * @param email the email
     */
    @Delete
    fun deleteEmail(email: Email)

    /**
     * Delete an email by uid
     * @param uid the id
     */
    @Query("DELETE FROM emails where contactId=:uid")
    fun deleteEmails(uid: String)

    /**
     * Insert a phone
     * @param phone the phone
     */
    @Insert
    fun insertPhone(phone: Phone): Long

    /**
     * Update a phone
     * @param phone the phone
     */
    @Update
    fun updatePhone(phone: Phone)

    /**
     * Delete a phone
     * @param phone the phone
     */
    @Delete
    fun deletePhone(phone: Phone)

    /**
     * Delete a phone by uid
     * @param uid the id
     */
    @Query("DELETE FROM phones where contactId=:uid")
    fun deletePhones(uid: String)
}