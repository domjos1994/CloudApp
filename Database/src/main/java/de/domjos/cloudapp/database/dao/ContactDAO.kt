package de.domjos.cloudapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.domjos.cloudapp.database.model.contacts.Address
import de.domjos.cloudapp.database.model.contacts.Contact
import de.domjos.cloudapp.database.model.contacts.ContactWithAddresses
import de.domjos.cloudapp.database.model.contacts.ContactWithEmails
import de.domjos.cloudapp.database.model.contacts.ContactWithPhones
import de.domjos.cloudapp.database.model.contacts.Email
import de.domjos.cloudapp.database.model.contacts.Phone

@Dao
interface ContactDAO {

    @Query("SELECT * FROM contacts WHERE authId=:authId")
    fun getAll(authId: Long): List<Contact>

    @Query("SELECT * FROM contacts WHERE authId=:authId")
    @Transaction
    fun getAllWithAddresses(authId: Long): List<ContactWithAddresses>

    @Query("SELECT * FROM contacts WHERE authId=:authId")
    @Transaction
    fun getAllWithEmails(authId: Long): List<ContactWithEmails>

    @Query("SELECT * FROM contacts WHERE authId=:authId")
    @Transaction
    fun getAllWithPhones(authId: Long): List<ContactWithPhones>

    @Query("SELECT DISTINCT addressBook FROM contacts WHERE authId=:authId")
    fun getAddressBooks(authId: Long): List<String>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId")
    fun getAddressBook(addressBook: String, authId: Long): List<Contact>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId")
    @Transaction
    fun getAddressBookWithAddresses(addressBook: String, authId: Long): List<ContactWithAddresses>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId")
    @Transaction
    fun getAddressBookWithEmails(addressBook: String, authId: Long): List<ContactWithEmails>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook and authId=:authId")
    @Transaction
    fun getAddressBookWithPhones(addressBook: String, authId: Long): List<ContactWithPhones>

    @Insert
    fun insertContact(contact: Contact)

    @Update
    fun updateContact(contact: Contact)

    @Delete
    fun deleteContact(contact: Contact)

    @Query("DELETE FROM contacts")
    fun deleteAllContacts()

    @Insert
    fun insertAddress(address: Address): Long

    @Update
    fun updateAddress(address: Address)

    @Delete
    fun deleteAddress(address: Address)

    @Query("DELETE FROM addresses")
    fun deleteAllAddresses()

    @Insert
    fun insertEmail(email: Email): Long

    @Update
    fun updateEmail(email: Email)

    @Delete
    fun deleteEmail(email: Email)

    @Query("DELETE FROM emails")
    fun deleteAllEmails()

    @Insert
    fun insertPhone(phone: Phone): Long

    @Update
    fun updatePhone(phone: Phone)

    @Delete
    fun deletePhone(phone: Phone)

    @Query("DELETE FROM phones")
    fun deleteAllPhones()
}