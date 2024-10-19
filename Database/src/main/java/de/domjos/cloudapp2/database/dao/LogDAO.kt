/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.domjos.cloudapp2.database.model.Log
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDAO {

    @Query("SELECT * FROM logItems")
    fun getAll(): Flow<List<Log>>

    @Query("SELECT * FROM logItems WHERE itemType=:type")
    fun getItemsByItemType(type: String): Flow<List<Log>>

    @Query("SELECT * FROM logItems WHERE messageType=:type")
    fun getItemsByMessageType(type: String): Flow<List<Log>>

    @Insert
    fun insertItem(log: Log)

    @Query("DELETE FROM logItems")
    fun deleteAll()

    @Query("DELETE FROM logItems WHERE itemType=:type")
    fun deleteItemsByItemType(type: String)
}