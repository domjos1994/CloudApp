/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder
import de.domjos.cloudapp2.appbasics.R
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.VCardVersion
import ezvcard.property.Address
import ezvcard.property.Birthday
import ezvcard.property.Categories
import ezvcard.property.Email
import ezvcard.property.Organization
import ezvcard.property.StructuredName
import ezvcard.property.Telephone

class VCFExportBuilder(private val context: Context): BaseExportBuilder(context)  {
    override suspend fun exportNotifications(): String {
        return this.path
    }

    override suspend fun exportData(): String {
        return this.path
    }

    override suspend fun exportNotes(): String {
        return this.path
    }

    override suspend fun exportCalendars(): String {
        return this.path
    }

    override suspend fun exportContacts(): String {
        update(context.getString(R.string.export_fetch))

        val contacts = super.contactDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        val vCards = contacts.map { contact ->
            val vcard = VCard()
            vcard.structuredName = StructuredName()
            vcard.structuredName.family = contact.familyName
            vcard.structuredName.given = contact.givenName
            vcard.structuredName.prefixes.add(contact.prefix)
            vcard.structuredName.suffixes.add(contact.suffix)
            vcard.organization = Organization()
            vcard.organization.values.add(contact.organization ?: "")
            if(contact.birthDay != null) {
                vcard.birthday = Birthday(contact.birthDay?.toInstant())
            }
            vcard.categories = Categories()
            contact.categories.forEach { cat -> vcard.categories.values.add(cat) }
            contact.addresses.forEach { address ->
                val vAddress = Address()
                vAddress.extendedAddresses.add(address.extendedAddress)
                vAddress.streetAddress = address.street
                vAddress.locality = address.locality
                vAddress.postalCode = address.postalCode
                vAddress.country = address.country
                vcard.addresses.add(vAddress)
            }
            contact.phoneNumbers.forEach { phone ->
                val vPhone = Telephone(phone.value)
                vcard.telephoneNumbers.add(vPhone)
            }
            contact.emailAddresses.forEach { email ->
                val vEmail = Email(email.value)
                vcard.emails.add(vEmail)
            }
            vcard
        }
        val content = Ezvcard.write(vCards)
            .version(VCardVersion.V4_0).go()

        super.writeFile(content)
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportToDos(): String {
        return this.path
    }

    override suspend fun exportChats(): String {
        return this.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.contacts)
    }

    override fun getExtension(): List<String> {
        return listOf("vcf")
    }
}