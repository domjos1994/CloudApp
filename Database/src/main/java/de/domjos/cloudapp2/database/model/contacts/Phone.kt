package de.domjos.cloudapp2.database.model.contacts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.LinkedList

@Entity(
    tableName = "phones",
    indices = [
        Index(value = ["id"], orders = [Index.Order.ASC], name = "contact_phone_id_index", unique = true)
    ]
)
data class Phone(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var contactId: String,
    var value: String,
    var types: LinkedList<PhoneType>) {
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