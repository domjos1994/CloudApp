/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.rest.model.msg.Message
import de.domjos.cloudapp2.rest.model.notes.Note
import de.domjos.cloudapp2.rest.model.notifications.Notification
import de.domjos.cloudapp2.rest.model.room.Room
import de.domjos.cloudapp2.webdav.model.Item
import java.util.concurrent.ConcurrentLinkedQueue

class HTMLExportBuilder(private val context: Context) : BaseExportBuilder(context) {
    override suspend fun exportNotifications(): String {
        update(context.getString(R.string.export_fetch))

        var notifications = listOf<Notification>()
        super.notificationRequest.getNotifications().collect {items ->
            notifications = items
        }
        notifications = notifications.filter { super.id==null || it.notification_id==super.id }


        update(context.getString(R.string.export_write))

        var content = printStart() + printHeader("Notifications")
        content += createTable(
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

        notifications.forEach { notification ->
            content =
                printLine(
                    listOf(
                        notification.notification_id.toString(),
                        notification.app,
                        notification.icon,
                        notification.datetime,
                        notification.link,
                        notification.message,
                        notification.subject,
                        notification.user
                    ), content
                )
        }
        content = content.replace("%s", "")

        this.writeFile(content + printEnd())
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportData(): String {
        update(context.getString(R.string.export_fetch))

        val items = ConcurrentLinkedQueue<Item>()
        items.addAll(super.webDav.getList())
        items.forEach { item -> addItems(item, items)}


        update(context.getString(R.string.export_write))

        var content = printStart() + printHeader("Data")
        content += createTable(
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

        items.forEach { item ->
            content =
                printLine(
                    listOf(
                        item.path,
                        item.name,
                        item.directory.toString(),
                        item.exists.toString(),
                        item.type,
                        item.sharedWithMe?.displayname_owner ?: "",
                        item.sharedFromMe?.share_with ?: ""
                    ), content
                )
        }
        content = content.replace("%s", "")

        this.writeFile(content + printEnd())
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

        var content = printStart() + printHeader("Notes")
        content += createTable(
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

        notes.forEach { note ->
            content =
                printLine(
                    listOf(
                        note.id.toString(),
                        note.title,
                        note.category,
                        note.content,
                        note.readonly.toString(),
                        note.favorite.toString(),
                        note.modified.toString()
                    ), content
                )
        }
        content = content.replace("%s", "")

        this.writeFile(content + printEnd())
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportCalendars(): String {
        update(context.getString(R.string.export_fetch))

        val calendarEvents = super.calendarEventDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }


        update(context.getString(R.string.export_write))

        var content = printStart() + printHeader("Data")
        content += createTable(
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

        calendarEvents.forEach { event ->
            content =
                printLine(
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
                    ), content
                )
        }
        content = content.replace("%s", "")

        this.writeFile(content + printEnd())
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportContacts(): String {
        update(context.getString(R.string.export_fetch))

        val contacts = super.contactDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }


        update(context.getString(R.string.export_write))

        var content = printStart() + printHeader("Data")
        content += createTable(
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

        contacts.forEach { contact ->
            content =
                printLine(
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
                    ), content
                )
        }
        content = content.replace("%s", "")

        this.writeFile(content + printEnd())
        update(context.getString(R.string.export_success))
        return super.path
    }

    override suspend fun exportToDos(): String {
        update(context.getString(R.string.export_fetch))

        val todos = super.toDoItemDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }


        update(context.getString(R.string.export_write))

        var content = printStart() + printHeader("Data")
        content += createTable(
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

        todos.forEach { todo ->
            content =
                printLine(
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
                    ), content
                )
        }
        content += content.replace("%s", "")

        this.writeFile(content + printEnd())
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

        var content = printStart() + printHeader("Data")
        content += createTable(
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

        items.forEach { (key, values) ->
            values.forEach { value ->
                content =
                    printLine(
                        listOf(
                            value.id.toString(),
                            value.token,
                            value.timestamp.toString(),
                            value.actorDisplayName,
                            value.message,
                            key.name ?: "",
                            key.type.toString(),
                            key.description ?: ""
                        ), content
                    )
            }
        }
        content += content.replace("%s", "")

        this.writeFile(content + printEnd())
        update(context.getString(R.string.export_success))
        return super.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.notifications, super.data, super.notes, super.contacts, super.calendars, super.todos, super.chats)
    }

    override fun getExtension(): List<String> {
        return listOf("html")
    }

    private fun printStart(): String {
        return "<html>\n\t<head>\n\t\t<title>Export</title>\n\t</head>\n\t<body>"
    }

    private fun printEnd(): String {
        return "\n\t</body>\n</html>"
    }

    @Suppress("SameParameterValue")
    private fun printHeader(value: String): String {
        return "\n\t\t<h1>$value</h1>\n"
    }

    private fun createTable(columns: List<String>): String {
        var content = "\n\t\t<table>\n\t\t\t<thead>\n\t\t\t\t<tr>"
        columns.forEach { column ->  content += "\n\t\t\t\t\t<th>$column</th>"}
        content += "\n\t\t\t\t</tr>\n\t\t\t</thead>\n\t\t\t<tbody>%s\n\t\t\t</tbody>\n\t\t</table>"
        return content
    }

    private fun printLine(items: List<String>, content: String): String {
        var line = "\n\t\t\t\t<tr>"
        items.forEach { column ->  line += "\n\t\t\t\t\t<td>$column</td>"}
        line += "\n\t\t\t\t</tr>"
        return content.replace("%s", "$line%s")
    }
}