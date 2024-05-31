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
import de.domjos.cloudapp2.rest.model.notifications.Notification
import de.domjos.cloudapp2.R
import de.domjos.cloudapp2.appbasics.helper.openUrl
import de.domjos.cloudapp2.receiver.AbstractWidgetReceiver
import kotlinx.serialization.json.Json

class NewsWidget : AbstractWidget<Notification>() {

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()
        val deserializedList = prefs[AbstractWidgetReceiver.currentData] ?: ""
        val itemList = if(deserializedList == "")
            emptyList()
        else
            Json.decodeFromString<List<Notification>>(deserializedList)

        Column(
            modifier =
                GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.primaryContainer),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                GlanceModifier.padding(5.dp).fillMaxWidth().wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    context.getString(R.string.widget_news),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.primary
                    )
                )
            }

            LazyColumn(GlanceModifier.fillMaxWidth().wrapContentHeight()) {
                items(itemList) {
                    Row(
                        GlanceModifier.padding(5.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .background(GlanceTheme.colors.primary)
                            .clickable {
                                openUrl(context, it.link)
                            }
                    ) {
                        Column(GlanceModifier.width(40.dp).height(40.dp).padding(5.dp)) {
                            Image(
                                getImageProvider(),
                                it.icon,
                                colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer)
                            )
                        }
                        Column(GlanceModifier.wrapContentWidth().wrapContentHeight()) {
                            Row(GlanceModifier.padding(1.dp)) {
                                Text(
                                    it.subject,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlanceTheme.colors.primaryContainer
                                    )
                                )
                            }
                            Row(GlanceModifier.padding(1.dp)) {
                                Text(
                                    it.message,
                                    style = TextStyle(
                                        fontSize = 10.sp,
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


    private fun getImageProvider(): ImageProvider {
        return ImageProvider(R.drawable.icon)
    }
}