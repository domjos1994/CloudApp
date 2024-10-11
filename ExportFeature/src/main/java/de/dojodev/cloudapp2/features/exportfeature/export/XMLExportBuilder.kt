/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import com.ryanharter.kotlinx.serialization.xml.Xml
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedAddress
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedChat
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedChats
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedContact
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedContacts
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedData
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedDataItems
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedEmailAddress
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedEvent
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedEvents
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedNote
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedNotes
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedNotification
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedNotifications
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedPhoneNumber
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedTodo
import de.dojodev.cloudapp2.features.exportfeature.model.SerializedTodos
import de.dojodev.cloudapp2.features.exportfeature.model.Share
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.rest.model.msg.Message
import de.domjos.cloudapp2.rest.model.notes.Note
import de.domjos.cloudapp2.rest.model.room.Room
import de.domjos.cloudapp2.webdav.model.Item
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import java.util.concurrent.ConcurrentLinkedQueue

@OptIn(ExperimentalSerializationApi::class)
open class XMLExportBuilder(private val context: Context): BaseExportBuilder(context) {
    protected lateinit var serializedNotifications: SerializedNotifications
    protected lateinit var serializedDataItems: SerializedDataItems
    protected lateinit var serializedNotes: SerializedNotes
    protected lateinit var serializedEvents: SerializedEvents
    protected lateinit var serializedContacts: SerializedContacts
    protected lateinit var serializedTodos: SerializedTodos
    protected lateinit var serializedChats: SerializedChats
    protected open var writeFile: Boolean = true

