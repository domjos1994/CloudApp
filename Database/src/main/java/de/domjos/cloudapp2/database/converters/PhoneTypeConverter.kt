/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.converters

import androidx.room.TypeConverter
import de.domjos.cloudapp2.database.model.contacts.PhoneType
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