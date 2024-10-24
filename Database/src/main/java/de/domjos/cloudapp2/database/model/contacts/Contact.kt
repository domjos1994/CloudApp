/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

@file:Suppress("unused")

package de.domjos.cloudapp2.database.model.contacts

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["uid"], orders = [Index.Order.ASC], name = "contact_uid_index", unique = false)
    ]
)
data class Contact(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Long = 0L,
    @ColumnInfo("uid", defaultValue = "")
    var uid: String? = "",
    @ColumnInfo("path", defaultValue = "")
    var path: String? = "",
    @ColumnInfo("suffix", defaultValue = "")
    var suffix: String? = "",
    @ColumnInfo("prefix", defaultValue = "")
    var prefix: String? = "",
    @ColumnInfo("familyName", defaultValue = "")
    var familyName: String? = "",
    var givenName: String,
    @ColumnInfo("additional", defaultValue = "")
    var additional: String? = "",
    @ColumnInfo("birthDay", defaultValue = "null")
    var birthDay: Date? = null,
    @ColumnInfo("organization", defaultValue = "")
    var organization: String? = "",
    @ColumnInfo("photo", defaultValue = "null")
    var photo: ByteArray? = null,
    var addressBook: String,
    @ColumnInfo("contactId", defaultValue = "")
    var contactId: String? = "",
    @ColumnInfo("lastUpdatedContactPhone", defaultValue = "-1")
    var lastUpdatedContactPhone: Long? = -1,
    @ColumnInfo("lastUpdatedContactServer", defaultValue = "-1")
    var lastUpdatedContactServer: Long? = -1,
    @ColumnInfo("authId", defaultValue = "0")
    var authId: Long,
    @ColumnInfo("deleted", defaultValue = "0")
    var deleted: Int = 0
) {
    @ColumnInfo("lastUpdatedContactApp", defaultValue = "-1")
    var lastUpdatedContactApp: Long? = -1
    @Ignore var addresses: MutableList<Address> = mutableListOf()
    @Ignore var phoneNumbers: MutableList<Phone> = mutableListOf()
    @Ignore var emailAddresses: MutableList<Email> = mutableListOf()
    @Ignore var categories: MutableList<String> = mutableListOf()
    @Ignore var contactToChat: Boolean = false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (uid != other.uid) return false
        if (path != other.path) return false
        if (suffix != other.suffix) return false
        if (prefix != other.prefix) return false
        if (familyName != other.familyName) return false
        if (givenName != other.givenName) return false
        if (additional != other.additional) return false
        if (birthDay != other.birthDay) return false
        if (categories != other.categories) return false
        if (organization != other.organization) return false
        if (addresses != other.addresses) return false
        if (phoneNumbers != other.phoneNumbers) return false
        if (emailAddresses != other.emailAddresses) return false
        if (!photo.contentEquals(other.photo)) return false
        if (deleted != other.deleted) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.hashCode()
        result = 31 * result + (path.hashCode())
        result = 31 * result + (suffix?.hashCode() ?: 0)
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + (familyName?.hashCode() ?: 0)
        result = 31 * result + givenName.hashCode()
        result = 31 * result + (additional?.hashCode() ?: 0)
        result = 31 * result + (birthDay?.hashCode() ?: 0)
        result = 31 * result + categories.hashCode()
        result = 31 * result + organization.hashCode()
        result = 31 * result + addresses.hashCode()
        result = 31 * result + phoneNumbers.hashCode()
        result = 31 * result + emailAddresses.hashCode()
        result = 31 * result + photo.contentHashCode()
        result = 31 * result + deleted.hashCode()
        return result
    }

    override fun toString(): String {
        return "$prefix $givenName $familyName $suffix".trim()
    }
}

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["id"], orders = [Index.Order.ASC], name = "contact_categories_id_index", unique = false)
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var contactId: String,
    var category: String
)

data class ContactWitCategories(
    @Embedded val contact: Contact,
    @Relation(
        parentColumn = "uid",
        entityColumn = "contactId"
    ) val categories: List<Category>
)