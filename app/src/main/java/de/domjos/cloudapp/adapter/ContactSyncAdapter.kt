package de.domjos.cloudapp.adapter

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.RawContacts
import android.util.Log
import de.domjos.cloudapp.R
import de.domjos.cloudapp.database.DB
import de.domjos.cloudapp.database.model.contacts.Address
import de.domjos.cloudapp.database.model.contacts.AddressType
import de.domjos.cloudapp.database.model.contacts.PhoneType
import java.util.Date
import java.util.LinkedList


class ContactSyncAdapter @JvmOverloads constructor(
    private val context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    private val contentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    override fun onPerformSync(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        provider: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        val db = DB.newInstance(this.context)
        val id = db.authenticationDao().getSelectedItem()?.id

        if(id != null) {
            val phones = db.contactDao().getAllWithPhones(id)
            val addresses = db.contactDao().getAllWithAddresses(id)
            val emails = db.contactDao().getAllWithEmails(id)
            db.contactDao().getAll(id).forEach { contact ->
                val phoneItems = LinkedList<de.domjos.cloudapp.database.model.contacts.Phone>()
                val emailItems = LinkedList<de.domjos.cloudapp.database.model.contacts.Email>()
                val addressItems = LinkedList<Address>()
                phones.map { item -> if(item.contact.id==contact.id) phoneItems.addAll(item.phones.toList()) }
                addresses.map { item -> if(item.contact.id==contact.id) addressItems.addAll(item.addresses.toList()) }
                emails.map { item -> if(item.contact.id==contact.id) emailItems.addAll(item.emails.toList()) }

                try {
                    var insert = false
                    val contactId = if(contact.contactId == "") {
                        val values = ContentValues()
                        values.put(RawContacts.ACCOUNT_TYPE, context.getString(R.string.sys_account_type))
                        values.put(RawContacts.ACCOUNT_NAME, context.getString(R.string.app_name))
                        val uri = this.contentResolver.insert(RawContacts.CONTENT_URI, values)
                        insert = true
                        ContentUris.parseId(uri!!)
                    } else {
                        contact.contactId.toLong()
                    }

                    val values = ContentValues()
                    values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                    values.put(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                    values.put(StructuredName.SUFFIX, contact.suffix)
                    values.put(StructuredName.GIVEN_NAME, contact.givenName)
                    values.put(StructuredName.FAMILY_NAME, contact.familyName)
                    values.put(StructuredName.PREFIX, contact.prefix)
                    if(insert) {
                        this.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
                    } else {
                        val selectionClause = "${StructuredName.GIVEN_NAME}=? and ${StructuredName.FAMILY_NAME}=?"
                        val selectionArgs = arrayOf(contact.givenName, contact.familyName)
                        this.contentResolver.update(ContactsContract.Data.CONTENT_URI, values, selectionClause, selectionArgs)
                    }

                    if(contact.photo != null) {
                        values.clear()
                        values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1)
                        values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, contact.photo)
                        this.contentResolver.delete(ContactsContract.Data.CONTENT_URI, "${ContactsContract.Data.RAW_CONTACT_ID}=?", arrayOf("$contactId"))
                        this.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
                    }

                    phoneItems.forEach { phone ->
                        values.clear()
                        values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        values.put(ContactsContract.Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
                        values.put(Phone.NUMBER, phone.value)
                        if(phone.types.size > 0) {
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
                            this.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
                        } else {
                            val selectionClause = "${Phone.NUMBER}=?"
                            val selectionArgs = arrayOf(phone.value)
                            this.contentResolver.update(ContactsContract.Data.CONTENT_URI, values, selectionClause, selectionArgs)
                        }
                    }

                    emailItems.forEach { email ->
                        values.clear()
                        values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId)
                        values.put(ContactsContract.Data.MIMETYPE, Email.CONTENT_ITEM_TYPE)
                        values.put(Email.ADDRESS, email.value)
                        values.put(Email.TYPE, "home")
                        if(insert) {
                            this.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
                        } else {
                            val selectionClause = "${Email.ADDRESS}=?"
                            val selectionArgs = arrayOf(email.value)
                            this.contentResolver.update(ContactsContract.Data.CONTENT_URI, values, selectionClause, selectionArgs)
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
                        if(address.types.size > 0) {
                            when(address.types[0]) {
                                AddressType.home-> values.put(StructuredPostal.TYPE, "home")
                                AddressType.work ->values.put(StructuredPostal.TYPE, "work")
                                else -> {values.put(StructuredPostal.TYPE, "other")}
                            }
                        }
                        if(insert) {
                            this.contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
                        } else {
                            val selectionClause = "${StructuredPostal.CITY}=? and ${StructuredPostal.STREET}=?"
                            val selectionArgs = arrayOf(address.locality, address.street)
                            this.contentResolver.update(ContactsContract.Data.CONTENT_URI, values, selectionClause, selectionArgs)
                        }
                    }
                    db.contactDao().updateContactSync("$contactId", Date().time, contact.id)
                } catch (e: Exception) {
                    Log.e("XYZ", "Something went wrong during creation! $e")
                    e.printStackTrace()
                }
            }
        }
    }
}