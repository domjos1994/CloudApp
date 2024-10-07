/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.features.todofeature.screens

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.data.repository.ToDoRepository
import de.domjos.cloudapp2.database.dao.ListTuple
import de.domjos.cloudapp2.database.model.todo.ToDoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoViewModel @Inject constructor(
    private val toDoRepository: ToDoRepository
) : LogViewModel() {
    private val _lists = MutableStateFlow(listOf<ListTuple>())
    val lists: StateFlow<List<ListTuple>> get() = _lists
    private val _items = MutableStateFlow(listOf<ToDoItem>())
    val items: StateFlow<List<ToDoItem>> get() = _items
    private val _selected = MutableStateFlow<ListTuple?>(null)
    val selected: StateFlow<ListTuple?> get() = _selected

    fun loadLists() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getLists()
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    private fun getLists() {
        val items = mutableListOf<ListTuple>()
        items.add(ListTuple(null, "", null))
        items.addAll(toDoRepository.getLists())
        _lists.value = items
    }

    fun hasNoAuthentications(): Boolean {
        return toDoRepository.hasNoAuths()
    }

    fun updateList(listTuple: ListTuple) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toDoRepository.updateList(listTuple)
                getLists()
                _selected.value = listTuple
                getToDos()
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun deleteList(listTuple: ListTuple) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toDoRepository.deleteList(listTuple)
                getLists()
                _selected.value = null
                getToDos()
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun import(updateProgress: (Float, String) -> Unit, progressLabel: String, finishProgress: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toDoRepository.import(_selected.value?.uid, updateProgress, progressLabel)
                loadLists()
                loadToDos()
                finishProgress()
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun select(listTuple: ListTuple?) {
        _selected.value = listTuple
        loadToDos()
    }

    fun loadToDos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getToDos()
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    private fun getToDos() {
        if(_selected.value == null) {
            _items.value = toDoRepository.getToDoItems()
        } else {
            _items.value = toDoRepository.getToDoItems(_selected.value?.uid)
        }
    }

    fun insertOrUpdateToDo(toDoItem: ToDoItem?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(toDoItem!!.id == 0L) {
                    toDoRepository.insertToDoItem(toDoItem)
                } else {
                    toDoRepository.updateToDoItem(toDoItem)
                }
                getToDos()
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun deleteToDo(toDoItem: ToDoItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                toDoRepository.deleteToDoItem(toDoItem)
                getToDos()
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }
}