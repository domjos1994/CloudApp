package de.domjos.cloudapp.database.converters

import androidx.room.TypeConverter
import de.domjos.cloudapp.database.model.contacts.AddressType
import java.util.LinkedList

class AddressTypeConverter {
    @TypeConverter
    fun fromString(value: String): LinkedList<AddressType> {
        val lst = LinkedList<AddressType>()
        try {
            value.split(",").forEach { type ->
                lst.add(AddressType.valueOf(type))
            }
        } catch (_:Exception) {}

        return lst
    }

    @TypeConverter
    fun addressTypeToString(types: LinkedList<AddressType>): String {
        val lst = LinkedList<String>()
        types.forEach { type ->
            lst.add(type.name)
        }
        return lst.joinToString(",")
    }
}