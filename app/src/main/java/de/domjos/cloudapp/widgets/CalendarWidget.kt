package de.domjos.cloudapp.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.domjos.cloudapp.R
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import de.domjos.cloudapp.receiver.AbstractWidgetReceiver
import de.domjos.cloudapp.receiver.Event
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalendarWidget : AbstractWidget<CalendarEvent>() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()
        val deserializedList = prefs[AbstractWidgetReceiver.currentData] ?: ""
        val itemList = if(deserializedList == "")
            emptyList()
        else
            Json.decodeFromString<List<Event>>(deserializedList)

        Column(
            modifier =
            GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                GlanceModifier.padding(5.dp).fillMaxWidth().wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    context.getString(R.string.widget_calendar),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }

            LazyColumn(GlanceModifier.fillMaxWidth().wrapContentHeight()) {
                this.items(itemList) { event ->
                    Row(
                        GlanceModifier.padding(5.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable {}) {

                        Column(
                            GlanceModifier.width(40.dp).height(40.dp).padding(5.dp),
                            horizontalAlignment = Alignment.Start) {
                            Image(getImageProvider(), event.title)
                        }
                        Column(GlanceModifier.fillMaxWidth().wrapContentHeight()) {
                            Row(GlanceModifier.padding(1.dp)) {
                                Text(event.title, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                            }
                            Row(GlanceModifier.padding(1.dp).fillMaxWidth()) {
                                Column(
                                    GlanceModifier.defaultWeight(),
                                    horizontalAlignment = Alignment.Start) {
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(event.calendar, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal))
                                    }
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(event.location, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal))
                                    }
                                }
                                Column(
                                    GlanceModifier.defaultWeight(),
                                    horizontalAlignment = Alignment.End) {
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(longToDate(event.start), style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Normal))
                                    }
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(longToDate(event.end), style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Normal))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun longToDate(ts: Long): String {
        val dt = Date(ts)
        return if(dt.hours == 0 && dt.minutes == 0) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(dt)
        } else {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.format(dt)
        }
    }

    private fun getImageProvider(): ImageProvider {
        return  ImageProvider(R.drawable.icon)
    }
}