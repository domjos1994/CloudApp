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
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.domjos.cloudapp.receiver.NewsWidgetReceiver
import de.domjos.cloudapp.webrtc.model.notifications.Notification
import kotlinx.serialization.json.Json
import de.domjos.cloudapp.R

class NewsWidget : AbstractWidget() {


    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()
        val deserializedList = prefs[NewsWidgetReceiver.currentData] ?: ""
        val items = if(deserializedList == "")
            emptyList()
        else
            Json.decodeFromString<List<Notification>>(deserializedList).subList(0, 3)

        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.background),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                GlanceModifier.padding(5.dp).fillMaxWidth().wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    context.getString(R.string.widget_news),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }

            items.forEach {
                Row(
                    GlanceModifier.padding(5.dp)
                        .fillMaxWidth()
                        .wrapContentHeight()) {
                    Column(GlanceModifier.width(40.dp).height(40.dp).padding(5.dp)) {
                        Image(getImageProvider(), it.icon)
                    }
                    Column(GlanceModifier.wrapContentWidth().wrapContentHeight()) {
                        Row(GlanceModifier.padding(1.dp)) {
                            Text(it.subject, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                        }
                        Row(GlanceModifier.padding(1.dp)) {
                            Text(it.message, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal))
                        }
                    }
                }
            }
        }
    }


    private fun getImageProvider(): ImageProvider {
        return ImageProvider(R.drawable.icon)
    }
}