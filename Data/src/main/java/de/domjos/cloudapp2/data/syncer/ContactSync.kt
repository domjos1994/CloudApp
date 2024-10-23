/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.data.syncer


import de.domjos.cloudapp2.cardav.CarDav
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.model.contacts.Contact
import java.util.Date

class ContactSync(
    private val contactDAO: ContactDAO,
    private val authenticationDAO: AuthenticationDAO) {
    private val loader: CarDav
    private val authId: Long

    init {
        val auth = this.authenticationDAO.getSelectedItem()!!
        this.authId = auth.id
        this.loader = CarDav(auth)
    }

    fun sync(
        updateProgress: ((Float, String) -> Unit) = {_,_->},
        onFinish: (()->Unit) = {},
        loadingLabel: String = "",
        deleteLabel: String = "",
        insertLabel: String = "",
        updateLabel: String = "") {

        try {
            // loading data
            var current = 0.0f
            updateProgress(current, loadingLabel)
            var appContacts = this.contactDAO.getAll(this.authenticationDAO.getSelectedItem()?.id!!)
            appContacts = appContacts.map {
                if((it.uid ?: "").startsWith("http")) {
                    val lastPart = it.uid?.split("/")?.get(it.uid?.split("/")!!.size - 1)
                    it.uid = lastPart?.replace(".vcf", "")
                }
                it
            }
            val serverContacts = mutableListOf<Contact>()
            this.loader.getAddressBooks().forEach { addressBook ->
                serverContacts.addAll(this.loader.getContacts(addressBook))
            }
            val sizeApp = appContacts.size
            val sizeServer = serverContacts.size
            val factor = 100.0f / (sizeApp + sizeServer)

            appContacts.forEach { appContact ->
                val find = serverContacts.find { it.uid == appContact.uid || it.path == appContact.uid }

                if(find == null) {
                    if(appContact.lastUpdatedContactServer == -1L) {
                        try {
                            // new contact
                            val ts = Date().time
                            appContact.uid = this.loader.insertContact(appContact)
                            appContact.lastUpdatedContactServer = ts
                            contactDAO.updateContact(appContact)

                            current += factor
                            updateProgress(current, String.format(insertLabel, appContact.toString(), "Server"))
                        } catch (ex: Exception) {
                            current += factor
                            updateProgress(current, ex.message ?: "")
                        }
                    } else {
                        try {
                            contactDAO.deleteContact(appContact)

                            current += factor
                            updateProgress(current, String.format(deleteLabel, appContact.toString(), "App"))
                        } catch (ex: Exception) {
                            current += factor
                            updateProgress(current, ex.message ?: "")
                        }
                    }
                } else {
                    if(appContact.lastUpdatedContactApp != find.lastUpdatedContactServer) {
                        if((appContact.lastUpdatedContactApp ?: 0L) > (find.lastUpdatedContactServer ?: 0L)) {
                            try {
                                val ts = Date().time
                                appContact.path = find.path
                                appContact.lastUpdatedContactApp = ts
                                appContact.lastUpdatedContactServer = ts

                                this.loader.updateContact(appContact)
                                this.contactDAO.updateContact(appContact)

                                current += factor
                                updateProgress(current, String.format(updateLabel, appContact.toString(), "Server"))
                            } catch (ex: Exception) {
                                current += factor
                                updateProgress(current, ex.message ?: "")
                            }
                        } else {
                            try {
                                find.id = appContact.id
                                find.lastUpdatedContactApp = appContact.lastUpdatedContactServer
                                this.insertAppContact(find)

                                current += factor
                                updateProgress(current, String.format(insertLabel, find.toString(), "App"))
                            } catch (ex: Exception) {
                                current += factor
                                updateProgress(current, ex.message ?: "")
                            }
                        }
                    }
                }
            }

            val deletedContacts = contactDAO.getDeletedItems(this.authId)
            serverContacts.forEach { serverContact ->
                val find = appContacts.find { it.uid == serverContact.uid }
                val findDeleted = deletedContacts.find { it.uid == serverContact.uid }

                if(find == null && findDeleted == null) {
                    try {
                        serverContact.lastUpdatedContactApp = serverContact.lastUpdatedContactServer
                        this.insertAppContact(serverContact)

                        current += factor
                        updateProgress(current, String.format(updateLabel, serverContact.toString(), "App"))
                    } catch (ex: Exception) {
                        current += factor
                        updateProgress(current, ex.message ?: "")
                    }
                }
                if(findDeleted != null) {
                    try {
                        this.loader.deleteContact(serverContact)
                        this.contactDAO.deleteContact(findDeleted)

                        current += factor
                        updateProgress(current, String.format(deleteLabel, findDeleted.toString(), "Server"))
                    } catch (ex: Exception) {
                        current += factor
                        updateProgress(current, ex.message ?: "")
                    }
                }
            }
        } finally {
            onFinish()
        }
    }

    private fun insertAppContact(contact: Contact) {
        val uid = contact.uid!!
        try {
            if(this.authenticationDAO.getSelectedItem() != null) {
                val tmp = this.contactDAO.getAll(this.authenticationDAO.getSelectedItem()!!.id, uid)
                if(tmp != null) {
                    contact.contactId = tmp.contactId
                    contact.lastUpdatedContactPhone = tmp.lastUpdatedContactPhone
                }
            }
            contactDAO.deleteAddresses(uid)
            contactDAO.deleteEmails(uid)
            contactDAO.deletePhones(uid)
            contactDAO.deleteContact(uid)
        } catch (_: Exception) {}

        if(contact.id != 0L) {
            contactDAO.updateContact(contact)
        } else {
            contactDAO.insertContact(contact)
        }
        for(i in 0..<contact.addresses.size) {
            if(contact.addresses[i].street != "") {
                contact.addresses[i].contactId = uid
                contact.addresses[i].id = contactDAO.insertAddress(contact.addresses[i])
            }
        }
        for(i in 0..<contact.phoneNumbers.size) {
            contact.phoneNumbers[i].contactId = uid
            contact.phoneNumbers[i].id = contactDAO.insertPhone(contact.phoneNumbers[i])
        }
        for(i in 0..<contact.emailAddresses.size) {
            contact.emailAddresses[i].contactId = uid
            contact.emailAddresses[i].id = contactDAO.insertEmail(contact.emailAddresses[i])
        }
    }
}