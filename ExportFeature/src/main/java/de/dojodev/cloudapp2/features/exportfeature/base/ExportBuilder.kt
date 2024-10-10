/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.base

import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.dao.ToDoItemDAO

interface ExportBuilder {

    fun getSupportedTypes(): List<String>
    fun getExtension(): List<String>
    fun generatePath(name: String, ext: String)
    fun initData(contactDAO: ContactDAO, calendarEventDAO: CalendarEventDAO, toDoItemDAO: ToDoItemDAO, authenticationDAO: AuthenticationDAO)
    fun setUpdateLabel(action: (String) -> Unit)

    suspend fun doExport(id: Long?, type: String, open: Boolean = false): String
}