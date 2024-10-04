/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.converters

import androidx.room.TypeConverter
import de.domjos.cloudapp2.database.model.todo.Status

class ToDoStatusConverter {
    @TypeConverter
    fun fromString(value: String): Status {
        return when(value) {
            "TENTATIVE" -> Status.TENTATIVE
            "CONFIRMED" -> Status.CONFIRMED
            "CANCELLED" -> Status.CANCELLED
            "NEEDS-ACTION" -> Status.NEEDS_ACTION
            "COMPLETED" -> Status.COMPLETED
            "IN-PROCESS" -> Status.IN_PROCESS
            "DRAFT" -> Status.DRAFT
            "FINAL" -> Status.FINAL
            else -> Status.IN_PROCESS
        }
    }

    @TypeConverter
    fun fromStatus(status: Status): String {
        return when(status) {
            Status.TENTATIVE -> "TENTATIVE"
            Status.CONFIRMED -> "CONFIRMED"
            Status.CANCELLED -> "CANCELLED"
            Status.NEEDS_ACTION -> "NEEDS-ACTION"
            Status.COMPLETED -> "COMPLETED"
            Status.IN_PROCESS -> "IN-PROCESS"
            Status.DRAFT -> "DRAFT"
            Status.FINAL -> "FINAL"
        }
    }
}