package de.domjos.cloudapp.database.model.contacts

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
    var id: Long,
    var contactId: String,
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