package de.domjos.cloudapp2.features.notifications.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import de.domjos.cloudapp2.features.notifications.screens.model.NotificationItem
import de.domjos.cloudapp2.rest.model.notifications.Action
import de.domjos.cloudapp2.rest.model.notifications.Notification
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.helper.ConnectivityViewModel
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

@Composable
fun NotificationScreen(viewModel: NotificationViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit, onChatScreen: (Int, String) -> Unit) {
    viewModel.setContext(LocalContext.current)
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val allTypes by viewModel.allTypes.collectAsStateWithLifecycle()

    ConnectivityViewModel.Init(viewModel)

    NotificationScreen(notifications, allTypes, {
        viewModel.getFullIconLink(it)
    }, viewModel.isConnected(), viewModel.hasAuthentications(), colorBackground, colorForeground, toAuths, onChatScreen)
}

@Composable
fun NotificationScreen(rooms: List<NotificationItem>, allTypes: Boolean, onIconLoad: (Notification) -> String, isConnected: Boolean, hasAuthentications: Boolean, colorBackground: Color, colorForeground: Color, toAuths: () -> Unit, onChatScreen: (Int, String) -> Unit) {
    Column(modifier = Modifier
        .fillMaxSize()) {

        var app by remember { mutableStateOf(true) }
        var server by remember { mutableStateOf(true) }

        Header(rooms, allTypes, colorBackground, colorForeground, {app = it}) {server = it}

        Row(Modifier
            .padding(5.dp)) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if(hasAuthentications) {
                    if(isConnected) {
                        if(rooms.isEmpty()) {
                            NoEntryItem(colorForeground, colorBackground)
                        } else {
                            rooms.forEach { notification ->
                                if(notification.notification != null) {
                                    if(server) {
                                        NotificationItem(notification.notification, colorBackground, colorForeground, onIconLoad, onChatScreen)
                                    }
                                } else {
                                    if(app) NotificationItem(notification, colorBackground, colorForeground)
                                }
                            }
                        }
                    } else {
                        NoInternetItem(colorForeground, colorBackground)
                    }
                } else {
                    NoAuthenticationItem(colorForeground, colorBackground, toAuths)
                }
            }
        }

    }
}

@Composable
fun Header(
    rooms: List<NotificationItem>,
    allTypes: Boolean,
    colorBackground: Color,
    colorForeground: Color,
    onAppChange: (Boolean)->Unit,
    onServerChange: (Boolean)->Unit) {
    var app by remember { mutableStateOf(true) }
    var server by remember { mutableStateOf(true) }
    val count = rooms.size
    val appCount = rooms.filter { it.type == NotificationItem.Type.App }.toList().count()
    val serverCount = rooms.filter { it.type == NotificationItem.Type.Server }.toList().count()
    val strApp = stringResource(R.string.notification_app)
    val strServer = stringResource(R.string.notification_server)
    val strWhole = stringResource(R.string.notification_whole)

    if(allTypes) {
        Row(
            Modifier
                .background(colorBackground)
                .height(50.dp)
                .fillMaxWidth()) {
            Column(
                Modifier
                    .padding(5.dp)
                    .height(50.dp)) {
                Row(Modifier.height(50.dp), verticalAlignment =  Alignment.CenterVertically) {
                    Text(strApp, Modifier.weight(3.0f), color = colorForeground, textAlign = TextAlign.Center)
                    Switch(app, onCheckedChange = {
                        app = it
                        onAppChange(app)
                    },
                        Modifier
                            .weight(2.0f)
                            .height(50.dp),
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = colorForeground,
                        checkedIconColor = colorBackground,
                        checkedThumbColor = colorBackground,
                        checkedTrackColor = colorForeground,
                        uncheckedBorderColor = colorForeground,
                        uncheckedIconColor = colorBackground,
                        uncheckedThumbColor = colorBackground,
                        uncheckedTrackColor = colorForeground
                    ))
                    Text(strServer, Modifier.weight(3.0f), color = colorForeground, textAlign = TextAlign.Center)
                    Switch(server, onCheckedChange = {
                        server = it
                        onServerChange(server)
                    },
                        Modifier
                            .weight(2.0f)
                            .height(50.dp),
                    colors = SwitchDefaults.colors(
                        checkedBorderColor = colorForeground,
                        checkedIconColor = colorBackground,
                        checkedThumbColor = colorBackground,
                        checkedTrackColor = colorForeground,
                        uncheckedBorderColor = colorForeground,
                        uncheckedIconColor = colorBackground,
                        uncheckedThumbColor = colorBackground,
                        uncheckedTrackColor = colorForeground
                    ))
                }
            }
        }
    } else {
        app = true
        server = true
        onAppChange(true)
        onServerChange(server)
    }

    Row(
        Modifier
            .background(colorBackground)
            .fillMaxWidth()) {
        Column(
            Modifier
                .padding(5.dp)
                .fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                val text = if(allTypes) {
                    "$strWhole: $count, $strApp: ${appCount}, $strServer: $serverCount"
                } else {
                    "$strWhole: $count"
                }
                Text(text, Modifier.fillMaxWidth(), color = colorForeground, textAlign = TextAlign.Center)
            }
        }
    }
    HorizontalDivider(color = colorForeground)
}