    override suspend fun exportNotifications(): String {
        update(context.getString(R.string.export_fetch))

        val notifications = ArrayList<SerializedNotification>()
        super.notificationRequest.getNotifications().collect {items ->
            notifications.addAll(
                items.filter { super.id==null || it.notification_id==super.id }
                    .map {
                        SerializedNotification(
                            it.notification_id, it.app, it.icon, it.user,
                            it.datetime, it.subject, it.message, it.link
                        )
                    }
            )
        }

        update(context.getString(R.string.export_write))

        this.serializedNotifications = SerializedNotifications(
            items = notifications.toTypedArray()
        )
        val content = Xml.Default.encodeToString(this.serializedNotifications)
        if(writeFile) this.writeFile(content)

        if(writeFile) update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportData(): String {
        update(context.getString(R.string.export_fetch))

        val items = ConcurrentLinkedQueue<Item>()
        val serializedItems = ConcurrentLinkedQueue<SerializedData>()
        items.addAll(super.webDav.getList())
        serializedItems.addAll(items.map {
            SerializedData(it.name, it.directory, it.exists, it.type,
                Share(
                    it.sharedWithMe?.displayname_owner ?: "",
                    it.sharedFromMe?.share_with ?: ""
                ), it.path)
        })
        items.forEach { item -> addItems(item, serializedItems)}

        update(context.getString(R.string.export_write))

        this.serializedDataItems = SerializedDataItems(
            serializedItems.toTypedArray()
        )
        val content = Xml.Default.encodeToString(this.serializedDataItems)
        if(writeFile) this.writeFile(content)

        if(writeFile) update(context.getString(R.string.export_success))
        return this.path
    }

    private fun addItems(item: Item, items: ConcurrentLinkedQueue<SerializedData>) {
        if(item.directory && item.name != "..") {
            super.webDav.openFolder(item)
            val subs = super.webDav.getList()
            items.addAll(subs.filter { it.name != ".." }.map {
                SerializedData(it.name, it.directory, it.exists, it.type,
                    Share(
                        it.sharedWithMe?.displayname_owner ?: "",
                        it.sharedFromMe?.share_with ?: ""
                    ), it.path)
            })
            subs.forEach { sub ->
                this.addItems(sub, items)
            }
        }
    }

    override suspend fun exportNotes(): String {
        update(context.getString(R.string.export_fetch))

        var notes = mutableListOf<Note>()
        super.noteRequest.getNotes().collect { noteList ->
            notes.addAll(noteList)
        }
        notes = notes
            .filter { super.id==null || super.id==it.id.toLong() }
            .toMutableList()
        val serializedNotes = notes.map {
            SerializedNote(
                it.id.toLong(),
                it.title,
                it.category,
                it.readonly,
                it.favorite,
                it.modified,
                it.content
            )
        }

        update(context.getString(R.string.export_write))

        this.serializedNotes = SerializedNotes(
            serializedNotes.toTypedArray()
        )
        val content = Xml.Default.encodeToString(this.serializedNotes)
        if(writeFile) this.writeFile(content)

        if(writeFile) update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportCalendars(): String {
        update(context.getString(R.string.export_fetch))

        val calendarEvents = super.calendarEventDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }
        val serializedEvents = calendarEvents.map {
            SerializedEvent(
                it.id, it.title, it.string_from, it.string_to,
                it.categories, it.confirmation, it.calendar,
                it.path, it.description
            )
        }

        update(context.getString(R.string.export_write))

        this.serializedEvents = SerializedEvents(
            serializedEvents.toTypedArray()
        )
        val content = Xml.Default.encodeToString(this.serializedEvents)
        if(writeFile) this.writeFile(content)

        if(writeFile) update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportContacts(): String {
        update(context.getString(R.string.export_fetch))

        val contacts = super.contactDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        val serializedContacts = contacts.map {
            SerializedContact(
                it.id, it.prefix ?: "", it.suffix ?: "", it.givenName, it.familyName ?: "",
                it.organization ?: "", it.additional ?: "", it.birthDay?.toString() ?: "",
                it.categories.joinToString(","), it.addressBook,
                it.addresses.map { add ->
                    SerializedAddress(
                        add.street, add.postalCode ?: "", add.locality ?: "",
                        add.country ?: "", add.extendedAddress ?: "",
                        add.types.map { t -> t.name }
                    )
                },
                it.emailAddresses.map { email ->
                    SerializedEmailAddress(email.value)
                },
                it.phoneNumbers.map { phone ->
                    SerializedPhoneNumber(
                        phone.value,
                        phone.types.map { t -> t.name }
                    )
                }
            )
        }

        update(context.getString(R.string.export_write))

        this.serializedContacts = SerializedContacts(
            serializedContacts.toTypedArray()
        )
        val content = Xml.Default.encodeToString(serializedContacts)
        if(writeFile) this.writeFile(content)

        if(writeFile) update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportToDos(): String {
        update(context.getString(R.string.export_fetch))

        val todos = super.toDoItemDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }
        val serializedTodos = todos.map {
            SerializedTodo(
                it.id, it.summary, it.categories ?: "", it.start.toString(),
                it.end.toString(), it.status.name, it.completed, it.priority,
                it.listName
            )
        }

        update(context.getString(R.string.export_write))

        this.serializedTodos = SerializedTodos(
            serializedTodos.toTypedArray()
        )
        val content = Xml.Default.encodeToString(this.serializedTodos)
        if(writeFile) this.writeFile(content)

        if(writeFile) update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportChats(): String {
        update(context.getString(R.string.export_fetch))

        val items = mutableMapOf<Room, List<Message>>()
        super.roomRequest.getRooms().collect { rooms ->
            rooms.forEach { room ->
                items[room] = super.chatRequest.getChats(token = room.token)
            }
        }
        val serializedChats = mutableListOf<SerializedChat>()
        items.forEach { (key, values) ->
            values.forEach { value ->
                serializedChats.add(
                    SerializedChat(
                        value.id.toLong(), value.token, value.timestamp,
                        value.actorDisplayName, key.name ?: "", key.type, key.description ?: "",
                        value.message
                    )
                )
            }
        }

        update(context.getString(R.string.export_write))

        this.serializedChats = SerializedChats(
            serializedChats.toTypedArray()
        )
        val content = Xml.Default.encodeToString(this.serializedChats)
        if(writeFile) this.writeFile(content)

        if(writeFile) update(context.getString(R.string.export_success))
        return this.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.notifications, super.data, super.notes, super.contacts, super.calendars, super.todos, super.chats)
    }

    override fun getExtension(): List<String> {
        return listOf("xml")
    }
}