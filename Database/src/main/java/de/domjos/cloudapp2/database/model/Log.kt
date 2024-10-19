/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "logItems",
    indices = []
)
data class Log(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: Date,
    val itemType: String,
    val messageType: String,
    val message: String,
    val object1: String? = "",
    val object2: String? = ""
)