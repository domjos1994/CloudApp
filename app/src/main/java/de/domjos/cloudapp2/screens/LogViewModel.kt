/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.database.dao.LogDAO
import de.domjos.cloudapp2.database.model.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logDAO: LogDAO
) : ViewModel() {
    private val _logs = MutableStateFlow(listOf<Log>())
    val logs: StateFlow<List<Log>> get() = _logs

    fun loadLogs(itemType: String = "", msgType: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            logDAO.getReallyAll().first()
                .filter { a ->
                    !logDAO.getAll().first().any { m -> m.id == a.id}
                }.forEach { logDAO.delete(it) }

            if(itemType.isEmpty() && msgType.isEmpty()) {
                _logs.value = logDAO.getAll().first()
            } else if(itemType.isNotEmpty()) {
                _logs.value = logDAO.getItemsByItemType(itemType).first()
            } else if(msgType.isNotEmpty()) {
                _logs.value = logDAO.getItemsByMessageType(msgType).first()
            }
        }
    }

    fun deleteLogs(itemType: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            if(itemType.isEmpty()) {
                logDAO.deleteAll()
                _logs.value = logDAO.getAll().first()
            } else {
                logDAO.deleteItemsByItemType(itemType)
                _logs.value = logDAO.getItemsByItemType(itemType).first()
            }
        }
    }
}