/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.model.contacts

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "addresses",
    indices = [
        Index(value = ["id"], orders = [Index.Order.ASC], name = "contact_address_id_index", unique = true)
    ]
)
data class Address(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Long = 0L,
    @ColumnInfo("contactId", defaultValue = "")
    var contactId: String? = "",
    val types: MutableList<AddressType> = mutableListOf(AddressType.home),
    @ColumnInfo("postOfficeAddress", defaultValue = "")
    var postOfficeAddress: String? = "",
    @ColumnInfo("extendedAddress", defaultValue = "")
    var extendedAddress: String? = "",
    var street: String,
    @ColumnInfo("locality", defaultValue = "")
    var locality: String? = "",
    var region: String? = "",
    @ColumnInfo("postalCode", defaultValue = "")
    var postalCode: String? = "",
    var country: String? = "")

enum class AddressType {
    domestic,
    international,
    postal,
    parcel,
    home,
    work
}

data class ContactWithAddresses(
    @Embedded val contact: Contact,
    @Relation(
        parentColumn = "uid",
        entityColumn = "contactId"
    ) val addresses: List<Address>
)