package de.domjos.cloudapp.database.converters

import androidx.room.TypeConverter
import de.domjos.cloudapp.database.model.contacts.AddressType
import de.domjos.cloudapp.database.model.contacts.PhoneType
import java.util.LinkedList

class PhoneTypeConverter {
    @TypeConverter
    fun fromString(value: String): LinkedList<PhoneType> {
        val lst = LinkedList<PhoneType>()
        try {
            value.split(",").forEach { type ->
                lst.add(PhoneType.valueOf(type))
            }
        } catch (_: Exception) {}

        return lst
    }

    @TypeConverter
    fun phoneTypeToString(types: LinkedList<PhoneType>): String {
        val lst = LinkedList<String>()
        return try {
            types.forEach { type ->
                lst.add(type.name)
            }
            lst.joinToString(",")
        } catch (_: Exception) {
            ""
        }
    }
}