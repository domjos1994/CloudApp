package de.domjos.cloudapp.database.model.calendar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "calendarEvents",
    indices = [
        Index(value = ["uid"], orders = [Index.Order.ASC], name = "ce_uid_index")
    ]
)
class CalendarEvent(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("id", defaultValue = "0") var id: Long,
    var uid: String = UUID.randomUUID().toString(),
    var from: Long, var to: Long, var title: String,
    var location: String, var description: String,
    var confirmation: String, var categories: String,
    var color: String, var calendar: String,
    @ColumnInfo("eventId", defaultValue = "") var eventId: String,
    @ColumnInfo("lastUpdatedEventPhone", defaultValue = "-1") var lastUpdatedEventPhone: Long,
    @ColumnInfo("lastUpdatedEventServer", defaultValue = "-1") var lastUpdatedEventServer: Long,
    @ColumnInfo("authId", defaultValue = "0") var authId: Long) {
}