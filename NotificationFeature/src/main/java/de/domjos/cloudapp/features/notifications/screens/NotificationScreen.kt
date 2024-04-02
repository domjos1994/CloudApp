package de.domjos.cloudapp.features.notifications.screens

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Scale
import de.domjos.cloudapp.appbasics.custom.NoEntryItem
import de.domjos.cloudapp.appbasics.custom.NoInternetItem
import de.domjos.cloudapp.appbasics.helper.ConnectionState
import de.domjos.cloudapp.appbasics.helper.connectivityState
import de.domjos.cloudapp.webrtc.model.notifications.Action
import de.domjos.cloudapp.webrtc.model.notifications.Notification

@Composable
fun NotificationScreen(viewModel: NotificationViewModel = hiltViewModel()) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()


    ImageLoader.Builder(LocalContext.current)
        .components {
            add(SvgDecoder.Factory())
        }
        .crossfade(true)
        .build()

    viewModel.reload()
    NotificationScreen(notifications) {
        viewModel.getFullIconLink(it)
    }
}

@Composable
fun NotificationScreen(rooms: List<Notification>, onIconLoad: (Notification) -> String) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)
        .verticalScroll(rememberScrollState())) {

        val connection by connectivityState()
        val isConnected = connection === ConnectionState.Available

        if(isConnected) {
            if(rooms.isEmpty()) {
                NoEntryItem()
            } else {
                rooms.forEach { room -> NotificationItem(room, onIconLoad)}
            }
        } else {
            NoInternetItem()
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onIconLoad: (Notification) -> String) {
    val uriHandler = LocalUriHandler.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)
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
                contentScale = ContentScale.Crop
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
                contentScale = ContentScale.Crop
            )
        }


        Column(modifier = Modifier
            .padding(5.dp)
            .weight(9f)) {
            Text(notification.subject, fontWeight= FontWeight.Bold, modifier = Modifier.padding(5.dp))
            Text(
                "${notification.app}: ${notification.message}",
                modifier = Modifier.padding(5.dp)
            )
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = MaterialTheme.colorScheme.primaryContainer)) {
        notification.actions.forEach { action ->
            Button(
                onClick = { uriHandler.openUri(action.link) },
                modifier = Modifier.padding(2.dp)) {
                Text(action.label, fontWeight = if(action.primary) FontWeight.Bold else FontWeight.Normal)
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
    NotificationScreen(listOf(fake(1), fake(2), fake(3))) { "" }
}

@Preview(showBackground = true)
@Composable
fun NotificationItemPreview() {
    NotificationItem(fake(1)) { "https://cloud.cz-dillingen.de/apps/updatenotification/img/notification.svg" }
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