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
    tableName = "phones",
    indices = [
        Index(value = ["id"], orders = [Index.Order.ASC], name = "contact_phone_id_index", unique = true)
    ]
)
data class Phone(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Long = 0L,
    @ColumnInfo("contactId", defaultValue = "")
    var contactId: String? = "",
    var value: String,
    var types: MutableList<PhoneType> = mutableListOf(PhoneType.HOME)
) {
    override fun toString(): String {
        return this.value
    }
}

enum class PhoneType {
    PREF,
    WORK,
    HOME,
    VOICE,
    FAX,
    MSG,
    CELL
}

data class ContactWithPhones(
    @Embedded val contact: Contact,
    @Relation(
        parentColumn = "uid",
        entityColumn = "contactId"
    ) val phones: List<Phone>
)