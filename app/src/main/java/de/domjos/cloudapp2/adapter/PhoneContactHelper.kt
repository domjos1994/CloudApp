/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.CalendarContract.Events
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Photo
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.RawContacts
import de.domjos.cloudapp2.cardav.CarDav
import de.domjos.cloudapp2.cardav.model.AddressBook
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.AddressType
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.PhoneType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects
import java.util.UUID

class PhoneContactHelper(private val account: Account?, private val contentResolver: ContentResolver, private val db: DB) {
    private val authId: Long = this.db.authenticationDao().getSelectedItem()?.id ?: 0L
    private val contactUri = ContactsContract.Data.CONTENT_URI
    private var addressBooks: List<AddressBook>? = null

    private var phoneContacts: List<Contact>? = null
    private var appContacts: List<Contact>? = null

    init {
        // load addressBooks if empty
        if(this.addressBooks == null) {
            val carDav = CarDav(db.authenticationDao().getSelectedItem())
            this.addressBooks = carDav.getAddressBooks()
        }
    }

    fun sync() {
        insertLogMessage(this.db, "Start syncing contacts!", "calendars")

        this.updateByStatus()
    }

    private fun updateByStatus() {
        try {
            val phones = this.getData()
            val apps = this.db.contactDao().getAll(this.authId)

            // 0 insert phone item
            // 1 insert app item
            // 2 update phone item
            // 3 update app item
            // 4 delete phone item
            // 5 delete app item
            val states = mutableListOf<Array<Long>>()
            phones.forEach { item ->
                val find = apps.find { it.id==item[2] }
                if(find == null) {
                    if(item[2] > 0L) {
                        states.add(arrayOf(item[0], 4))
                    } else {
                        states.add(arrayOf(item[0], 1))
                    }
                } else {
                    if((find.lastUpdatedContactApp ?: 0L) > item[1]) {
                        states.add(arrayOf(find.id, 2))
                    } else if((find.lastUpdatedContactApp ?: 0L) < item[1]) {
                        states.add(arrayOf(item[0], 3))
                    } else {
                        states.add(arrayOf(item[0], 3))
                    }
                }
            }
            apps.forEach {item ->
                val find = phones.find { it[2]==item.id }
                if(find == null) {
                    if(item.contactId?.isNotEmpty() == true) {
                        states.add(arrayOf(item.id, 5))
                    } else {
                        states.add(arrayOf(item.id, 0))
                    }
                }
            }

            this.phoneContacts = this.getPhoneContacts()
            this.appContacts = this.getAppContacts()

            states.forEach {
                when(it[1]) {
                    0L -> this.insertPhoneItem(it[0])
                    1L -> this.insertAppItem(it[0])
                    2L -> this.updateAppItem(it[0])
                    3L -> this.updatePhoneItem(it[0])
                    4L -> this.deletePhoneItem(it[0])
                    5L -> this.deleteAppItem(it[0])
                }
            }
            this.contentResolver.notifyChange(Events.CONTENT_URI, null)
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem syncing data!")
        } finally {
            this.db.close()
        }
    }

