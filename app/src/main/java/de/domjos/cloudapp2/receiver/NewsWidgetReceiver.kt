package de.domjos.cloudapp2.receiver

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dagger.hilt.android.AndroidEntryPoint
import de.domjos.cloudapp2.data.repository.NotificationsRepository
import de.domjos.cloudapp2.rest.model.notifications.Notification
import de.domjos.cloudapp2.widgets.NewsWidget
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
        } catch (ex: Exception) {
            Log.e(this.javaClass.name, ex.message, ex)
        }
    }
}