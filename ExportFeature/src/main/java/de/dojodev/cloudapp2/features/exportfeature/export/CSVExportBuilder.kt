/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder
import java.io.File
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.rest.model.msg.Message
import de.domjos.cloudapp2.rest.model.notes.Note
import de.domjos.cloudapp2.rest.model.notifications.Notification
import de.domjos.cloudapp2.rest.model.room.Room
import de.domjos.cloudapp2.webdav.model.Item
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.forEach

class CSVExportBuilder(private val context: Context): BaseExportBuilder(context) {
    override suspend fun exportNotifications(): String {
        update(context.getString(R.string.export_fetch))

        var notifications = listOf<Notification>()
        super.notificationRequest.getNotifications().collect {items ->
            notifications = items
        }
        notifications = notifications.filter { super.id==null || it.notification_id==super.id }


        update(context.getString(R.string.export_write))

        val lines = mutableListOf<String>()
        lines.add(
            buildLine(
                listOf(
                    "id",
                    "app",
                    "icon",
                    "datetime",
                    "link",
                    "message",
                    "subject",
                    "user"
                )
            )
        )

        notifications.forEach { notification ->
            lines.add(
                buildLine(
                    listOf(
                        notification.notification_id.toString(),
                        notification.app,
                        notification.icon,
                        notification.datetime,
                        notification.link,
                        notification.message,
                        notification.subject,
                        notification.user
                    )
                )
            )
        }

        this.writeFile(lines)
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportData(): String {
        update(context.getString(R.string.export_fetch))

        val items = ConcurrentLinkedQueue<Item>()
        items.addAll(super.webDav.getList())
        items.forEach { item -> addItems(item, items)}

        update(context.getString(R.string.export_write))

        val lines = mutableListOf<String>()
        lines.add(
            buildLine(
                listOf(
                    "path",
                    "name",
                    "directory",
                    "exists",
                    "type",
                    "shared_with_me",
                    "shared_from_me"
                )
            )
        )

        items.forEach { item ->
            lines.add(
                buildLine(
                    listOf(
                        item.path,
                        item.name,
                        item.directory.toString(),
                        item.exists.toString(),
                        item.type,
                        item.sharedWithMe?.displayname_owner ?: "",
                        item.sharedFromMe?.share_with ?: ""
                    )
                )
            )
        }

        this.writeFile(lines)
        update(context.getString(R.string.export_success))
        return super.path
    }

    private fun addItems(item: Item, items: ConcurrentLinkedQueue<Item>) {
        if(item.directory && item.name != "..") {
            super.webDav.openFolder(item)
            val subs = super.webDav.getList()
            items.addAll(subs.filter { it.name != ".." })
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
        notes = notes.filter { super.id==null || super.id==it.id.toLong() }.toMutableList()

        update(context.getString(R.string.export_write))

        val lines = mutableListOf<String>()
        lines.add(
            buildLine(
                listOf(
                    "id",
                    "title",
                    "category",
                    "content",
                    "readonly",
                    "favorite",
                    "modified"
                )
            )
        )

        notes.forEach { note ->
            lines.add(
                buildLine(
                    listOf(
                        note.id.toString(),
                        note.title,
                        note.category,
                        note.content,
                        note.readonly.toString(),
                        note.favorite.toString(),
                        note.modified.toString()
                    )
                )
            )
        }

        this.writeFile(lines)
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportCalendars(): String {
        update(context.getString(R.string.export_fetch))

        val calendarEvents = super.calendarEventDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        val lines = mutableListOf<String>()
        lines.add(
            buildLine(
                listOf(
                    "id",
                    "title",
                    "from",
                    "to",
                    "categories",
                    "description",
                    "confirmation",
                    "calendar",
                    "path"
                )
            )
        )

        calendarEvents.forEach { event ->
            lines.add(
                buildLine(
                    listOf(
                        event.id.toString(),
                        event.title,
                        event.string_from,
                        event.string_to,
                        event.categories,
                        event.description,
                        event.confirmation,
                        event.calendar,
                        event.path
                    )
                )
            )
        }

        this.writeFile(lines)
        update(context.getString(R.string.export_success))
        return super.path

    }

    override suspend fun exportContacts(): String {
        update(context.getString(R.string.export_fetch))

        val contacts = super.contactDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        val lines = mutableListOf<String>()
        lines.add(
            buildLine(
                listOf(
                    "id",
                    "prefix",
                    "suffix",
                    "givenName",
                    "familyName",
                    "organization",
                    "additional",
                    "birthday",
                    "categories",
                    "phoneNumbers",
                    "addresses",
                    "emailAddresses",
                    "addressBook"
                )
            )
        )

        contacts.forEach { contact ->
            lines.add(
                buildLine(
                    listOf(
                        contact.id.toString(),
                        contact.prefix ?: "",
                        contact.suffix ?: "",
                        contact.givenName,
                        contact.familyName ?: "",
                        contact.organization ?: "",
                        contact.additional ?: "",
                        contact.birthDay.toString(),
                        contact.categories.joinToString(","),
                        contact.phoneNumbers.joinToString(",") {
                            "${it.value}:" + it.types.joinToString("-") { m -> m.name }
                        },
                        contact.addresses.joinToString(",") {
                            "${it.street}, ${it.postalCode} ${it.locality}, ${it.country}, ${it.extendedAddress}" + it.types.joinToString(
                                "-"
                            ) { m -> m.name }
                        },
                        contact.emailAddresses.joinToString(",") { it.value },
                        contact.addressBook
                    )
                )
            )
        }

        this.writeFile(lines)
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportToDos(): String {
        update(context.getString(R.string.export_fetch))

        val todos = super.toDoItemDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        val lines = mutableListOf<String>()
        lines.add(
            buildLine(
                listOf(
                    "id",
                    "summary",
                    "categories",
                    "start",
                    "end",
                    "status",
                    "completed",
                    "priority",
                    "list"
                )
            )
        )

        todos.forEach { todo ->
            lines.add(
                buildLine(
                    listOf(
                        todo.id.toString(),
                        todo.summary,
                        todo.categories ?: "",
                        todo.start?.toString() ?: "",
                        todo.end?.toString() ?: "",
                        todo.status.name,
                        todo.completed.toString(),
                        todo.priority.toString(),
                        todo.listName
                    )
                )
            )
        }

        this.writeFile(lines)
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportChats(): String {
        update(context.getString(R.string.export_fetch))

        val items = mutableMapOf<Room, List<Message>>()
        super.roomRequest.getRooms().collect { rooms ->
            rooms.forEach { room ->
                items[room] = super.chatRequest.getChats(token = room.token)
            }
        }


        update(context.getString(R.string.export_write))

        val lines = mutableListOf<String>()
        lines.add(
            buildLine(
                listOf(
                    "id",
                    "token",
                    "timestamp",
                    "actor",
                    "message",
                    "room-name",
                    "room-type",
                    "room-description"
                )
            )
        )

        items.forEach { (key, values) ->
            values.forEach {value ->
                lines.add(
                    buildLine(
                        listOf(
                            value.id.toString(),
                            value.token,
                            value.timestamp.toString(),
                            value.actorDisplayName,
                            value.message,
                            key.name ?: "",
                            key.type.toString(),
                            key.description ?: ""
                        )
                    )
                )
            }
        }

        this.writeFile(lines)
        update(context.getString(R.string.export_success))
        return super.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.notifications, super.data, super.notes, super.contacts, super.calendars, super.todos, super.chats)
    }

    override fun getExtension(): List<String> {
        return listOf("csv", "txt")
    }

    private fun buildLine(items: List<String>): String {
        return "${items.joinToString(";")}\n"
    }

    private fun writeFile(lines: List<String>) {
        val file = File(super.path)
        if(!file.exists()) {
            file.createNewFile()
        }
        file.bufferedWriter().use { out ->
            lines.forEach {
                out.write(it)
            }
        }
    }
}