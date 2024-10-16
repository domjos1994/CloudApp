package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.SyncResult
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.RawContacts
import android.util.Log
import de.domjos.cloudapp2.cardav.CarDav
import de.domjos.cloudapp2.cardav.model.AddressBook
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.database.model.contacts.Address
import de.domjos.cloudapp2.database.model.contacts.AddressType
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.database.model.contacts.PhoneType
import java.util.Date
import java.util.LinkedList
import java.util.Objects


class ContactSyncAdapter @JvmOverloads constructor(
    private val context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    private val contentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private var account: Account? = null
    private var addressBooks: List<AddressBook>? = null

    private fun getGroupNames(db: DB) {
        if(addressBooks == null) {
            val carDav = CarDav(db.authenticationDao().getSelectedItem())
            addressBooks = carDav.getAddressBooks()
        }
    }

    override fun onPerformSync(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        provider: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        this.account = account
        val db = DB.newInstance(this.context)
        this.getGroupNames(db)
        val id = db.authenticationDao().getSelectedItem()?.id

        if(id != null) {
            val phones = db.contactDao().getAllWithPhones(id)
            val addresses = db.contactDao().getAllWithAddresses(id)
            val emails = db.contactDao().getAllWithEmails(id)
            db.contactDao().getAll(id).forEach       { contact ->
                val groupId = this.addOrGetGroup(account, contact.addressBook, provider!!)
                val phoneItems = LinkedList<de.domjos.cloudapp2.database.model.contacts.Phone>()
                val emailItems = LinkedList<de.domjos.cloudapp2.database.model.contacts.Email>()
                val addressItems = LinkedList<Address>()
                phones.map { item -> if(item.contact.id==contact.id) phoneItems.addAll(item.phones.toList()) }
                addresses.map { item -> if(item.contact.id==contact.id) addressItems.addAll(item.addresses.toList()) }
                emails.map { item -> if(item.contact.id==contact.id) emailItems.addAll(item.emails.toList()) }

                try {
                    var insert = false
                    var lastUpdated = -1L
                    val contactId = if(contact.contactId == "") {
                        val values = ContentValues()
                        values.put(RawContacts.ACCOUNT_TYPE, this.account?.type)
                        values.put(RawContacts.ACCOUNT_NAME, this.account?.name)
                        val uri = provider.insert(asSyncAdapter(RawContacts.CONTENT_URI), values)
                        insert = true
                        val cid = ContentUris.parseId(uri!!)
                        lastUpdated = this.getLastUpdateTimestampForContact(contentResolver, cid)
                        cid
                    } else {
                        contact.contactId!!.toLong()
                    }

                    // create content-values from the name
                    val nameValues = this.nameToValues(contact, contactId)
                    if(insert) {
                        provider.insert(asSyncAdapter(ContactsContract.Data.CONTENT_URI), nameValues)
                    } else {
                        val selectionClause = "${StructuredName.GIVEN_NAME}=? and ${StructuredName.FAMILY_NAME}=?"
                        val selectionArgs = arrayOf(contact.givenName, contact.familyName ?: "")
                        provider.update(ContactsContract.Data.CONTENT_URI, nameValues, selectionClause, selectionArgs)
                    }

                    val values = ContentValues()
                    values.put(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, groupId)
                    values.put(ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID, contactId)
                    values.put(ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
                    if(insert) {
                        provider.insert(asSyncAdapter(ContactsContract.Data.CONTENT_URI), values)
                    } else {
                        if(lastUpdated>contact.lastUpdatedContactPhone!!) {
                            // ToDo
                            println()
                        } else {
                            val uri = Uri.withAppendedPath(
                                ContactsContract.Data.CONTENT_URI,
                                "$contactId"
                            )
                            provider.delete(
                                uri,
                                "${ContactsContract.CommonDataKinds.GroupMembership.RAW_CONTACT_ID}=?",
                                arrayOf("$contactId")
                            )
                            provider.insert(
                                asSyncAdapter(ContactsContract.Data.CONTENT_URI),
                                values
                            )
                        }
                    }

                    if(contact.photo != null) {
                        values.clear()
                        values.put(ContactsContract.CommonDataKinds.Photo.RAW_CONTACT_ID, contactId)
                        values.put(ContactsContract.CommonDataKinds.Photo.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        values.put(ContactsContract.CommonDataKinds.Photo.IS_SUPER_PRIMARY, 1)
                        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, contact.photo)
                        val uri = Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, "$contactId")
                        if(lastUpdated>contact.lastUpdatedContactPhone!!) {
                            // ToDo
                            println()
                        } else {
                            provider.delete(
                                uri,
                                "${ContactsContract.CommonDataKinds.Photo.RAW_CONTACT_ID}=?",
                                arrayOf("$contactId")
                            )
                            provider.insert(
                                asSyncAdapter(ContactsContract.Data.CONTENT_URI),
                                values
                            )
                        }
                    }

                    phoneItems.forEach { phone ->
                        values.clear()
                        values.put(Phone.RAW_CONTACT_ID, contactId)
                        values.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        values.put(Phone.NUMBER, phone.value)
                        if(phone.types.isNotEmpty()) {
                            when(phone.types[0]) {
                                PhoneType.CELL -> values.put(Phone.TYPE, "mobile")
                                PhoneType.PREF -> values.put(Phone.TYPE, "main")
                                PhoneType.WORK -> values.put(Phone.TYPE, "work")
                                PhoneType.HOME -> values.put(Phone.TYPE, "home")
                                PhoneType.VOICE -> values.put(Phone.TYPE, "other")
                                PhoneType.FAX -> values.put(Phone.TYPE, "fax_home")
                                PhoneType.MSG -> values.put(Phone.TYPE, "mms")
                            }
                        }
                        if(insert) {
                            provider.insert(asSyncAdapter(ContactsContract.Data.CONTENT_URI), values)
                        } else {
                            if(lastUpdated>contact.lastUpdatedContactPhone!!) {
                                // todo
                                println()
                            } else {
                                val selectionClause = "${Phone.NUMBER}=?"
                                val selectionArgs = arrayOf(phone.value)
                                provider.update(ContactsContract.Data.CONTENT_URI, values, selectionClause, selectionArgs)
                            }
                        }
                    }

                    emailItems.forEach { email ->
                        values.clear()
                        values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        values.put(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                        values.put(Email.ADDRESS, email.value)
                        values.put(Email.TYPE, "home")
                        if(insert) {
                            provider.insert(asSyncAdapter(ContactsContract.Data.CONTENT_URI), values)
                        } else {
                            val selectionClause = "${Email.ADDRESS}=?"
                            val selectionArgs = arrayOf(email.value)
                            provider.update(ContactsContract.Data.CONTENT_URI, values, selectionClause, selectionArgs)
                        }
                    }

                    addressItems.forEach { address ->
                        values.clear()
                        values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        values.put(ContactsContract.Data.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE)
                        values.put(StructuredPostal.POBOX, address.postOfficeAddress)
                        values.put(StructuredPostal.STREET, address.street)
                        values.put(StructuredPostal.CITY, address.locality)
                        values.put(StructuredPostal.REGION, address.region)
                        values.put(StructuredPostal.POSTCODE, address.postalCode)
                        values.put(StructuredPostal.COUNTRY, address.country)
                        if(address.types.isNotEmpty()) {
                            when(address.types[0]) {
                                AddressType.home-> values.put(StructuredPostal.TYPE, "home")
                                AddressType.work ->values.put(StructuredPostal.TYPE, "work")
                                else -> {values.put(StructuredPostal.TYPE, "other")}
                            }
                        }
                        if(insert) {
                            provider.insert(asSyncAdapter(ContactsContract.Data.CONTENT_URI), values)
                        } else {
                            val selectionClause = "${StructuredPostal.CITY}=? and ${StructuredPostal.STREET}=?"
                            val selectionArgs = arrayOf(address.locality, address.street)
                            provider.update(ContactsContract.Data.CONTENT_URI, values, selectionClause, selectionArgs)
                        }
                    }
                    db.contactDao().updateContactSync("$contactId", Date().time, contact.id)
                } catch (e: Exception) {
                    Log.e(this.javaClass.name, e.message, e)
                }
            }
        }
    }

    @Throws(java.lang.Exception::class)
    private fun nameToValues(contact: Contact, contactId: Long): ContentValues {
        val values = ContentValues()
        values.put(StructuredName.SUFFIX, contact.suffix ?: "")
        values.put(StructuredName.GIVEN_NAME, contact.givenName)
        values.put(StructuredName.FAMILY_NAME, contact.familyName ?: "")
        values.put(StructuredName.PREFIX, contact.prefix ?: "")
        values.put(StructuredName.RAW_CONTACT_ID, contactId)
        values.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
        return values
    }

    @Throws(java.lang.Exception::class)
    private fun getLastUpdateTimestampForContact(contentResolver: ContentResolver, contactId: Long): Long {
        var lastUpdateTimeStamp: Long = 0
        try {
            val contactUri = ContactsContract.Contacts.CONTENT_URI
            val projection = arrayOf(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
            val selection = ContactsContract.Contacts._ID + "=?"
            val selectionArgs = arrayOf(contactId.toString())
            val cursor =
                contentResolver.query(contactUri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
                if(index != -1) {
                    lastUpdateTimeStamp = cursor.getLong(index)
                }
                cursor.close()
            }
        } catch (_: java.lang.Exception) {}
        return lastUpdateTimeStamp
    }

    @Throws(java.lang.Exception::class)
    private fun getContactLists(provider: ContentProviderClient): Map<String, Long> {
        val directoryMap: MutableMap<String, Long> = LinkedHashMap()
        val projection = arrayOf(ContactsContract.Groups._ID, ContactsContract.Groups.TITLE)
        val groupCursor =
            provider.query(asSyncAdapter(ContactsContract.Groups.CONTENT_URI), projection, null, null, null)
        if (groupCursor != null) {
            while (groupCursor.moveToNext()) {
                directoryMap[groupCursor.getString(1)] = groupCursor.getLong(0)
            }
            groupCursor.close()
        }
        return directoryMap
    }

    @Throws(java.lang.Exception::class)
    private fun addOrGetGroup(account: Account?, group: String, provider: ContentProviderClient): Long {
        val label = this.addressBooks?.find { it.name == group }?.label ?: group
        val lst = getContactLists(provider)
        return if(lst.containsKey(label)) {
            lst[label]!!
        } else {
            val contentValues = ContentValues()
            contentValues.put(ContactsContract.Groups.ACCOUNT_NAME, account!!.name)
            contentValues.put(ContactsContract.Groups.ACCOUNT_TYPE, account.type)
            contentValues.put(ContactsContract.Groups.TITLE, label)
            contentValues.put(ContactsContract.Groups.GROUP_VISIBLE, 1)
            contentValues.put(ContactsContract.Groups.SHOULD_SYNC, true)
            val newGroupUri = provider.insert(
                asSyncAdapter(ContactsContract.Groups.CONTENT_URI),
                contentValues
            )
            ContentUris.parseId(Objects.requireNonNull<Uri?>(newGroupUri))
        }
    }

    @Throws(java.lang.Exception::class)
    private fun asSyncAdapter(uri: Uri): Uri {
        return if(this.account != null) {
            uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(ContactsContract.PRIMARY_ACCOUNT_NAME, this.account?.name)
                .appendQueryParameter(ContactsContract.PRIMARY_ACCOUNT_TYPE, this.account?.type).build()
        } else {
            uri
        }
    }
}