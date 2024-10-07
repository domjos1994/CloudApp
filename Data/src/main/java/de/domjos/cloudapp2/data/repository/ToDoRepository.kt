/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.caldav.ToDoCalDav
import de.domjos.cloudapp2.caldav.model.ToDoList
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.ListTuple
import de.domjos.cloudapp2.database.dao.ToDoItemDAO
import de.domjos.cloudapp2.database.model.todo.ToDoItem
import javax.inject.Inject

interface ToDoRepository {
    fun getLists(): List<ListTuple>
    fun updateList(listTuple: ListTuple)
    fun deleteList(listTuple: ListTuple)

    fun import(uid: String? = null, updateProgress: (Float, String) -> Unit, progressLabel: String)
    fun getToDoItems(uid: String? = null): List<ToDoItem>
    fun insertToDoItem(toDoItem: ToDoItem)
    fun updateToDoItem(toDoItem: ToDoItem)
    fun deleteToDoItem(toDoItem: ToDoItem)
    fun hasNoAuths(): Boolean
}

class DefaultToDoRepository @Inject constructor(
    authenticationDAO: AuthenticationDAO,
    private val toDoItemDAO: ToDoItemDAO
) : ToDoRepository {
    private var authId: Long = 0
    private var toDoCalDav: ToDoCalDav

    init {
        val auth = authenticationDAO.getSelectedItem()
        this.authId = auth?.id ?: 0
        this.toDoCalDav = ToDoCalDav(auth)
    }

    override fun hasNoAuths(): Boolean {
        return this.authId.toInt() == 0
    }

    override fun getLists(): List<ListTuple> {
        return this.toDoCalDav.getToDoLists().map { ListTuple(it.path, it.name, it.color) }
    }

    override fun updateList(listTuple: ListTuple) {
        val items = this.toDoItemDAO.getItemsOfList(this.authId, listTuple.uid!!).map {
            it.listName = listTuple.name!!
            it.listColor = listTuple.color!!
            it
        }
        items.forEach { item -> this.toDoItemDAO.updateToDoItem(item)}
        val list = ToDoList(listTuple.name!!, listTuple.color!!, listTuple.uid!!)
        if((listTuple.uid ?: "") != "") {
            this.toDoCalDav.updateToDoList(list)
        } else {
            this.toDoCalDav.insertToDoList(list)
        }
    }

    override fun deleteList(listTuple: ListTuple) {
        val items = this.toDoItemDAO.getItemsOfList(this.authId, listTuple.uid!!)
        items.forEach { item -> this.toDoItemDAO.deleteToDoItem(item)}
        val list = ToDoList(listTuple.name!!, listTuple.color!!, listTuple.uid!!)
        this.toDoCalDav.deleteToDoList(list)
    }

    override fun import(uid: String?, updateProgress: (Float, String) -> Unit, progressLabel: String) {
        if(uid == null) {
            this.toDoItemDAO.getAll(this.authId).forEach { this.toDoItemDAO.deleteToDoItem(it) }
            this.toDoCalDav.getToDos(updateProgress, progressLabel).forEach { (key, values) ->
                values.forEach { value ->
                    val item = this.toDoCalDav.toDoToDatabase(key, value)
                    this.toDoItemDAO.insertToDoItem(item)
                }
            }
        } else {
            val dbList = toDoItemDAO.getLists(authId).find { it.uid == uid }
            if(dbList != null) {
                val list = ToDoList(dbList.name!!, dbList.color!!, dbList.uid!!)
                this.toDoItemDAO.getAll(this.authId).forEach { this.toDoItemDAO.deleteToDoItem(it) }
                this.toDoCalDav.getToDos(list, updateProgress, progressLabel).forEach { value ->
                    val item = this.toDoCalDav.toDoToDatabase(list, value)
                    this.toDoItemDAO.insertToDoItem(item)
                }
            }
        }
    }

    override fun getToDoItems(uid: String?): List<ToDoItem> {
        return if(uid == null) {
            this.toDoItemDAO.getAll(this.authId)
        } else {
            this.toDoItemDAO.getItemsOfList(this.authId, uid)
        }
    }

    override fun insertToDoItem(toDoItem: ToDoItem) {
        var find = this.toDoCalDav.getToDoLists().find { it.path == toDoItem.listUid }
        if(find == null) {
            val toDoList = this.toDoCalDav.databaseToList(toDoItem)
            toDoList.path = this.toDoCalDav.insertToDoList(toDoList)
            find = toDoList
        }
        var toDo = this.toDoCalDav.databaseToToDo(toDoItem)
        toDo = this.toDoCalDav.insertToDo(find, toDo)
        toDoItem.path = toDo.path
        toDoItem.uid = toDo.uid
        toDoItem.authId = authId

        this.toDoItemDAO.insertToDoItem(toDoItem)

    }

    override fun updateToDoItem(toDoItem: ToDoItem) {
        val todo = toDoCalDav.databaseToToDo(toDoItem)
        toDoItem.authId = authId
        toDoCalDav.updateToDo(todo)
        toDoItemDAO.updateToDoItem(toDoItem)
    }

    override fun deleteToDoItem(toDoItem: ToDoItem) {
        val todo = toDoCalDav.databaseToToDo(toDoItem)
        toDoCalDav.deleteToDo(todo)
        toDoItemDAO.deleteToDoItem(toDoItem)
    }

}