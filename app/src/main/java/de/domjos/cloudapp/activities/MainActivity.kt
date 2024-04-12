package de.domjos.cloudapp.activities

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp.appbasics.R
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Scale
import dagger.hilt.android.AndroidEntryPoint
import de.domjos.cloudapp.appbasics.helper.Notifications
import de.domjos.cloudapp.screens.SettingsScreen
import de.domjos.cloudapp.features.calendars.screens.CalendarScreen
import de.domjos.cloudapp.features.calendars.screens.importCalendarAction
import de.domjos.cloudapp.features.chats.screens.ChatScreen
import de.domjos.cloudapp.features.chats.screens.RoomScreen
import de.domjos.cloudapp.features.contacts.screens.ContactScreen
import de.domjos.cloudapp.features.contacts.screens.importContactAction
import de.domjos.cloudapp.features.data.screens.DataScreen
import de.domjos.cloudapp.features.notifications.screens.NotificationScreen
import de.domjos.cloudapp.screens.AuthenticationScreen

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channel = NotificationChannel("cloud_app_notifications", "CloudApp", NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        setContent {
            val notificationsTab = TabBarItem(title = stringResource(id = R.string.notifications), selectedIcon = Icons.Filled.Notifications, unselectedIcon = Icons.Outlined.Notifications)
            val dataTab = TabBarItem(title = stringResource(id = R.string.data), selectedIcon = Icons.AutoMirrored.Filled.List, unselectedIcon = Icons.AutoMirrored.Outlined.List)
            val calendarsTab = TabBarItem(title = stringResource(id = R.string.calendars), selectedIcon = Icons.Filled.DateRange, unselectedIcon = Icons.Outlined.DateRange)
            val contactsTab = TabBarItem(title = stringResource(id = R.string.contacts), selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
            val roomTab = TabBarItem(title = stringResource(id = R.string.chats_room), selectedIcon = Icons.Filled.AccountBox, unselectedIcon = Icons.Outlined.AccountBox)
            val chatsTab = TabBarItem(title = stringResource(id = R.string.chats), selectedIcon = Icons.Filled.AccountBox, unselectedIcon = Icons.Outlined.AccountBox)

            // creating a list of all the tabs
            val tabBarItems = listOf(notificationsTab, dataTab, calendarsTab, contactsTab, roomTab)
            val authentications = stringResource(id = R.string.login_authentications)
            val settings = stringResource(id = R.string.settings)

            // creating our navController
            val navController = rememberNavController()

            var title by rememberSaveable { mutableStateOf("") }
            var header by rememberSaveable { mutableStateOf("") }
            var refreshVisible by rememberSaveable { mutableStateOf(false) }
            var progress by remember { mutableStateOf<((updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit)-> Unit)?>(null) }
            val context = LocalContext.current
            var notification: NotificationCompat.Builder?
            val import = stringResource(R.string.calendar_import)
            val viewModel: MainActivityViewModel = hiltViewModel()
            val tmpBackground = MaterialTheme.colorScheme.primaryContainer
            val tmpForeground = MaterialTheme.colorScheme.primary
            var colorBackground by remember { mutableStateOf(tmpBackground) }
            var colorForeground by remember { mutableStateOf(tmpForeground) }
            var icon by remember { mutableStateOf("") }
            var authTitle by remember { mutableStateOf("") }

            CloudAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(bottomBar = { TabView(tabBarItems, navController) }, topBar = {
                        viewModel.getCapabilities({
                            if(it != null) {
                                colorBackground = Color(android.graphics.Color.parseColor(it.capabilities.theming.background))
                                colorForeground = Color(android.graphics.Color.parseColor(it.capabilities.theming.`color-text`))
                                icon = it.capabilities.theming.logo
                                authTitle = "(${it.capabilities.theming.url})"
                            }
                        }, null)


                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = colorBackground,
                                titleContentColor = colorForeground,
                            ),
                            title={
                                Row {
                                    Column {
                                        Text(header)
                                    }
                                    Column(Modifier.padding(2.dp), verticalArrangement = Arrangement.Bottom) {
                                        Text(authTitle, fontSize = 12.sp)
                                    }
                                }
                            },
                            actions = {
                        if(refreshVisible) {
                            var showDialog by rememberSaveable { mutableStateOf(false) }
                            var currentProgress by remember { mutableFloatStateOf(0f) }
                            var currentText by remember { mutableStateOf("") }

                            IconButton(onClick = {
                                showDialog = true
                                notification = Notifications.showBasicNotification(context, import, "")
                                progress?.let {
                                    it({ progress, text ->
                                        currentProgress = progress
                                        currentText = text
                                        if(notification != null) {
                                            Notifications.updateNotification(context, progress, text, notification!!)
                                        }
                                    }, {
                                        showDialog = false
                                        Notifications.deleteNotification(context)
                                    })
                                }
                            }) {
                                Icon(imageVector = Icons.Outlined.Refresh, import)
                            }

                            if(showDialog) {
                                ProgressDialog(
                                    onShowDialog = {showDialog=it},
                                    currentText = currentText,
                                    currentProgress = currentProgress
                                )
                            }

                        }
                        IconButton(onClick = {
                            navController.navigate(authentications)
                        }) {
                            if(icon == "") {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = stringResource(R.string.login)
                                )
                            } else {
                                AsyncImage(model = ImageRequest.Builder(LocalContext.current)
                                    .decoderFactory(SvgDecoder.Factory())
                                    .data(icon)
                                    .scale(Scale.FIT)
                                    .build(), contentDescription = stringResource(R.string.login))
                            }
                        }

                        var menuExpanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { menuExpanded = !menuExpanded }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More",
                            )
                        }
                        if(menuExpanded) {
                            Menu({menuExpanded = it}, true, {navController.navigate(settings)})
                        }
                    })}) {
                        NavHost(modifier = Modifier.padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding()), navController = navController, startDestination = notificationsTab.title) {
                            composable(authentications) {
                                AuthenticationScreen() {auth ->
                                    viewModel.getCapabilities({ data ->
                                        if(data != null) {
                                            colorBackground = Color(android.graphics.Color.parseColor(data.capabilities.theming.background))
                                            colorForeground = Color(android.graphics.Color.parseColor(data.capabilities.theming.`color-text`))
                                            icon = data.capabilities.theming.logo
                                            authTitle = "(${data.capabilities.theming.url})"
                                        }
                                    }, auth)
                                }
                                title = authentications
                                header = authentications
                                refreshVisible = false
                            }
                            composable(notificationsTab.title) {
                                NotificationScreen()
                                title = notificationsTab.title
                                header = notificationsTab.title
                                refreshVisible = false
                            }
                            composable(dataTab.title) {
                                DataScreen()
                                title = dataTab.title
                                header = dataTab.title
                                refreshVisible = false
                            }
                            composable(calendarsTab.title) {
                                CalendarScreen()
                                title = calendarsTab.title
                                header = calendarsTab.title
                                refreshVisible = true
                                progress = importCalendarAction()
                            }
                            composable(contactsTab.title) {
                                ContactScreen()
                                title = contactsTab.title
                                header = contactsTab.title
                                refreshVisible = true
                                progress = importContactAction()
                            }
                            composable(roomTab.title) {
                                RoomScreen(onChatScreen = { x, y ->
                                    navController.navigate("android-app://androidx.navigation/Chats/$x/$y".toUri())
                                })
                                title = chatsTab.title
                                header = roomTab.title
                                refreshVisible = false
                            }
                            composable(
                                "${chatsTab.title}/{lookIntoFuture}/{token}",
                                    arguments = listOf(
                                        navArgument("lookIntoFuture") { type = NavType.IntType },
                                        navArgument("token") { type = NavType.StringType }
                                    )
                                ) { stack ->
                                val x = stack.arguments?.getInt("lookIntoFuture")!!
                                val y = stack.arguments?.getString("token")!!
                                ChatScreen(lookIntoFuture = x, token = y)
                                title = chatsTab.title
                                header = chatsTab.title
                            }
                            composable(settings) {
                                title = settings
                                header = settings
                                SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressDialog(onShowDialog: (Boolean) -> Unit, currentText: String, currentProgress: Float) {
    Dialog(
        onDismissRequest = {onShowDialog(false)},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(5.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)) {
                Row(
                    Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(currentText, fontWeight = FontWeight.Bold)
                }
                Row(Modifier.height(150.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { currentProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .padding(5.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun Menu(onExpanded: (Boolean) -> Unit, expanded: Boolean, onSettings: () -> Unit) {
    DropdownMenu(expanded = expanded, onDismissRequest = { onExpanded(false) }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.settings)) }, onClick = { onSettings() })
    }
}

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    NavigationBar {
        // looping over each tab to generate the views and navigation for each item
        tabBarItems.forEachIndexed { index, tabBarItem ->
            NavigationBarItem(
                selected = selectedTabIndex == index,
                onClick = {
                    selectedTabIndex = index
                    navController.navigate(tabBarItem.title)
                },
                icon = {
                    TabBarIconView(
                        isSelected = selectedTabIndex == index,
                        selectedIcon = tabBarItem.selectedIcon,
                        unselectedIcon = tabBarItem.unselectedIcon,
                        title = tabBarItem.title,
                        badgeAmount = tabBarItem.badgeAmount
                    )
                },
                label = {Text(tabBarItem.title)})
        }
    }
}

@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String,
    badgeAmount: Int? = null
) {
    BadgedBox(badge = { TabBarBadgeView(badgeAmount) }) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}

@Composable
fun TabBarBadgeView(count: Int? = null) {
    if (count != null) {
        Badge {
            Text(count.toString())
        }
    }
}


@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_MASK)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED)
@Composable
fun ProgressDialogPreview() {
    CloudAppTheme {
        ProgressDialog(onShowDialog = {}, currentText = "Test", currentProgress = 0.5f)
    }
}