    private fun getData() : MutableList<Array<Long>> {
        val items: MutableList<Array<Long>> = mutableListOf()
        try {
            val uri = asSyncAdapter(RawContacts.CONTENT_URI, this.account)
            val projection = arrayOf(RawContacts.SOURCE_ID, RawContacts.CONTACT_ID)
            val where = "${RawContacts.ACCOUNT_NAME}=? AND ${RawContacts.ACCOUNT_TYPE}=? AND ${RawContacts.DELETED}=0"
            val args = arrayOf(account?.name, account?.type)
            val cursor = this.contentResolver.query(uri, projection, where, args, null)
            cursor?.use { c ->
                while(c.moveToNext()) {
                    val contactId = getValue(c, RawContacts.CONTACT_ID, 0L) ?: 0L
                    val appId = (getValue(c, RawContacts.SOURCE_ID, "0") ?: "0").toLong()
                    items.add(arrayOf(contactId, getLastUpdateTimestampForContact(contactId), appId))
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem loading Base-Data!")
        }
        return items
    }

    private fun getAppContacts(): List<Contact> {
        return try {
            val phones = this.db.contactDao().getAllWithPhones(this.authId)
            val addresses = this.db.contactDao().getAllWithAddresses(this.authId)
            val emails = this.db.contactDao().getAllWithEmails(this.authId)

            val appContacts = this.db.contactDao().getAll(authId).filter { it.addressBook != "" }
            appContacts.map { contact ->
                contact.addresses = addresses.find { it.contact.id == contact.id }?.addresses?.toMutableList() ?: mutableListOf()
                contact.phoneNumbers = phones.find { it.contact.id == contact.id }?.phones?.toMutableList() ?: mutableListOf()
                contact.emailAddresses = emails.find { it.contact.id == contact.id }?.emails?.toMutableList() ?: mutableListOf()
            }
            appContacts
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching App-Contacts!")
            return listOf()
        }
    }

    private fun insertPhoneItem(id: Long) {
        val contact = this.appContacts?.find { it.id == id }
        if(contact != null) {
            contact.contactId = ""
            contact.lastUpdatedContactPhone = -1
            val phoneId = this.insertOrUpdatePhoneContact(contact)
            this.connectAppAndPhoneContacts(phoneId, id)
        }
    }

    private fun updatePhoneItem(id: Long) {
        val contact = this.appContacts?.find { it.id == id }
        if(contact != null) {
            val phoneId = this.insertOrUpdatePhoneContact(contact)
            this.connectAppAndPhoneContacts(phoneId, id)
        }
    }

    private fun insertAppItem(id: Long) {
        val contact = this.phoneContacts?.find { it.id == id }
        if(contact != null) {
            contact.id = 0L
            contact.uid = ""
            contact.lastUpdatedContactApp = -1
            contact.lastUpdatedContactServer = -1
            val appId = this.insertOrUpdateAppContact(contact)
            this.connectAppAndPhoneContacts(id, appId)
        }
    }

    private fun updateAppItem(id: Long) {
        val contact = this.phoneContacts?.find { it.id == id }
        if(contact != null) {
            val appId = this.insertOrUpdateAppContact(contact)
            this.connectAppAndPhoneContacts(id, appId)
        }
    }

    private fun deletePhoneItem(id: Long) {
        val dataUri = asSyncAdapter(this.contactUri, this.account)
        var where = "${StructuredName.RAW_CONTACT_ID}=?"
        val args = arrayOf("$id")
        this.contentResolver.delete(dataUri, where, args)
        val rawUri = asSyncAdapter(RawContacts.CONTENT_URI, this.account)
        where = "${RawContacts._ID}=?"
        this.contentResolver.delete(rawUri, where, args)
    }

    private fun deleteAppItem(id: Long) {
        this.db.contactDao().deleteContact(id)
    }

    private fun connectAppAndPhoneContacts(phoneContactId: Long, appContactId: Long) {
        try {
            val contacts = this.db.contactDao().getAll(this.authId)
            val find = contacts.find { it.id == appContactId }
            if(find != null) {
                find.lastUpdatedContactPhone = this.getLastUpdateTimestampForContact(phoneContactId)
                find.contactId = "$phoneContactId"
                this.db.contactDao().updateContact(find)
            }

            val values = ContentValues()
            values.put(RawContacts.ACCOUNT_TYPE, this.account?.type)
            values.put(RawContacts.ACCOUNT_NAME, this.account?.name)
            values.put(RawContacts.SOURCE_ID, "$phoneContactId")
            val where = "${RawContacts._ID}=?"
            val args = arrayOf("$appContactId")
            this.contentResolver.update(asSyncAdapter(RawContacts.CONTENT_URI, this.account), values, where, args)
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on connecting accounts $appContactId $phoneContactId")
        }
    }

    private fun getPhoneContacts(): List<Contact> {

        // load names and photo
        val contacts = mutableListOf<Contact>()
        val ids = mutableMapOf<Long, Long>()

        try {
            val uri = asSyncAdapter(ContactsContract.Contacts.CONTENT_URI, this.account)
            val projection = arrayOf(ContactsContract.Contacts.NAME_RAW_CONTACT_ID, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
            val cursor = this.contentResolver.query(uri, projection, null, null, null)
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val id = getValue(c, ContactsContract.Contacts.NAME_RAW_CONTACT_ID, 0L) ?: 0L
                    val timestamp = getValue(c, ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP, 0L) ?: 0L

                    ids[id] = timestamp
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem at fetching ids")
        }


        ids.forEach { (key, value) ->
            val contact = this.getBaseContact(key, value)
            if(contact != null) {
                contacts.add(contact)
            }
        }

        contacts.map { contact ->
            contact.organization = this.getOrganization(contact.contactId?.toLong() ?: 0L)
            contact.photo = this.getPhoto(contact.contactId?.toLong() ?: 0L)
            contact.birthDay = this.getBirthDay(contact.contactId?.toLong() ?: 0L)
            contact.addresses = this.getAddresses(contact.contactId?.toLong() ?: 0L)
            contact.phoneNumbers = this.getPhones(contact.contactId?.toLong() ?: 0L)
            contact.emailAddresses = this.getEmails(contact.contactId?.toLong() ?: 0L)
        }

        return contacts
    }

    private fun insertOrUpdateAppContact(contact: Contact): Long {
        contact.lastUpdatedContactApp = Date().time
        contact.addressBook = this.getAddressBook(contact.contactId?.toLong() ?: 0L)
        if(contact.uid == "") {
            contact.uid = UUID.randomUUID().toString()
        }
        if(contact.addressBook.isEmpty()) {
            insertLogMessage(this.db, "No Address-Book in Contact!", "contacts", contact)
            return 0L
        }
        var id = contact.id
        if(contact.id != 0L) {
            this.db.contactDao().updateContact(contact)
        } else {
            id = this.db.contactDao().insertContact(contact)
        }
        this.db.contactDao().deleteAddresses(contact.uid ?: "")
        this.db.contactDao().deletePhones(contact.uid ?: "")
        this.db.contactDao().deleteEmails(contact.uid ?: "")
        contact.addresses.forEach { address ->
            address.id = 0L
            address.contactId = contact.uid ?: ""
            this.db.contactDao().insertAddress(address)
        }
        contact.phoneNumbers.forEach { phone ->
            phone.id = 0L
            phone.contactId = contact.uid ?: ""
            this.db.contactDao().insertPhone(phone)
        }
        contact.emailAddresses.forEach { email ->
            email.id = 0L
            email.contactId = contact.uid ?: ""
            this.db.contactDao().insertEmail(email)
        }
        return id
    }

    private fun insertOrUpdatePhoneContact(contact: Contact): Long {

        // if contact-id is empty create a new contact
        val dataUri = asSyncAdapter(this.contactUri, this.account)
        val rawContactId = try {
            if(contact.contactId == "") {
                insertRawContact(contact.id)
            } else {
                val uri = asSyncAdapter(RawContacts.CONTENT_URI, this.account)
                val projection = arrayOf(RawContacts.CONTACT_ID)
                val selection = "${RawContacts._ID}=?"
                val args = arrayOf(contact.contactId)
                val cursor = this.contentResolver.query(uri, projection, selection, args, null)
                var exists = false
                cursor?.use { c -> if(c.moveToFirst()) exists = true }
                if(exists) {
                    contact.contactId!!.toLong()
                } else {
                    insertRawContact(contact.id)
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on getting rawContactId", "contacts", contact)
            return 0L
        }
        val timestamp = this.getLastUpdateTimestampForContact(rawContactId)
        val tmp = this.getBaseContact(rawContactId, timestamp)

        // insert or update structured name
        try {
            val baseValues = ContentValues()
            baseValues.put(StructuredName.SUFFIX, contact.suffix)
            baseValues.put(StructuredName.PREFIX, contact.prefix)
            baseValues.put(StructuredName.GIVEN_NAME, contact.givenName)
            baseValues.put(StructuredName.FAMILY_NAME, contact.familyName)
            baseValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
            baseValues.put(StructuredName.RAW_CONTACT_ID, rawContactId)
            val baseWhere = "${StructuredName.RAW_CONTACT_ID}=? AND ${StructuredName.MIMETYPE}=?"
            val baseClause = arrayOf("$rawContactId", StructuredName.CONTENT_ITEM_TYPE)
            if(tmp != null) {
                this.contentResolver.update(dataUri, baseValues, baseWhere, baseClause)
            } else {
                this.contentResolver.insert(dataUri, baseValues)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting name $rawContactId", "contacts", contact)
        }

        // insert, update or delete birthday
        try {
            val bdWhere = "${Event.RAW_CONTACT_ID}=? AND ${Event.MIMETYPE}=? AND ${Event.TYPE}=?"
            val bdClause = arrayOf("$rawContactId", Event.CONTENT_ITEM_TYPE, "${Event.TYPE_BIRTHDAY}")
            if(contact.birthDay != null) {
                val bd = this.getBirthDay(rawContactId)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val bdValues = ContentValues()
                bdValues.put(Event.START_DATE, sdf.format(contact.birthDay!!))
                bdValues.put(Event.TYPE, Event.TYPE_BIRTHDAY)
                bdValues.put(Event.MIMETYPE, Event.CONTENT_ITEM_TYPE)
                bdValues.put(Event.RAW_CONTACT_ID, rawContactId)
                if(bd != null) {
                    this.contentResolver.update(dataUri, bdValues, bdWhere, bdClause)
                } else {
                    this.contentResolver.insert(dataUri, bdValues)
                }
            } else {
                this.contentResolver.delete(dataUri, bdWhere, bdClause)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting birthday $rawContactId", "contacts", contact)
        }

        // insert, update, or delete photo
        try {
            val photoWhere = "${Photo.RAW_CONTACT_ID}=? AND ${Photo.MIMETYPE}=?"
            val photoClause = arrayOf("$rawContactId", Photo.CONTENT_ITEM_TYPE)
            if(contact.photo != null) {
                val photo = this.getPhoto(rawContactId)
                val photoValues = ContentValues()
                photoValues.put(Photo.PHOTO, contact.photo)
                photoValues.put(Photo.IS_SUPER_PRIMARY, 1)
                photoValues.put(Photo.MIMETYPE, Photo.CONTENT_ITEM_TYPE)
                photoValues.put(Photo.RAW_CONTACT_ID, rawContactId)
                if(photo != null) {
                    this.contentResolver.update(dataUri, photoValues, photoWhere, photoClause)
                } else {
                    this.contentResolver.insert(dataUri, photoValues)
                }
            } else {
                this.contentResolver.delete(dataUri, photoWhere, photoClause)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting photo $rawContactId", "contacts", contact)
        }

        // insert, update or delete organization
        try {
            val orgWhere = "${Organization.RAW_CONTACT_ID}=? AND ${Organization.MIMETYPE}=?"
            val orgClause = arrayOf("$rawContactId", Organization.CONTENT_ITEM_TYPE)
            if(contact.organization != null) {
                if(contact.organization?.isNotEmpty()!!) {
                    val organization = this.getOrganization(rawContactId)
                    val orgValues = ContentValues()
                    orgValues.put(Organization.COMPANY, contact.organization)
                    orgValues.put(Organization.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                    orgValues.put(Organization.RAW_CONTACT_ID, rawContactId)
                    if(organization.isNotEmpty()) {
                        this.contentResolver.update(dataUri, orgValues, orgWhere, orgClause)
                    } else {
                        this.contentResolver.insert(dataUri, orgValues)
                    }
                } else {
                    this.contentResolver.delete(dataUri, orgWhere, orgClause)
                }
            } else {
                this.contentResolver.delete(dataUri, orgWhere, orgClause)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting organization $rawContactId", "contacts", contact)
        }

        // insert, update or delete group
        try {
            val group = this.getAddressBook(rawContactId)
            val groupWhere = "${GroupMembership.RAW_CONTACT_ID}=? AND ${GroupMembership.MIMETYPE}=?"
            val groupClause = arrayOf("$rawContactId", GroupMembership.CONTENT_ITEM_TYPE)
            val groupValues = ContentValues()
            groupValues.put(GroupMembership.GROUP_ROW_ID, this.addOrGetGroup(contact.addressBook))
            groupValues.put(GroupMembership.RAW_CONTACT_ID, rawContactId)
            groupValues.put(GroupMembership.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE)
            if(group.isNotEmpty()) {
                this.contentResolver.update(dataUri, groupValues, groupWhere, groupClause)
            } else {
                this.contentResolver.insert(dataUri, groupValues)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting group $rawContactId", "contacts", contact)
        }

        var forWhere = "${StructuredPostal.RAW_CONTACT_ID}=? AND ${StructuredPostal.MIMETYPE}=?"
        var forClause = arrayOf("$rawContactId", StructuredPostal.CONTENT_ITEM_TYPE)
        try {
            this.contentResolver.delete(dataUri, forWhere, forClause)
            contact.addresses.forEach { address ->
                val forValues = ContentValues()
                forValues.put(StructuredPostal.POBOX, address.postOfficeAddress)
                forValues.put(StructuredPostal.STREET, address.street)
                forValues.put(StructuredPostal.CITY, address.locality)
                forValues.put(StructuredPostal.POSTCODE, address.postalCode)
                forValues.put(StructuredPostal.COUNTRY, address.country)
                forValues.put(StructuredPostal.REGION, address.region)
                if(address.types.isNotEmpty()) {
                    when(address.types[0]) {
                        AddressType.home-> forValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_HOME)
                        AddressType.work ->forValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK)
                        else -> {forValues.put(StructuredPostal.TYPE, StructuredPostal.TYPE_OTHER)}
                    }
                }
                forValues.put(StructuredPostal.RAW_CONTACT_ID, rawContactId)
                forValues.put(StructuredPostal.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
                this.contentResolver.insert(dataUri, forValues)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting address $rawContactId", "contacts", contact)
        }

        forWhere = "${Phone.RAW_CONTACT_ID}=? AND ${Phone.MIMETYPE}=?"
        forClause = arrayOf("$rawContactId", Phone.CONTENT_ITEM_TYPE)
        try {
            this.contentResolver.delete(dataUri, forWhere, forClause)
            contact.phoneNumbers.forEach { phone ->
                val forValues = ContentValues()
                forValues.put(Phone.NUMBER, phone.value)
                if(phone.types.isNotEmpty()) {
                    when(phone.types[0]) {
                        PhoneType.CELL -> forValues.put(Phone.TYPE, Phone.TYPE_MOBILE)
                        PhoneType.PREF -> forValues.put(Phone.TYPE, Phone.TYPE_CUSTOM)
                        PhoneType.WORK -> forValues.put(Phone.TYPE, Phone.TYPE_WORK)
                        PhoneType.HOME -> forValues.put(Phone.TYPE, Phone.TYPE_HOME)
                        PhoneType.VOICE -> forValues.put(Phone.TYPE, Phone.TYPE_WORK)
                        PhoneType.FAX -> forValues.put(Phone.TYPE, Phone.TYPE_FAX_HOME)
                        PhoneType.MSG -> forValues.put(Phone.TYPE, Phone.TYPE_MMS)
                    }
                }
                forValues.put(Phone.RAW_CONTACT_ID, rawContactId)
                forValues.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                this.contentResolver.insert(dataUri, forValues)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting phone $rawContactId", "contacts", contact)
        }

        forWhere = "${Email.RAW_CONTACT_ID}=? AND ${Email.MIMETYPE}=?"
        forClause = arrayOf("$rawContactId", Email.CONTENT_ITEM_TYPE)
        try {
            this.contentResolver.delete(dataUri, forWhere, forClause)
            contact.emailAddresses.forEach { email ->
                val forValues = ContentValues()
                forValues.put(Email.ADDRESS, email.value)
                forValues.put(Email.TYPE, Email.TYPE_HOME)
                forValues.put(Email.RAW_CONTACT_ID, rawContactId)
                forValues.put(Email.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                this.contentResolver.insert(dataUri, forValues)
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on inserting email-address $rawContactId", "contacts", contact)
        }

        return rawContactId
    }

    @Throws(java.lang.Exception::class)
    private fun addOrGetGroup(group: String): Long {
        val label = this.addressBooks?.find { it.name == group }?.label ?: group
        val lst = getContactLists()
        return if(lst.containsKey(label)) {
            lst[label]!!
        } else {
            val contentValues = ContentValues()
            contentValues.put(ContactsContract.Groups.ACCOUNT_NAME, account!!.name)
            contentValues.put(ContactsContract.Groups.ACCOUNT_TYPE, account.type)
            contentValues.put(ContactsContract.Groups.TITLE, label)
            contentValues.put(ContactsContract.Groups.GROUP_VISIBLE, 1)
            contentValues.put(ContactsContract.Groups.SHOULD_SYNC, true)
            val newGroupUri = this.contentResolver.insert(
                asSyncAdapter(ContactsContract.Groups.CONTENT_URI, this.account),
                contentValues
            )
            ContentUris.parseId(Objects.requireNonNull<Uri?>(newGroupUri))
        }
    }
    @Throws(java.lang.Exception::class)
    private fun getContactLists(): Map<String, Long> {
        val directoryMap: MutableMap<String, Long> = LinkedHashMap()
        val projection = arrayOf(ContactsContract.Groups._ID, ContactsContract.Groups.TITLE)
        val groupCursor =
            this.contentResolver.query(asSyncAdapter(ContactsContract.Groups.CONTENT_URI, this.account), projection, null, null, null)
        if (groupCursor != null) {
            while (groupCursor.moveToNext()) {
                directoryMap[groupCursor.getString(1)] = groupCursor.getLong(0)
            }
            groupCursor.close()
        }
        return directoryMap
    }

    private fun insertRawContact(id: Long): Long {
        val values = ContentValues()
        values.put(RawContacts.ACCOUNT_TYPE, this.account?.type)
        values.put(RawContacts.ACCOUNT_NAME, this.account?.name)
        values.put(RawContacts.SOURCE_ID, "$id")
        val uri2 = this.contentResolver.insert(asSyncAdapter(RawContacts.CONTENT_URI, this.account), values)
        val cid = ContentUris.parseId(uri2!!)
        return cid
    }

    private fun getBaseContact(id: Long, timestamp: Long): Contact? {
        var contact: Contact? = null
        try {
            val uri = asSyncAdapter(this.contactUri, this.account)
            val clause = "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${StructuredName.MIMETYPE}=?"
            val args = arrayOf("$id", StructuredName.CONTENT_ITEM_TYPE)
            val projection = arrayOf(
                StructuredName.SUFFIX, StructuredName.GIVEN_NAME,
                StructuredName.FAMILY_NAME, StructuredName.PREFIX
            )
            val cursor = this.contentResolver.query(uri, projection, clause, args, null)
            cursor?.use { c ->
                while (c.moveToNext()) {
                    try {
                        val suffix = getValue(c, StructuredName.SUFFIX, "") ?: ""
                        val prefix = getValue(c, StructuredName.PREFIX, "") ?: ""
                        val given = getValue(c, StructuredName.GIVEN_NAME, "")
                        val family = getValue(c, StructuredName.FAMILY_NAME, "") ?: ""

                        if(given != null) {
                            contact = Contact(
                                suffix = suffix,
                                prefix = prefix,
                                givenName = given,
                                familyName = family,
                                authId = this.authId,
                                addressBook = this.getAddressBook(id),
                                contactId = "$id",
                                lastUpdatedContactPhone = timestamp
                            )
                        }
                    } catch (ex: Exception) {
                        insertLogException(this.db, ex, "contacts", "Problem on fetching Name ${c.position}", "contacts", contact)
                    }
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Name", "contacts", contact)
        }
        return contact
    }

    private fun getEmails(contactId: Long): MutableList<de.domjos.cloudapp2.database.model.contacts.Email> {
        val emailAddresses = mutableListOf<de.domjos.cloudapp2.database.model.contacts.Email>()

        try {
            val uri = asSyncAdapter(this.contactUri, this.account)
            val selectionClause = "${Email.RAW_CONTACT_ID}=? AND ${Email.MIMETYPE}=?"
            val selectionArgs = arrayOf(contactId.toString(), Email.CONTENT_ITEM_TYPE)
            val projection = arrayOf(
                Email.ADDRESS
            )
            val cursor = contentResolver.query(uri, projection, selectionClause, selectionArgs, null)
            cursor?.use { c ->
                while (c.moveToNext()) {
                    try {
                        val address = getValue(c, Email.ADDRESS, "") ?: ""
                        emailAddresses.add(
                            de.domjos.cloudapp2.database.model.contacts.Email(value = address)
                        )
                    } catch (ex: Exception) {
                        insertLogException(this.db, ex, "contacts", "Problem on fetching Email-Address ${c.position}")
                    }
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Email-Address")
        }

        return emailAddresses
    }

    private fun getPhones(contactId: Long): MutableList<de.domjos.cloudapp2.database.model.contacts.Phone> {
        val phoneNumbers = mutableListOf<de.domjos.cloudapp2.database.model.contacts.Phone>()

        try {
            val uri = asSyncAdapter(this.contactUri, this.account)
            val selectionClause = "${Phone.RAW_CONTACT_ID}=? AND ${Phone.MIMETYPE}=?"
            val selectionArgs = arrayOf(contactId.toString(), Phone.CONTENT_ITEM_TYPE)
            val projection = arrayOf(
                Phone.NUMBER, Phone.TYPE
            )
            val cursor = contentResolver.query(uri, projection, selectionClause, selectionArgs, null)
            cursor?.use { c ->
                while (c.moveToNext()) {
                    try {
                        val number = getValue(c, Phone.NUMBER, "") ?: ""
                        val type = getValue(c, Phone.TYPE, Phone.TYPE_OTHER) ?: Phone.TYPE_OTHER
                        val types = mutableListOf<PhoneType>()
                        when(type) {
                            Phone.TYPE_MOBILE -> types.add(PhoneType.CELL)
                            Phone.TYPE_MAIN -> types.add(PhoneType.PREF)
                            Phone.TYPE_WORK -> types.add(PhoneType.WORK)
                            Phone.TYPE_HOME -> types.add(PhoneType.HOME)
                            Phone.TYPE_OTHER -> types.add(PhoneType.VOICE)
                            Phone.TYPE_FAX_HOME -> types.add(PhoneType.FAX)
                            Phone.TYPE_MMS -> types.add(PhoneType.MSG)
                        }

                        phoneNumbers.add(
                            de.domjos.cloudapp2.database.model.contacts.Phone(
                                value = number,
                                types = types
                            )
                        )
                    } catch (ex: Exception) {
                        insertLogException(this.db, ex, "contacts", "Problem on fetching Phone-Number ${c.position}")
                    }
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Phone-Number")
        }

        return phoneNumbers
    }

    private fun getAddresses(contactId: Long): MutableList<Address> {
        val addresses = mutableListOf<Address>()

        try {
            val uri = asSyncAdapter(this.contactUri, this.account)
            val selectionClause = "${StructuredPostal.RAW_CONTACT_ID}=? AND ${StructuredPostal.MIMETYPE}=?"
            val selectionArgs = arrayOf(contactId.toString(), StructuredPostal.CONTENT_ITEM_TYPE)
            val projection = arrayOf(
                StructuredPostal.POBOX, StructuredPostal.STREET, StructuredPostal.CITY,
                StructuredPostal.REGION, StructuredPostal.POSTCODE, StructuredPostal.COUNTRY
            )
            val cursor = contentResolver.query(uri, projection, selectionClause, selectionArgs, null)
            cursor?.use { c ->
                while (c.moveToNext()) {
                    try {
                        val poBox = getValue(c, StructuredPostal.POBOX, "") ?: ""
                        val street = getValue(c, StructuredPostal.STREET, "") ?: ""
                        val city = getValue(c, StructuredPostal.CITY, "") ?: ""
                        val region = getValue(c, StructuredPostal.REGION, "") ?: ""
                        val postCode = getValue(c, StructuredPostal.POSTCODE, "") ?: ""
                        val country = getValue(c, StructuredPostal.COUNTRY, "") ?: ""
                        val type = getValue(c, StructuredPostal.TYPE, "other") ?: "other"
                        val types = mutableListOf<AddressType>()
                        when(type) {
                            "home" -> types.add(AddressType.home)
                            "work" -> types.add(AddressType.work)
                            "other" -> types.add(AddressType.postal)
                        }

                        addresses.add(
                            Address(
                                postOfficeAddress = poBox,
                                street = street,
                                locality = city,
                                region = region,
                                postalCode = postCode,
                                country = country,
                                types = types
                            )
                        )
                    } catch (ex: Exception) {
                        insertLogException(this.db, ex, "contacts", "Problem on fetching Address ${c.position}")
                    }
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Address")
        }

        return addresses
    }

    private fun getOrganization(contactId: Long): String {
        var result = ""

        try {
            val uri = asSyncAdapter(this.contactUri, this.account)
            val projection = arrayOf(Organization.COMPANY)
            val selectionClause = "${Organization.RAW_CONTACT_ID}=? AND ${Organization.MIMETYPE}=?"
            val selectionArgs = arrayOf("$contactId", Organization.CONTENT_ITEM_TYPE)
            val cursor = this.contentResolver.query(uri, projection, selectionClause, selectionArgs, null)
            cursor?.use { c ->
                if(c.moveToFirst()) {
                    result = getValue(c, Organization.COMPANY, "") ?: ""
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Organization")
        }

        return result
    }

    private fun getBirthDay(contactId: Long): Date? {
        var date: Date? = null

        try {
            val uri = asSyncAdapter(this.contactUri, this.account)
            val selectionClause = "${Event.RAW_CONTACT_ID}=? AND ${Event.TYPE}=? AND ${Event.MIMETYPE}=?"
            val selectionArgs = arrayOf("$contactId", "${Event.TYPE_BIRTHDAY}", Event.CONTENT_ITEM_TYPE)
            val projection = arrayOf(Event.START_DATE)

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cursor = contentResolver.query(uri, projection, selectionClause, selectionArgs, null)
            cursor?.use { c ->
                if(c.moveToFirst()) {
                    date = sdf.parse(getValue(c, Event.START_DATE, "") ?: "")
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Birthday")
        }

        return date
    }

    private fun getAddressBook(id: Long): String {
        var result = ""
        try {
            // get address book from contact id
            var uri = asSyncAdapter(this.contactUri, this.account)
            var clause = "${GroupMembership.RAW_CONTACT_ID}=? AND ${GroupMembership.MIMETYPE}=?"
            var args = arrayOf("$id", GroupMembership.CONTENT_ITEM_TYPE)
            var projection = arrayOf(GroupMembership.GROUP_ROW_ID)

            val cursor = this.contentResolver.query(uri, projection, clause, args, null)
            cursor?.use { c ->
                if(c.moveToFirst()) {
                    val groupId = getValue(c, GroupMembership.GROUP_ROW_ID, 0L) ?: 0L
                    uri = asSyncAdapter(ContactsContract.Groups.CONTENT_URI, this.account)
                    clause = "${ContactsContract.Groups._ID}=?"
                    args = arrayOf("$groupId")
                    projection = arrayOf(ContactsContract.Groups.TITLE)
                    val c2 = this.contentResolver.query(uri, projection, clause, args, null)
                    c2?.use { ct ->
                        if(ct.moveToFirst()) {
                            result = getValue(ct, ContactsContract.Groups.TITLE, "") ?: ""
                        }
                    }
                }
            }

            return this.addressBooks?.find { it.label == result }?.name ?: ""
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Address-Book")
        }

        return result
    }

    private fun getPhoto(id: Long): ByteArray? {
        var result: ByteArray? = null

        try {
            val uri = asSyncAdapter(this.contactUri, this.account)
            val clause = "${Photo.RAW_CONTACT_ID}=? AND ${Photo.MIMETYPE}=?"
            val args = arrayOf("$id", Photo.CONTENT_ITEM_TYPE)
            val projection = arrayOf(Photo.PHOTO)

            val cursor = this.contentResolver.query(uri, projection, clause, args, null)
            cursor?.use { c ->
                if(c.moveToFirst()) {
                    result = getValue(c, Photo.PHOTO, ByteArray(0)) ?: ByteArray(0)
                }
            }
        } catch (ex: Exception) {
            insertLogException(this.db, ex, "contacts", "Problem on fetching Photo")
        }

        return result
    }

    @Throws(java.lang.Exception::class)
    private fun getLastUpdateTimestampForContact(contactId: Long): Long {
        var lastUpdateTimeStamp: Long = 0
        try {
            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
            val projection = arrayOf(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
            val cursor = contentResolver.query(contactUri, projection, null, null, null)
            cursor?.use {
                if(it.moveToFirst()) {
                    val index = cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
                    if (index != -1) {
                        lastUpdateTimeStamp = it.getLong(index)
                    }
                }
            }
        } catch (ex: Exception) {
            println(ex)
        }
        return lastUpdateTimeStamp
    }
}