@Composable
fun NotificationItem(notification: Notification, colorBackground: Color, colorForeground: Color, onIconLoad: (Notification) -> String, onChatScreen: (Int, String) -> Unit) {
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
            .wrapContentHeight()
            .background(colorBackground)) {

        notification.actions.forEach { action ->
            val actionProcess = if(notification.app == "spreed" && notification.object_type == "chat") {
                {
                    val id = notification.object_id
                    onChatScreen(1, if(id.contains("/")) id.split("/")[0] else id)
                }
            } else {
                {uriHandler.openUri(action.link)}
            }

            Button(
                onClick = { actionProcess() },
                modifier = Modifier.padding(2.dp), colors = ButtonDefaults.buttonColors(containerColor = colorForeground, contentColor = colorBackground)) {
                Text(action.label, fontWeight = if(action.primary) FontWeight.Bold else FontWeight.Normal, color = colorBackground)
            }
        }
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {}
}

@Composable
fun NotificationItem(notification: NotificationItem, colorBackground: Color, colorForeground: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(colorBackground)) {
        notification.icon(colorForeground)


        Column(modifier = Modifier
            .padding(5.dp)
            .weight(9f)) {
            Text(notification.title, fontWeight= FontWeight.Bold, modifier = Modifier.padding(5.dp), color = colorForeground)
            Text(
                notification.description,
                modifier = Modifier.padding(5.dp), color = colorForeground
            )
        }
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)) {}
}

@Preview(showBackground = true)
@Composable
fun NotificationScreenPreview() {
    NotificationScreen(listOf(fake(1), fake(2), fake(3)), true, { "" },
        isConnected = true, hasAuthentications = true, colorBackground = Color.Blue, colorForeground = Color.White, {}) {_,_->}
}

@Preview(showBackground = true)
@Composable
fun NotificationItemPreview() {
    NotificationItem(fake(1).notification!!, colorBackground = Color.Blue, colorForeground = Color.White, { "https://cloud.cz-dillingen.de/apps/updatenotification/img/notification.svg" }) {_,_->}
}

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    CloudAppTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            Header(
                rooms = listOf(fake(1), fake(2), fake(3)),
                allTypes = true,
                colorBackground = Color.Blue,
                colorForeground = Color.White,
                onAppChange = {}) {}
        }
    }
}

private fun fake(id: Long): NotificationItem {
    val action1 = Action("Action 1", "https://google.com", "POST", true)
    val action2 = Action("Action 2", "https://yahoo.com", "DELETE", false)

    return NotificationItem(
        NotificationItem.Type.Server, Notification(
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
    ))
}