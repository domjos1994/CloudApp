/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp2.database.model.todo.ToDoItem

@Dao
interface ToDoItemDAO {

    @Query("SELECT * FROM todoItems WHERE authId=:authId")
    fun getAll(authId: Long): List<ToDoItem>

    @Query("SELECT list_uid, list_name, list_color FROM todoItems WHERE authId=:authId GROUP BY list_uid, list_name, list_color")
    fun getLists(authId: Long): List<ListTuple>

    @Query("SELECT * FROM todoItems WHERE authId=:authId and list_uid=:listUid")
    fun getItemsOfList(authId: Long, listUid: String): List<ToDoItem>

    @Insert
    fun insertToDoItem(toDoItem: ToDoItem): Long

    @Update
    fun updateToDoItem(toDoItem: ToDoItem)

    @Delete
    fun deleteToDoItem(toDoItem: ToDoItem)
}

data class ListTuple(
    @ColumnInfo(name = "list_uid")
    val uid: String?,
    @ColumnInfo(name = "list_name")
    val name: String?,
    @ColumnInfo(name = "list_color")
    val color: String?
)
