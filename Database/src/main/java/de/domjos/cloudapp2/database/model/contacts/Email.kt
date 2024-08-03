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
    tableName = "emails",
    indices = [
        Index(value = ["id"], orders = [Index.Order.ASC], name = "contact_email_id_index", unique = true)
    ]
)
data class Email(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Long = 0L,
    @ColumnInfo("contactId", defaultValue = "")
    var contactId: String? = "",
    var value: String) {

    override fun toString(): String {
        return this.value
    }
}

data class ContactWithEmails(
    @Embedded val contact: Contact,
    @Relation(
        parentColumn = "uid",
        entityColumn = "contactId"
    ) val emails: List<Email>
)