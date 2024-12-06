package de.domjos.cloudapp2.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
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
import de.domjos.cloudapp2.R
import de.domjos.cloudapp2.appbasics.helper.Converter
import de.domjos.cloudapp2.appbasics.helper.openEvent
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import de.domjos.cloudapp2.receiver.AbstractWidgetReceiver
import de.domjos.cloudapp2.receiver.Event
import kotlinx.serialization.json.Json

class CalendarWidget : AbstractWidget<CalendarEvent>() {
    @Composable
    override fun Content() {
        val prefs = currentState<Preferences>()
        val deserializedList = prefs[AbstractWidgetReceiver.currentData] ?: ""
        val itemList = if(deserializedList == "")
            emptyList()
        else
            Json.decodeFromString<List<Event>>(deserializedList)

        ViewContent(lst = itemList)
    }

    @Composable
    fun ViewContent(lst: List<Event>) {
        val context = LocalContext.current

        Column(
            modifier =
            GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                GlanceModifier.padding(5.dp).fillMaxWidth().wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    context.getString(R.string.widget_calendar),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GlanceTheme.colors.primary)
                )
            }

            LazyColumn(GlanceModifier.fillMaxWidth().wrapContentHeight()) {
                this.items(lst) { event ->
                    Row(
                        GlanceModifier.padding(5.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(GlanceTheme.colors.primary)
                            .clickable {
                                val uid = event.uid
                                if(uid.isNotEmpty()) {
                                    openEvent(context, uid)
                                }
                            }) {

                        Column(
                            GlanceModifier.width(40.dp).height(40.dp).padding(5.dp),
                            horizontalAlignment = Alignment.Start) {
                            Image(
                                getImageProvider(),
                                event.title,
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer))
                        }
                        Column(GlanceModifier.fillMaxWidth().wrapContentHeight()) {
                            Row(GlanceModifier.padding(1.dp)) {
                                Text(
                                    event.title,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlanceTheme.colors.primaryContainer
                                    )
                                )
                            }
                            Row(GlanceModifier.padding(1.dp).fillMaxWidth()) {
                                Column(
                                    GlanceModifier.defaultWeight(),
                                    horizontalAlignment = Alignment.Start) {
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(
                                            event.calendar,
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = GlanceTheme.colors.primaryContainer
                                            )
                                        )
                                    }
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(
                                            event.location,
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = GlanceTheme.colors.primaryContainer
                                            )
                                        )
                                    }
                                }
                                Column(
                                    GlanceModifier.defaultWeight(),
                                    horizontalAlignment = Alignment.End) {
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(
                                            Converter.toFormattedString(context, event.start, true),
                                            style = TextStyle(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = GlanceTheme.colors.primaryContainer
                                            )
                                        )
                                    }
                                    Row(GlanceModifier.padding(1.dp)) {
                                        Text(
                                            Converter.toFormattedString(context, event.end, true),
                                            style = TextStyle(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = GlanceTheme.colors.primaryContainer
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getImageProvider(): ImageProvider {
        return  ImageProvider(R.drawable.icon)
    }
}