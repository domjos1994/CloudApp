/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder

class MDExportBuilder(context: Context) : BaseExportBuilder(context) {
    override suspend fun exportNotifications(): String {
        TODO("Not yet implemented")
    }

    override suspend fun exportData(): String {
        TODO("Not yet implemented")
    }

    override suspend fun exportNotes(): String {
        TODO("Not yet implemented")
    }

    override suspend fun exportCalendars(): String {
        TODO("Not yet implemented")
    }

    override suspend fun exportContacts(): String {
        TODO("Not yet implemented")
    }

    override suspend fun exportToDos(): String {
        TODO("Not yet implemented")
    }

    override suspend fun exportChats(): String {
        TODO("Not yet implemented")
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.notifications, super.data, super.notes, super.contacts, super.calendars, super.todos, super.chats)
    }

    override fun getExtension(): List<String> {
        return listOf("md")
    }
}