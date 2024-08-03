/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.converters

import androidx.room.TypeConverter
import de.domjos.cloudapp2.database.model.contacts.AddressType
import java.util.LinkedList

class AddressTypeConverter {
    @TypeConverter
    fun fromString(value: String): MutableList<AddressType> {
        val lst = mutableListOf<AddressType>()
        try {
            value.split(",").forEach { type ->
                lst.add(AddressType.valueOf(type))
            }
        } catch (_:Exception) {}

        return lst
    }

    @TypeConverter
    fun addressTypeToString(types: MutableList<AddressType>): String {
        val lst = LinkedList<String>()
        types.forEach { type ->
            lst.add(type.name)
        }
        return lst.joinToString(",")
    }
}