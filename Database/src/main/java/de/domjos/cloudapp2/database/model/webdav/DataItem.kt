/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.model.webdav

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "dataItems",
    indices = [
        Index(value = ["id"], orders = [Index.Order.ASC], name = "data_id_index", unique = true),
        Index(value = ["name"], orders = [Index.Order.ASC], name = "data_name_index", unique = false),
        Index(value = ["path"], orders = [Index.Order.ASC], name = "data_path_index", unique = false)
    ]
)
data class DataItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id", index = true, defaultValue = "0")
    var id: Long,
    @ColumnInfo(name = "name", index = true, defaultValue = "")
    var name: String,
    @ColumnInfo(name = "path", index = true, defaultValue = "")
    var path: String,
    @ColumnInfo(name = "type", defaultValue = "")
    var type: String,
    @ColumnInfo(name = "directory", defaultValue = "false")
    var directory: Boolean = false,
    @ColumnInfo(name = "exists", defaultValue = "false")
    var exists: Boolean = false,
    @ColumnInfo(name = "authId", defaultValue = "0")
    var authId: Long
)
