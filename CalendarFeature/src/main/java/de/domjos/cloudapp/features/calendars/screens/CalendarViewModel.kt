package de.domjos.cloudapp.features.calendars.screens

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.CalendarRepository
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    private val _events = MutableStateFlow(listOf<CalendarEvent>())
    val events: StateFlow<List<CalendarEvent>> get() = _events
    private val _days = MutableStateFlow(listOf<Int>())
    val days: StateFlow<List<Int>> get() = _days
    private val _calendars = MutableStateFlow(listOf<String>())
    val calendars: StateFlow<List<String>> get() = _calendars
    val message = MutableLiveData<String?>()

    fun load(calendar: String, startTime: Long, endTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _events.value = calendarRepository.loadData(calendar, startTime, endTime)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun getCalendars() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _calendars.value = calendarRepository.getCalendars()
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun count(calendar: String, event: Calendar) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _days.value = calendarRepository.countData(calendar, event)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun reload(updateProgress: (Float, String) -> Unit, onFinish: ()->Unit, progressLabel: String, saveLabel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                calendarRepository.reload(updateProgress, progressLabel, saveLabel)
                onFinish()
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun insertCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                calendarRepository.insert(calendarEvent)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun deleteCalendar(calendarEvent: CalendarEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                calendarRepository.delete(calendarEvent)
            } catch (ex: Exception) {
                message.postValue(ex.message)
                Log.e(this.javaClass.name, ex.message, ex)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        return calendarRepository.hasAuthentications()
    }
}