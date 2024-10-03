/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.caldav

import android.Manifest
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import de.domjos.cloudapp2.caldav.model.ToDoList
import de.domjos.cloudapp2.caldav.model.Todo
import de.domjos.cloudapp2.caldav.test.R
import de.domjos.cloudapp2.database.model.Authentication
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.Properties
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ToDoCalDavTest {
    private lateinit var toDoCalDav: ToDoCalDav

    @Rule
    @JvmField
    var runtimePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)

    companion object {
        private var context: Context? = null
        @JvmStatic
        private var authentication: Authentication? = null
        @JvmStatic
        private var props: Properties? = null

        /**
         * Read connection
         * and initialize authentication
         */
        @JvmStatic
        @BeforeClass
        fun before() {
            this.context = InstrumentationRegistry.getInstrumentation().targetContext
            val stream = this.context!!.resources.openRawResource(R.raw.example)
            props = Properties()
            props?.load(stream)
            stream.close()

            authentication = Authentication(
                1L, "N28", props!!["url"].toString(),
                props!!["user"].toString(), props!!["pwd"].toString(),
                true, "", null
            )
        }
    }

    @Before
    fun init() {
        this.toDoCalDav = ToDoCalDav(authentication)
    }

    @Test
    fun testGettingToDoLists() {
        runBlocking {
            val lst = toDoCalDav.getToDoLists()
            assertNotEquals(0, lst.size)
        }
    }

    @Test
    fun testInsertingDeletingToDoList() {
        runBlocking {
            // get items
            val lst = toDoCalDav.getToDoLists()
            val count = lst.size

            // insert items
            val lists = ToDoList(UUID.randomUUID().toString(), "#ff00ff", "")
            toDoCalDav.insertToDoList(lists)

            // compare items
            assertNotEquals(count, toDoCalDav.getToDoLists().size)

            // delete items
            toDoCalDav.deleteToDoList(lists)
            assertEquals(count, toDoCalDav.getToDoLists().size)
        }
    }

    @Test
    fun testUpdateToDoList() {
        runBlocking {
            // get items
            val lst = toDoCalDav.getToDoLists()
            val count = lst.size

            // create uuids
            val old = UUID.randomUUID().toString()
            val new = UUID.randomUUID().toString()

            // insert items
            val list = ToDoList(old, "#ff00ff", "")
            list.path = toDoCalDav.insertToDoList(list)

            // compare items
            assertNotEquals(count, toDoCalDav.getToDoLists().size)

            // update item
            list.name = new
            toDoCalDav.updateToDoList(list)

            // find item
            val find = toDoCalDav.getToDoLists().find { it.name == new }
            assertNotNull(find)

            // delete items
            toDoCalDav.deleteToDoList(list)
            assertEquals(count, toDoCalDav.getToDoLists().size)
        }
    }

    @Test
    fun testGettingToDos() {
        runBlocking {
            // todos
            val toDos = toDoCalDav.getToDos({_,_->}, "")
            assertNotNull(toDos)
            assertNotEquals(0, toDos.size)
        }
    }

    @Test
    fun testInsertingDeletingToDos() {
        runBlocking {

            // new uuid and new todolist
            var uuid = UUID.randomUUID().toString().lowercase()
            val todoList = ToDoList(uuid, "", "")
            todoList.path = toDoCalDav.insertToDoList(todoList)

            // create to Do
            val dt = Date()
            uuid = UUID.randomUUID().toString()
            val todo = Todo(
                "", dt, dt, dt, uuid, dt, dt, "IN-PROCESS", 30, 2,
                "", "", "test, test-1", ""
            )
            toDoCalDav.insertToDo(todoList, todo)

            // find item
            var items = toDoCalDav.getToDos(todoList, {_,_->}, "")
            var find = items.find { it.summary == uuid }
            assertNotNull(find)

            toDoCalDav.deleteToDo(find!!)

            // find item
            items = toDoCalDav.getToDos(todoList, {_,_->}, "")
            find = items.find { it.summary == uuid }
            assertNull(find)

            // delete item
            toDoCalDav.deleteToDoList(todoList)
        }
    }

    @Test
    fun testUpdatingToDos() {
        runBlocking {

            // new uuid and new todolist
            var uuid = UUID.randomUUID().toString().lowercase()
            val todoList = ToDoList(uuid, "", "")
            todoList.path = toDoCalDav.insertToDoList(todoList)

            // create to Do
            val dt = Date()
            uuid = UUID.randomUUID().toString()
            var todo = Todo(
                "", dt, dt, dt, uuid, dt, dt, "IN-PROCESS", 30, 2,
                uuid, "", "test, test-1", ""
            )
            todo = toDoCalDav.insertToDo(todoList, todo)

            // find item
            var items = toDoCalDav.getToDos(todoList, {_,_->}, "")
            var find = items.find { it.summary == uuid }
            assertNotNull(find)

            // update item
            val new = UUID.randomUUID().toString()
            todo.summary = new
            toDoCalDav.updateToDo(todo)

            // search new name
            items = toDoCalDav.getToDos(todoList, {_,_->}, "")
            find = items.find { it.summary == new }
            assertNotNull(find)

            toDoCalDav.deleteToDo(find!!)

            // find item
            items = toDoCalDav.getToDos(todoList, {_,_->}, "")
            find = items.find { it.summary == uuid }
            assertNull(find)

            // delete item
            toDoCalDav.deleteToDoList(todoList)
        }
    }
}