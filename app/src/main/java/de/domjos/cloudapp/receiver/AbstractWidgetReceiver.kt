package de.domjos.cloudapp.receiver

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
abstract class AbstractWidgetReceiver(private val widget: GlanceAppWidget) : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget get() = widget
    private val coroutineScope = MainScope()

    abstract suspend fun observe(context: Context)

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
            observe(context)
        }
    }

    companion object {
        val currentData = stringPreferencesKey("currentData")
    }
}