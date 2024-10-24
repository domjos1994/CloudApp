/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.activities

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.caldav.CalendarCalDav
import de.domjos.cloudapp2.cardav.CarDav
import de.domjos.cloudapp2.data.repository.AuthenticationRepository
import de.domjos.cloudapp2.data.repository.CalendarRepository
import de.domjos.cloudapp2.data.repository.ContactRepository
import de.domjos.cloudapp2.data.repository.DataRepository
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.webdav.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import javax.inject.Inject

@HiltViewModel
class ShareActivityViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val contactRepository: ContactRepository,
    private val calendarRepository: CalendarRepository,
    private val dataRepository: DataRepository
) : LogViewModel() {
    private val _calendars = MutableStateFlow<List<String>>(listOf())
    val calendars: StateFlow<List<String>> get() = _calendars
    private val _contactLists = MutableStateFlow<List<String>>(listOf())
    val contactLists: StateFlow<List<String>> get() = _contactLists
    private val _dataItems = MutableStateFlow<List<Item>>(listOf())
    val dataItems: StateFlow<List<Item>> get() = _dataItems

    fun initCalendars() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val calendars = mutableListOf<String>()
                calendarRepository.getCalendars().forEach { calendar ->
                    calendars.add(calendar.label)
                }
                _calendars.value = calendars
            } catch (ex: Exception) {
                printException(ex, _calendars)
            }
        }
    }

    fun initContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contactLists = mutableListOf<String>()
                contactRepository.loadAddressBooks(true).forEach { addressBook ->
                    contactLists.add(addressBook.label ?: addressBook.name)
                }
                _contactLists.value = contactLists
            } catch (ex: Exception) {
                printException(ex, _contactLists)
            }
        }
    }

    fun initDataItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataRepository.init()
                _dataItems.value = dataRepository.getList()
            } catch (ex: Exception) {
                printException(ex, _dataItems)
            }
        }
    }

    fun loadData(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(item.name == "..") {
                    dataRepository.back()
                } else {
                    dataRepository.openFolder(item)
                }
                _dataItems.value = dataRepository.getList()
            } catch (ex: Exception) {
                printException(ex, _dataItems)
            }
        }
    }

    fun saveContact(contactList: String, files: List<FileObject>, success: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val item = contactRepository.loadAddressBooks(true).find { it.label == contactList }
                if(item != null) {
                    val cardav = CarDav(authenticationRepository.getLoggedInUser())
                    val contacts = mutableListOf<Contact>()
                    files.forEach { file ->
                        contacts.addAll(cardav.fileToModels(file.data, item))
                    }
                    contacts.forEach { contact -> contactRepository.insertOrUpdateContact(true, contact) }
                    printMessage(success, files)
                }
            } catch (ex: Exception) {
                printException(ex, files)
            }
        }
    }

    fun saveCalendar(calendar: String, files: List<FileObject>, success: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val item = calendarRepository.getCalendars().find { it.label == calendar }
                if(item != null) {
                    val caldav = CalendarCalDav(authenticationRepository.getLoggedInUser())
                    val events = mutableListOf<CalendarEvent>()
                    files.forEach { file ->
                        events.addAll(caldav.fileToModels(file.data, item))
                    }
                    events.forEach { event -> calendarRepository.insert(event) }
                    printMessage(success, files)
                }
            } catch (ex: Exception) {
                printException(ex, files)
            }
        }
    }

    fun saveData(data: Item, files: List<FileObject>, success: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                files.forEach { file ->
                    dataRepository.openFolder(data)
                    val baStream = ByteArrayInputStream(file.data)
                    baStream.use { stream ->
                        dataRepository.createFile(file.name, stream)
                    }
                }
                printMessage(success, files)
            } catch (ex: Exception) {
                printException(ex, files)
            }
        }
    }
}