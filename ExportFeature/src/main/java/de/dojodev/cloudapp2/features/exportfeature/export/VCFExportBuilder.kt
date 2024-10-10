/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder

class VCFExportBuilder(context: Context): BaseExportBuilder(context)  {
    override suspend fun exportNotifications(): String {
        return this.path
    }

    override suspend fun exportData(): String {
        return this.path
    }

    override suspend fun exportNotes(): String {
        return this.path
    }

    override suspend fun exportCalendars(): String {
        return this.path
    }

    override suspend fun exportContacts(): String {
        TODO("Not yet implemented")
    }

    override suspend fun exportToDos(): String {
        return this.path
    }

    override suspend fun exportChats(): String {
        return this.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.contacts)
    }

    override fun getExtension(): List<String> {
        return listOf("vcf")
    }
}