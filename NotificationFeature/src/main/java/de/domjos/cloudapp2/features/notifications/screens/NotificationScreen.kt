package de.domjos.cloudapp2.features.notifications.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Scale
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.NoEntryItem
import de.domjos.cloudapp2.appbasics.custom.NoInternetItem
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.rest.model.notifications.Action
import de.domjos.cloudapp2.rest.model.notifications.Notification
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val connection by connectivityState()
    val isConnected = connection === ConnectionState.Available
    val context = LocalContext.current

    viewModel.message.observe(LocalLifecycleOwner.current) {
        if(it != null) {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }

    if(isConnected) {
        viewModel.reload()
    }

    NotificationScreen(notifications, {
        viewModel.getFullIconLink(it)
    }, isConnected, viewModel.hasAuthentications(), colorBackground, colorForeground, toAuths)
}

@Composable
fun NotificationScreen(rooms: List<Notification>, onIconLoad: (Notification) -> String, isConnected: Boolean, hasAuthentications: Boolean, colorBackground: Color, colorForeground: Color, toAuths: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)
        .verticalScroll(rememberScrollState())) {

        if(hasAuthentications) {
            if(isConnected) {
                if(rooms.isEmpty()) {
                    NoEntryItem(colorForeground, colorBackground)
                } else {
                    rooms.forEach { room -> NotificationItem(room, colorBackground, colorForeground, onIconLoad)}
                }
            } else {
                NoInternetItem(colorForeground, colorBackground)
            }
        } else {
            NoAuthenticationItem(colorForeground, colorBackground, toAuths)
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, colorBackground: Color, colorForeground: Color, onIconLoad: (Notification) -> String) {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colorBackground)
            .clickable {
                if (notification.link != "") {
                    uriHandler.openUri(notification.link)
                }
            }) {
        val icon = onIconLoad(notification)
        if(icon=="") {
            Image(
                Icons.Outlined.Notifications,
                notification.subject,
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(colorForeground)
            )
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .decoderFactory(SvgDecoder.Factory())
                    .data(icon)
                    .scale(Scale.FIT)
                    .build(),
                notification.subject,
                modifier = Modifier
                    .padding(5.dp)
                    .weight(1f),
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.tint(colorForeground)
            )
        }


        Column(modifier = Modifier
            .padding(5.dp)
            .weight(9f)) {
            Text(notification.subject, fontWeight= FontWeight.Bold, modifier = Modifier.padding(5.dp), color = colorForeground)
            Text(
                "${notification.app}: ${notification.message}",
                modifier = Modifier.padding(5.dp), color = colorForeground
            )
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight().background(colorBackground)) {
        notification.actions.forEach { action ->
            Button(
                onClick = { uriHandler.openUri(action.link) },
                modifier = Modifier.padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = colorForeground, contentColor = colorBackground)) {
                Text(action.label, fontWeight = if(action.primary) FontWeight.Bold else FontWeight.Normal, color = colorBackground)
            }
        }
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {}
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    NotificationScreen(listOf(fake(1), fake(2), fake(3)), { "" },
        isConnected = true, hasAuthentications = true, colorBackground = Color.Blue, colorForeground = Color.White) {}
}

@Preview(showBackground = true)
@Composable
fun NotificationItemPreview() {
    NotificationItem(fake(1), colorBackground = Color.Blue, colorForeground = Color.White) { "https://cloud.cz-dillingen.de/apps/updatenotification/img/notification.svg" }
}

private fun fake(id: Long): Notification {
    val action1 = Action("Action 1", "https://google.com", "POST", true)
    val action2 = Action("Action 2", "https://yahoo.com", "DELETE", false)

    return Notification(
        id,
        "contacts",
        "domjos",
        "2024-03-18T08:52+33:00",
        "test $id",
        "$id",
        "Test $id",
        "This is test $id!",
        "https://microsoft.com",
        "/apps/survey_client/img/app-dark.svg",
        true,
        arrayOf(action1, action2)
    )
}