package de.domjos.cloudapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp.database.model.contacts.Address
import de.domjos.cloudapp.database.model.contacts.Contact
import de.domjos.cloudapp.database.model.contacts.ContactWithAddresses
import de.domjos.cloudapp.database.model.contacts.ContactWithEmails
import de.domjos.cloudapp.database.model.contacts.ContactWithPhones
import de.domjos.cloudapp.database.model.contacts.Email
import de.domjos.cloudapp.database.model.contacts.Phone
import kotlinx.coroutines.flow.Flow
import java.util.LinkedList

@Dao
interface ContactDAO {

    @Query("SELECT * FROM contacts")
    fun getAll(): List<Contact>

    @Query("SELECT * FROM contacts")
    fun getAllWithAddresses(): List<ContactWithAddresses>

    @Query("SELECT * FROM contacts")
    fun getAllWithEmails(): List<ContactWithEmails>

    @Query("SELECT * FROM contacts")
    fun getAllWithPhones(): List<ContactWithPhones>

    @Query("SELECT DISTINCT addressBook FROM contacts")
    fun getAddressBooks(): List<String>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook")
    fun getAddressBook(addressBook: String): List<Contact>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook")
    fun getAddressBookWithAddresses(addressBook: String): List<ContactWithAddresses>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook")
    fun getAddressBookWithEmails(addressBook: String): List<ContactWithEmails>

    @Query("SELECT * FROM contacts WHERE addressBook=:addressBook")
    fun getAddressBookWithPhones(addressBook: String): List<ContactWithPhones>

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