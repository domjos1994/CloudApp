/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.BaseTest
import de.domjos.cloudapp2.database.model.todo.ToDoItem
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ToDoItemDaoTest : BaseTest() {
    private lateinit var toDoItemDAO: ToDoItemDAO
    private lateinit var uidList1: String
    private lateinit var uidList2: String
    private lateinit var list1Item1Uid: String

    @Before
    fun before() {
        super.init()
        toDoItemDAO = super.db.todoItemDao()

        uidList1 = UUID.randomUUID().toString()
        uidList2 = UUID.randomUUID().toString()
        list1Item1Uid = UUID.randomUUID().toString()
    }

    @Test
    fun testInsertingDeleting() {
        val item1 = ToDoItem(
            uid = list1Item1Uid, listUid = uidList1, listName = uidList1, listColor = uidList1,
            summary = list1Item1Uid
        )

        // load items
        var items = this.toDoItemDAO.getAll(0L)
        assertEquals(0, items.size)

        // insert Item
        item1.id = this.toDoItemDAO.insertToDoItem(item1)

        // load items
        items = this.toDoItemDAO.getAll(0L)
        assertNotEquals(0, items.size)

        // delete item
        this.toDoItemDAO.deleteToDoItem(item1)

        // load items
        items = this.toDoItemDAO.getAll(0L)
        assertEquals(0, items.size)
    }

    @Test
    fun testUpdating() {
        val item1 = ToDoItem(
            uid = list1Item1Uid, listUid = uidList1, listName = uidList1, listColor = uidList1,
            summary = list1Item1Uid
        )

        // load items
        var items = this.toDoItemDAO.getAll(0L)
        assertEquals(0, items.size)

        // insert Item
        item1.id = this.toDoItemDAO.insertToDoItem(item1)

        // load items
        items = this.toDoItemDAO.getItemsOfList(0L, uidList1)
        assertNotEquals(0, items.size)
        items = this.toDoItemDAO.getItemsOfList(0L, uidList2)
        assertEquals(0, items.size)

        // update item
        item1.listUid = uidList2
        this.toDoItemDAO.updateToDoItem(item1)

        // load items
        items = this.toDoItemDAO.getItemsOfList(0L, uidList1)
        assertEquals(0, items.size)
        items = this.toDoItemDAO.getItemsOfList(0L, uidList2)
        assertNotEquals(0, items.size)

        // delete item
        this.toDoItemDAO.deleteToDoItem(item1)

        // load items
        items = this.toDoItemDAO.getAll(0L)
        assertEquals(0, items.size)
    }
}