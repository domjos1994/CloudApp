/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.base

import android.content.Context
import android.os.Environment
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.dao.ToDoItemDAO
import de.domjos.cloudapp2.rest.requests.AvatarRequest
import de.domjos.cloudapp2.rest.requests.ChatRequest
import de.domjos.cloudapp2.rest.requests.NoteRequest
import de.domjos.cloudapp2.rest.requests.NotificationRequest
import de.domjos.cloudapp2.rest.requests.RoomRequest
import de.domjos.cloudapp2.webdav.WebDav

abstract class BaseExportBuilder(context: Context) : ExportBuilder {
    protected val notifications = context.getString(R.string.notifications)
    protected val data = context.getString(R.string.data)
    protected val notes = context.getString(R.string.notes)
    protected val calendars = context.getString(R.string.calendars)
    protected val contacts = context.getString(R.string.contacts)
    protected val todos = context.getString(R.string.todos)
    protected val chats = context.getString(R.string.chats)

    protected var id: Long? = null
    protected var name: String = ""
    protected var open: Boolean = false
    protected lateinit var path: String
    protected lateinit var contactDAO: ContactDAO
    protected lateinit var toDoItemDAO: ToDoItemDAO
    protected lateinit var calendarEventDAO: CalendarEventDAO
    protected lateinit var authenticationDAO: AuthenticationDAO
    protected lateinit var noteRequest: NoteRequest
    protected lateinit var notificationRequest: NotificationRequest
    protected lateinit var roomRequest: RoomRequest
    protected lateinit var chatRequest: ChatRequest
    protected lateinit var avatarRequest: AvatarRequest
    protected lateinit var webDav: WebDav
    private var updateLabel: (String) -> Unit = {}

    override fun setUpdateLabel(action: (String) -> Unit) {
        this.updateLabel = action
    }

    protected fun update(label: String) {
        this.updateLabel(label)
    }

    override suspend fun doExport(id: Long?, type: String, open: Boolean): String {
        this.id = id
        this.open = open

        return when(type) {
            this.notifications -> exportNotifications()
            this.data -> exportData()
            this.notes -> exportNotes()
            this.calendars -> exportCalendars()
            this.contacts -> exportContacts()
            this.todos -> exportToDos()
            this.chats -> exportChats()
            else -> ""
        }
    }

    override fun generatePath(name: String, ext: String) {
        this.name = name
        val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        this.path = "${downloadDir}/${this.name}.${ext}"
    }

    override fun initData(
        contactDAO: ContactDAO,
        calendarEventDAO: CalendarEventDAO,
        toDoItemDAO: ToDoItemDAO,
        authenticationDAO: AuthenticationDAO
    ) {
        this.contactDAO = contactDAO
        this.calendarEventDAO = calendarEventDAO
        this.toDoItemDAO = toDoItemDAO
        this.authenticationDAO = authenticationDAO
        this.noteRequest = NoteRequest(this.authenticationDAO.getSelectedItem())
        this.notificationRequest = NotificationRequest(this.authenticationDAO.getSelectedItem())
        this.roomRequest = RoomRequest(this.authenticationDAO.getSelectedItem())
        this.chatRequest = ChatRequest(this.authenticationDAO.getSelectedItem())
        this.avatarRequest = AvatarRequest(this.authenticationDAO.getSelectedItem())
        this.webDav = WebDav(this.authenticationDAO.getSelectedItem()!!)
    }

    protected abstract suspend fun exportNotifications(): String
    protected abstract suspend fun exportData(): String
    protected abstract suspend fun exportNotes(): String
    protected abstract suspend fun exportCalendars(): String
    protected abstract suspend fun exportContacts(): String
    protected abstract suspend fun exportToDos(): String
    protected abstract suspend fun exportChats(): String
}