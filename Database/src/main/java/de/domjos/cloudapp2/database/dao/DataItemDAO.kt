/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp2.database.model.webdav.DataItem

@Dao
interface DataItemDAO {

    @Query("SELECT * FROM dataItems WHERE authId=:authId")
    fun getDataItems(authId: Long): List<DataItem>

    @Query("SELECT * FROM dataItems WHERE authId=:authId and path like :path")
    fun getDataItemsByPath(authId: Long, path: String): List<DataItem>

    @Query("DELETE FROM dataItems WHERE authId=:authId")
    fun deleteAll(authId: Long)

    @Insert
    fun insertDataItem(dataItem: DataItem)

    @Update
    fun updateDataItem(dataItem: DataItem)

    @Delete
    fun deleteDataItem(dataItem: DataItem)
}