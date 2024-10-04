/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.model.todo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "todoItems",
    indices = [
        Index(value = ["uid"], orders = [Index.Order.ASC], name = "toi_uid_index")
    ]
)
data class ToDoItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo("id")
    var id: Long = 0L,
    @ColumnInfo("uid", defaultValue = "")
    var uid: String,
    @ColumnInfo("list_uid", defaultValue = "")
    var listUid: String,
    @ColumnInfo("list_name", defaultValue = "")
    var listName: String,
    @ColumnInfo("list_color", defaultValue = "")
    var listColor: String,
    @ColumnInfo("summary", defaultValue = "")
    var summary: String,
    @ColumnInfo("start", defaultValue = "0")
    var start: Date? = null,
    @ColumnInfo("end", defaultValue = "0")
    var end: Date? = null,
    @ColumnInfo("status", defaultValue = "IN-PROCESS")
    var status: Status = Status.IN_PROCESS,
    @ColumnInfo("completed", defaultValue = "0")
    var completed: Int = 0,
    @ColumnInfo("priority", defaultValue = "4")
    var priority: Int = 4,
    @ColumnInfo("location", defaultValue = "")
    var location: String? = "",
    @ColumnInfo("url", defaultValue = "")
    var url: String? = "",
    @ColumnInfo("categories", defaultValue = "")
    var categories: String? = "",
    @ColumnInfo("path", defaultValue = "")
    var path: String? = "",
    @ColumnInfo("authId", defaultValue = "0")
    var authId: Long = 0L
)

enum class Status {
    TENTATIVE,
    CONFIRMED,
    CANCELLED,
    NEEDS_ACTION,
    COMPLETED,
    IN_PROCESS,
    DRAFT,
    FINAL
}