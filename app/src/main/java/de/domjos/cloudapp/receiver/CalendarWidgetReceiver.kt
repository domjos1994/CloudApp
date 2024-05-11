package de.domjos.cloudapp.receiver

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.AndroidEntryPoint
import de.domjos.cloudapp.data.repository.CalendarRepository
import de.domjos.cloudapp.widgets.CalendarWidget
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class CalendarWidgetReceiver : AbstractWidgetReceiver(CalendarWidget()) {

    @Inject
    lateinit var calendarRepository: CalendarRepository

    override suspend fun observe(context: Context) {
        try {
            val glanceId =
                GlanceAppWidgetManager(context).getGlanceIds(CalendarWidget::class.java).firstOrNull()

            val today = Calendar.getInstance()
            val twoWeeks = Calendar.getInstance()
            twoWeeks.add(Calendar.DAY_OF_MONTH, 14)

            val tsStart = today.timeInMillis
            val tsEnd = twoWeeks.timeInMillis

            val events = mutableListOf<Event>()
            calendarRepository.getCalendars().forEach { calendar ->
                calendarRepository.loadData(calendar, tsStart, tsEnd).forEach { event ->
                    events.add(Event(event.title, event.description, event.location, event.calendar, event.from, event.to, event.eventId))
                }
            }

            glanceId?.let {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                    pref.toMutablePreferences().apply {
                        val data = Json.encodeToString(events)
                        this[currentData] = data
                    }
                }
                glanceAppWidget.update(context, it)
            }
        } catch (ex: Exception) {
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }
}

@Serializable
data class Event(val title: String, val description: String, val location: String, val calendar: String, val start: Long, val end: Long, val uid: String)