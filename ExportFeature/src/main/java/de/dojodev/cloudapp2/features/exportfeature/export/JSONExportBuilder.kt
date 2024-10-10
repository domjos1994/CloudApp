/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import de.domjos.cloudapp2.appbasics.R
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JSONExportBuilder(private val context: Context): XMLExportBuilder(context) {
    override var writeFile: Boolean = false

    override suspend fun exportNotifications(): String {
        super.exportNotifications()

        val content = Json.encodeToString(super.value)
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportData(): String {
        super.exportData()

        val content = Json.encodeToString(super.value)
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportNotes(): String {
        super.exportNotes()

        val content = Json.encodeToString(super.value)
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportCalendars(): String {
        super.exportCalendars()

        val content = Json.encodeToString(super.value)
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportContacts(): String {
        super.exportContacts()

        val content = Json.encodeToString(super.value)
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportToDos(): String {
        super.exportToDos()

        val content = Json.encodeToString(super.value)
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return this.path
    }

    override suspend fun exportChats(): String {
        super.exportChats()

        val content = Json.encodeToString(super.value)
        this.writeFile(content)

        update(context.getString(R.string.export_success))
        return this.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.notifications, super.data, super.notes, super.contacts, super.calendars, super.todos, super.chats)
    }

    override fun getExtension(): List<String> {
        return listOf("json")
    }
}