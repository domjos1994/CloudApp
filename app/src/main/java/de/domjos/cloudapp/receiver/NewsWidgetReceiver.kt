package de.domjos.cloudapp.receiver

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.AndroidEntryPoint
import de.domjos.cloudapp.data.repository.NotificationsRepository
import de.domjos.cloudapp.webrtc.model.notifications.Notification
import de.domjos.cloudapp.widgets.NewsWidget
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class NewsWidgetReceiver : AbstractWidgetReceiver(NewsWidget()) {
    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    override suspend fun observe(context: Context) {
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
                        val data = Json.encodeToString(items.subList(0, 3))
                        this[currentData] = data
                    }
                }
                glanceAppWidget.update(context, it)
            }
        } catch (_: Exception) {}
    }
}