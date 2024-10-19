/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.model.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendarEvents",
    indices = [
        Index(value = ["uid"], orders = [Index.Order.ASC], name = "ce_uid_index")
    ]
)
data class CalendarEvent(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Long = 0L,
    @ColumnInfo("uid", defaultValue = "")
    var uid: String = "",
    @ColumnInfo("string_from", defaultValue = "")
    var string_from: String = "",
    @ColumnInfo("string_to", defaultValue = "")
    var string_to: String = "",
    var title: String,
    @ColumnInfo("location", defaultValue = "")
    var location: String = "",
    @ColumnInfo("description", defaultValue = "")
    var description: String = "",
    @ColumnInfo("confirmation", defaultValue = "")
    var confirmation: String = "",
    @ColumnInfo("categories", defaultValue = "")
    var categories: String = "",
    @ColumnInfo("color", defaultValue = "")
    var color: String = "",
    var calendar: String,
    @ColumnInfo("eventId", defaultValue = "")
    var eventId: String = "",
    @ColumnInfo("lastUpdatedEventPhone", defaultValue = "-1")
    var lastUpdatedEventPhone: Long = -1L,
    @ColumnInfo("lastUpdatedEventServer", defaultValue = "-1")
    var lastUpdatedEventServer: Long = -1L,
    @ColumnInfo("authId", defaultValue = "0")
    var authId: Long = 0L,
    @ColumnInfo("path", defaultValue = "")
    var path: String = "",
    @ColumnInfo("recurrence", defaultValue = "")
    var recurrence: String = "",
    @ColumnInfo("deleted", defaultValue = "0")
    var deleted: Int = 0) {

    @ColumnInfo("lastUpdatedEventApp", defaultValue = "-1")
    var lastUpdatedEventApp: Long? = -1
}