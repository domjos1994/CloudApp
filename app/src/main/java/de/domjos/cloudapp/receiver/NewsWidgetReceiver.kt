package de.domjos.cloudapp.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.AndroidEntryPoint
import de.domjos.cloudapp.data.repository.NotificationsRepository
import de.domjos.cloudapp.webrtc.model.notifications.Notification
import de.domjos.cloudapp.widgets.NewsWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class NewsWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = NewsWidget()
    private val coroutineScope = MainScope()

    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        observeData(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        observeData(context)
    }

    private fun observeData(context: Context) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val glanceId =
                    GlanceAppWidgetManager(context).getGlanceIds(NewsWidget::class.java).firstOrNull()

                val items = mutableListOf<Notification>()
                notificationsRepository.reload()
                notificationsRepository.notifications.collect {
                    items.addAll(it)
                }

                glanceId?.let {
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, it) { pref ->
                        pref.toMutablePreferences().apply {
                            val data = Json.encodeToString(items)
                            this[currentData] = data
                        }
                    }
                    glanceAppWidget.update(context, it)
                }
            } catch (_: Exception) {}
        }
    }

    companion object {
        val currentData = stringPreferencesKey("currentData")
    }
}