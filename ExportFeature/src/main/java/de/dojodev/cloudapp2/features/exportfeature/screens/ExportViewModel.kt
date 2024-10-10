/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.screens

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.dojodev.cloudapp2.features.exportfeature.base.ExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.CSVExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.HTMLExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.ICSExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.JSONExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.MDExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.PDFExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.VCFExportBuilder
import de.dojodev.cloudapp2.features.exportfeature.export.XMLExportBuilder
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.CalendarEventDAO
import de.domjos.cloudapp2.database.dao.ContactDAO
import de.domjos.cloudapp2.database.dao.ToDoItemDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val contactDAO: ContactDAO,
    private val calendarEventDAO: CalendarEventDAO,
    private val toDoItemDAO: ToDoItemDAO,
    private val authenticationDAO: AuthenticationDAO
): LogViewModel() {
    private val _extensions = MutableStateFlow(listOf<String>())
    val extensions: StateFlow<List<String>> get() = _extensions
    private val _types = MutableStateFlow(listOf<String>())
    val types: StateFlow<List<String>> get() = _types

    private var ext: String = ""
    private var type: String = ""
    private val exportBuilder = mutableListOf<ExportBuilder>()

    val progress = MutableLiveData<String>()

    fun initBuilder(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                exportBuilder.add(CSVExportBuilder(context))
                exportBuilder.add(HTMLExportBuilder(context))
                exportBuilder.add(ICSExportBuilder(context))
                exportBuilder.add(JSONExportBuilder(context))
                exportBuilder.add(MDExportBuilder(context))
                exportBuilder.add(PDFExportBuilder(context))
                exportBuilder.add(VCFExportBuilder(context))
                exportBuilder.add(XMLExportBuilder(context))

                val extensions = mutableListOf<String>()
                exportBuilder.forEach { builder ->
                    extensions.addAll(builder.getExtension())
                    builder.setUpdateLabel { progress.postValue(it) }
                }
                _extensions.value = extensions
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun getTypes(extension: String) {
        this.ext = extension
        viewModelScope.launch(Dispatchers.IO) {
            try {
                exportBuilder.forEach { builder ->
                    if(builder.getExtension().contains(ext)) {
                        _types.value = builder.getSupportedTypes()
                    }
                }
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun doExport(type: String, name: String, open: Boolean = false, id: Long?) {
        this.type = type
        viewModelScope.launch(Dispatchers.IO) {
            try {
                exportBuilder.forEach { builder ->
                    if(builder.getExtension().contains(ext)) {
                        builder.generatePath(name, ext)
                        builder.initData(contactDAO, calendarEventDAO, toDoItemDAO, authenticationDAO)
                        val path = builder.doExport(id, type, open)
                        printMessage(path, this)
                    }
                }
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }
}