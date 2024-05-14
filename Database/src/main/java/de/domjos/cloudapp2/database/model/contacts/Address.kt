package de.domjos.cloudapp2.database.model.contacts

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.LinkedList

@Entity(
    tableName = "addresses",
    indices = [
        Index(value = ["id"], orders = [Index.Order.ASC], name = "contact_address_id_index", unique = true)
    ]
)
data class Address(
    @PrimaryKey(autoGenerate = true)
    var id: Long,
    var contactId: String,
    val types: LinkedList<AddressType>,
    var postOfficeAddress: String?,
    var extendedAddress: String?,
    var street: String,
    var locality: String?,
    var region: String?,
    var postalCode: String?,
    var country: String?) {
}

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