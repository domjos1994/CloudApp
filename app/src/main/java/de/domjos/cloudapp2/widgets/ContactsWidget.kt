package de.domjos.cloudapp2.widgets

import android.graphics.BitmapFactory
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
import androidx.glance.layout.wrapContentWidth
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import de.domjos.cloudapp2.R
import de.domjos.cloudapp2.appbasics.helper.openPhone
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.receiver.AbstractWidgetReceiver
import de.domjos.cloudapp2.receiver.WidgetContact
import kotlinx.serialization.json.Json


class ContactsWidget : AbstractWidget<Contact>() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()
        val deserializedList = prefs[AbstractWidgetReceiver.currentData] ?: ""
        val itemList = if(deserializedList == "")
            emptyList()
        else
            Json.decodeFromString<List<WidgetContact>>(deserializedList)

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
                    context.getString(R.string.widget_contacts),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                )
            }

            LazyColumn(GlanceModifier.fillMaxWidth().wrapContentHeight()) {
                this.items(itemList) {
                    Row(
                        GlanceModifier.padding(5.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clickable {
                                openPhone(context, it.phone)
                            }) {
                        Column(GlanceModifier.width(40.dp).height(40.dp).padding(5.dp)) {
                            Image(getImageProvider(it), it.name)
                        }
                        Column(GlanceModifier.wrapContentWidth().wrapContentHeight()) {
                            Row(GlanceModifier.padding(1.dp)) {
                                Text(it.name, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
                            }
                            Row(GlanceModifier.padding(1.dp)) {
                                Text(it.phone, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal))
                            }
                            Row(GlanceModifier.padding(1.dp)) {
                                Text(it.addressBook, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getImageProvider(contact: WidgetContact): ImageProvider {
        return if(contact.photo != null) {
            val bmp = BitmapFactory.decodeByteArray(contact.photo, 0, contact.photo.size)
            if(bmp != null) {
                ImageProvider(bmp)
            } else {
                ImageProvider(R.drawable.icon)
            }
        } else {
            ImageProvider(R.drawable.icon)
        }
    }
}