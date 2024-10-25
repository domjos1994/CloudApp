package de.domjos.cloudapp2.activities

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PermDeviceInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.appbasics.R
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Scale
import dagger.hilt.android.AndroidEntryPoint
import de.dojodev.cloudapp2.features.exportfeature.screens.ExportScreen
import de.domjos.cloudapp2.adapter.FileSyncAdapter
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.ConnectionType
import de.domjos.cloudapp2.appbasics.helper.Notifications
import de.domjos.cloudapp2.appbasics.helper.ProgressDialog
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.appbasics.helper.connectivityType
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.screens.SettingsScreen
import de.domjos.cloudapp2.features.calendars.screens.CalendarScreen
import de.domjos.cloudapp2.features.calendars.screens.importCalendarAction
import de.domjos.cloudapp2.features.chats.screens.ChatScreen
import de.domjos.cloudapp2.features.chats.screens.RoomScreen
import de.domjos.cloudapp2.features.contacts.screens.ContactScreen
import de.domjos.cloudapp2.features.contacts.screens.importContactAction
import de.domjos.cloudapp2.features.data.screens.DataScreen
import de.domjos.cloudapp2.features.notesfeature.NotesScreen
import de.domjos.cloudapp2.features.notifications.screens.NotificationScreen
import de.domjos.cloudapp2.features.todofeature.screens.ToDoScreen
import de.domjos.cloudapp2.features.todofeature.screens.importToDoAction
import de.domjos.cloudapp2.screens.AuthenticationScreen
import de.domjos.cloudapp2.screens.LogScreen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import de.domjos.cloudapp2.screens.PermissionScreen
import de.domjos.cloudapp2.services.AuthenticatorService
import de.domjos.cloudapp2.widgets.CalendarWidget
import de.domjos.cloudapp2.widgets.ContactsWidget
import de.domjos.cloudapp2.widgets.NewsWidget
import de.domjos.cloudapp2.worker.CalendarWorker
import de.domjos.cloudapp2.worker.ContactWorker
import java.util.Locale


data class TabBarItem(
    val id: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null,
    val header: String = "",
    var visible: Boolean = true
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val ACCOUNT = "de.domjos.cloudapp2.account"
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create splash-Screen
        installSplashScreen()

        setContent {

            // create tabs
            val notificationsTab = TabBarItem(id="notifications", title = stringResource(id = R.string.notifications), selectedIcon = Icons.Filled.Notifications, unselectedIcon = Icons.Outlined.Notifications)
            val dataTab = TabBarItem(id="data", title = stringResource(id = R.string.data), selectedIcon = Icons.AutoMirrored.Filled.List, unselectedIcon = Icons.AutoMirrored.Outlined.List)
            val notesTab = TabBarItem(id="notes", title = stringResource(id = R.string.notes), selectedIcon = Icons.Filled.Create, unselectedIcon = Icons.Outlined.Create)
            val calendarsTab = TabBarItem(id="calendars", title = stringResource(id = R.string.calendars), selectedIcon = Icons.Filled.DateRange, unselectedIcon = Icons.Outlined.DateRange)
            val contactsTab = TabBarItem(id="contacts", title = stringResource(id = R.string.contacts), selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
            val todosTab = TabBarItem(id="todos", title = stringResource(R.string.todos), selectedIcon = Icons.Filled.Check, unselectedIcon = Icons.Outlined.Check, header = stringResource(R.string.todos))
            val roomTab = TabBarItem(id="rooms", title = stringResource(id = R.string.chats_room), selectedIcon = Icons.AutoMirrored.Filled.Message, unselectedIcon = Icons.AutoMirrored.Outlined.Message, header = stringResource(id = R.string.chats))
            val chatsTab = TabBarItem(id="chats", title = stringResource(id = R.string.chats), selectedIcon = Icons.AutoMirrored.Filled.Message, unselectedIcon = Icons.AutoMirrored.Outlined.Message)

            // creating a list of all the tabs
            var tabBarItems = remember { mutableListOf(notificationsTab, dataTab, notesTab, calendarsTab, contactsTab, todosTab, roomTab) }
            val authentications = stringResource(id = R.string.login_authentications)
            val settings = stringResource(id = R.string.settings)
            val permissions = stringResource(R.string.permissions)
            val export = stringResource(R.string.export)

            // creating our navController
            val navController = rememberNavController()

            // create params
            var title by rememberSaveable { mutableStateOf("") }
            var header by rememberSaveable { mutableStateOf("") }
            var refreshVisible by rememberSaveable { mutableStateOf(false) }
            var progress by remember { mutableStateOf<((updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit)-> Unit)?>(null) }
            val context = LocalContext.current
            var notification: NotificationCompat.Builder?
            val import = String.format(stringResource(R.string.import_loading), "").trim()
            val viewModel: MainActivityViewModel = hiltViewModel()
            val tmpBackground = MaterialTheme.colorScheme.primaryContainer
            val tmpForeground = MaterialTheme.colorScheme.primary
            var colorBackground by remember { mutableStateOf(tmpBackground) }
            var colorForeground by remember { mutableStateOf(tmpForeground) }
            var icon by remember { mutableStateOf<ByteArray?>(null) }
            var authTitle by remember { mutableStateOf("") }
            var breadcrumb by remember { mutableStateOf("") }
            val connection by connectivityState()
            val connectionType by connectivityType()
            val isConnected = connection === ConnectionState.Available
            val isWifi = connectionType === ConnectionType.Wifi
            var hasAuthentications by remember { mutableStateOf(viewModel.hasAuthentications()) }
            val toAuths = {navController.navigate(authentications)}
            val toPermissions = {navController.navigate(permissions)}
            val tabBarVisible = remember { mutableStateOf(true) }
            val back = {
                navController.navigate(notificationsTab.title)
                tabBarVisible.value = true
            }

            viewModel.message.observe(LocalLifecycleOwner.current) { msg ->
                msg?.let {
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    viewModel.message.value = null
                }
            }

            // updates the theme if connection and so on
            val updateTheme: (Authentication?) -> Unit = {auth: Authentication? ->
                viewModel.getCloudTheme {
                    viewModel.getCloudThemeMobile { mobile ->
                        val isMobile = mobile || isWifi

                        if(isConnected && it && isMobile) {
                            viewModel.getCapabilities({ data ->
                                if(data != null) {
                                    colorBackground = Color(android.graphics.Color.parseColor(data.colorBackground))
                                    colorForeground = Color(android.graphics.Color.parseColor(data.colorForeground))
                                    icon = data.thumbNail
                                    authTitle = "(${data.thUrl})"
                                    hasAuthentications = viewModel.hasAuthentications()
                                }
                            }, auth)
                        } else {
                            colorBackground = tmpBackground
                            colorForeground = tmpForeground
                            icon = null
                            authTitle = ""
                        }
                    }
                }
            }

            val initWorker: () -> Unit = {
                // initiates the worker to sync data from dav-server
                try {
                    var contactPeriod = 0.0F
                    var contactFlexPeriod = 0.0F
                    var calendarPeriod = 0.0F
                    var calendarFlexPeriod = 0.0F
//                    var chatPeriod = 0.0F
//                    var chatFlexPeriod = 0.0F


                    viewModel.getContactWorkerPeriod {
                        contactPeriod = it * 60F * 1000F
                        contactFlexPeriod = it * 120F * 1000F
                    }
                    viewModel.getCalendarWorkerPeriod {
                        calendarPeriod = it * 60F * 1000F
                        calendarFlexPeriod = it * 120F * 1000F
                    }
                    /*viewModel.getChatWorkerPeriod {
                        chatPeriod = it * 60F * 1000F
                        chatFlexPeriod = it * 120F * 1000F
                    }*/

                    val manager = WorkManager.getInstance(context)
                    val conWorker = viewModel.createWorkRequest(contactPeriod, contactFlexPeriod, ContactWorker::class.java)
                    val fConWorker = OneTimeWorkRequestBuilder<ContactWorker>().build()
                    if(conWorker != null) {
                        manager.enqueue(conWorker)
                    }

                    val calWorker = viewModel.createWorkRequest(calendarPeriod, calendarFlexPeriod, CalendarWorker::class.java)
                    val fCalWorker = OneTimeWorkRequestBuilder<CalendarWorker>().build()
                    if(calWorker != null) {
                        manager.enqueue(calWorker)
                    }

//                    val chatWorker = viewModel.createWorkRequest(chatPeriod, chatFlexPeriod, ChatWorker::class.java)
//                    val fChatWorker = OneTimeWorkRequestBuilder<ChatWorker>().build()
//                    if(chatWorker != null) {
//                        manager.enqueue(chatWorker)
//                    }
                    manager.beginWith(fCalWorker).then(fConWorker).enqueue()
                } catch (ex: Exception) {
                    viewModel.message.postValue(ex.message)
                }
            }

            val initSyncAdapter: () -> Unit = {
                try {
                    var contactPollFrequency = 0L
                    viewModel.getContactSyncPeriod {
                        contactPollFrequency = if(it.toLong() == 0L) {
                            15 * 60L
                        } else {
                            it.toLong() * 60L
                        }
                    }
                    val account = AuthenticatorService.getAccount(context, ACCOUNT)
                    ContentResolver.removePeriodicSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
                    ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
                    ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY, contactPollFrequency)
                    ContentResolver.requestSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
                } catch (ex: Exception) {
                    viewModel.message.postValue(ex.message)
                }

                try {
                    val extras = Bundle().apply {
                        putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                        putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                    }
                    var contactPollFrequency = 0L
                    viewModel.getCalendarSyncPeriod {
                        contactPollFrequency = if(it.toLong() == 0L) {
                            15 * 60L
                        } else {
                            it.toLong() * 60L
                        }
                    }
                    val account = AuthenticatorService.getAccount(context, ACCOUNT)
                    ContentResolver.removePeriodicSync(account, CalendarContract.AUTHORITY, Bundle.EMPTY)
                    ContentResolver.setSyncAutomatically(account, CalendarContract.AUTHORITY, true)
                    ContentResolver.addPeriodicSync(account, CalendarContract.AUTHORITY, Bundle.EMPTY, contactPollFrequency)
                    ContentResolver.requestSync(account, CalendarContract.AUTHORITY, extras)
                } catch (ex: Exception) {
                    viewModel.message.postValue(ex.message)
                }
                try {
                    val account = AuthenticatorService.getAccount(context, ACCOUNT)
                    ContentResolver.removePeriodicSync(account, FileSyncAdapter.AUTHORITY, Bundle.EMPTY)
                    ContentResolver.setSyncAutomatically(account, FileSyncAdapter.AUTHORITY, true)
                    ContentResolver.requestSync(account, FileSyncAdapter.AUTHORITY, Bundle.EMPTY)
                } catch (ex: Exception) {
                    viewModel.message.postValue(ex.message)
                }
            }

            val updateNavBar: () -> Unit = {
                viewModel.setVisibility(tabBarItems) { items -> tabBarItems = items }
            }

            val updateWidgets: () -> Unit = {
                try {
                    // updates the widgets
                    viewModel.updateWidget(NewsWidget(), context)
                    viewModel.updateWidget(CalendarWidget(), context)
                    viewModel.updateWidget(ContactsWidget(), context)
                } catch (ex: Exception) {
                    viewModel.message.postValue(ex.message)
                }
            }


            LaunchedEffect(isConnected) {
                updateTheme(null)
                initWorker()
                initSyncAdapter()
                updateNavBar()
                updateWidgets()
            }

            CloudAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Scaffold(
                        bottomBar = { TabView(tabBarItems, navController, tabBarVisible, updateNavBar) },
                        topBar = {
                            Column {
                                Row {
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
                                               if(breadcrumb == "") {
                                                   Column(
                                                       Modifier.padding(2.dp),
                                                       verticalArrangement = Arrangement.Bottom
                                                   ) {
                                                       Text(authTitle, fontSize = 12.sp)
                                                   }
                                               } else {
                                                   Column(
                                                       Modifier.padding(2.dp),
                                                       verticalArrangement = Arrangement.Bottom
                                                   ) {
                                                       Text("$authTitle ($breadcrumb)", fontSize = 10.sp)
                                                   }
                                               }
                                           }
                                       },
                                       actions = {
                                           if(refreshVisible && isConnected && hasAuthentications) {
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
                                                               Notifications.updateNotification(context, progress, notification!!)
                                                           }
                                                       }, {
                                                           showDialog = false
                                                           Notifications.deleteNotification(context)
                                                       })
                                                   }
                                               }) {
                                                   Icon(imageVector = Icons.Outlined.Refresh, import, tint = colorForeground)
                                               }

                                               if(showDialog) {
                                                   ProgressDialog(
                                                       onShowDialog = {showDialog=it},
                                                       currentText = currentText,
                                                       currentProgress = currentProgress,
                                                       foregroundColor = colorForeground,
                                                       backgroundColor = colorBackground
                                                   )
                                               }

                                           } else if(refreshVisible && hasAuthentications) {
                                               Icon(Icons.Filled.CloudOff, import, tint = colorForeground)
                                           }
                                           IconButton(onClick = {
                                               navController.navigate(authentications)
                                           }) {
                                               if(icon == null) {
                                                   Icon(
                                                       imageVector = Icons.Filled.Person,
                                                       contentDescription = stringResource(R.string.login),
                                                       tint = colorForeground
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
                                                   tint = colorForeground
                                               )
                                           }
                                           if(menuExpanded) {
                                               Menu(
                                                   {menuExpanded = it},
                                                   updateTheme, updateNavBar,
                                                   true,
                                                   {navController.navigate(settings)},
                                                   {navController.navigate(permissions)},
                                                   {navController.navigate(export)},
                                                   {navController.navigate("log")})
                                           }
                                       }
                                   )
                                }
                                Row {
                                    HorizontalDivider(color = colorForeground)
                                }
                            }
                        }
                    ) {
                        NavHost(modifier = Modifier.padding(top = it.calculateTopPadding(), bottom = it.calculateBottomPadding()), navController = navController, startDestination = notificationsTab.title) {
                            composable(authentications) {
                                AuthenticationScreen(colorForeground = colorForeground, colorBackground = colorBackground, onSelectedChange = updateTheme)
                                title = authentications
                                header = authentications
                                refreshVisible = false
                                breadcrumb = ""
                            }
                            composable(notificationsTab.title) {
                                if(viewModel.getFirstStart()) {
                                    PermissionScreen {
                                        viewModel.saveFirstStart()
                                        back()
                                    }
                                    title = permissions
                                    header = permissions
                                    refreshVisible = false
                                    tabBarVisible.value = false
                                    breadcrumb = ""
                                } else {
                                    NotificationScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground, onChatScreen = { x, y ->
                                        navController.navigate("android-app://androidx.navigation/Chats/$x/$y".toUri())
                                    })
                                    title = notificationsTab.title
                                    header = notificationsTab.title
                                    refreshVisible = false
                                    tabBarVisible.value = true
                                    breadcrumb = ""
                                }
                            }
                            composable(dataTab.title) {
                                DataScreen(
                                    toAuths = toAuths,
                                    colorBackground = colorBackground,
                                    colorForeground = colorForeground,
                                    onBreadCrumbChange = {text -> breadcrumb = text}
                                )

                                title = dataTab.title
                                header = dataTab.title
                                refreshVisible = false
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(notesTab.title) {
                                NotesScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = notesTab.title
                                header = notesTab.title
                                refreshVisible = false
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(calendarsTab.title) {
                                CalendarScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = calendarsTab.title
                                header = calendarsTab.title
                                refreshVisible = true
                                progress = importCalendarAction()
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(contactsTab.title) {
                                ContactScreen(
                                    toAuths = toAuths,
                                    toPermissions = toPermissions,
                                    toChat = {m -> navController.navigate("${chatsTab.id}/0/${m}")},
                                    colorBackground = colorBackground,
                                    colorForeground = colorForeground
                                )
                                title = contactsTab.title
                                header = contactsTab.title
                                refreshVisible = true
                                progress = importContactAction(hasInternet = true)
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(todosTab.title) {
                                ToDoScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = todosTab.title
                                header = todosTab.title
                                refreshVisible = true
                                progress = importToDoAction()
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(roomTab.title) {
                                RoomScreen(onChatScreen = { x, y ->
                                    navController.navigate("${chatsTab.id}/$x/$y")
                                }, toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = chatsTab.title
                                header = roomTab.title
                                refreshVisible = false
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(
                                "${chatsTab.id}/{lookIntoFuture}/{token}",
                                    arguments = listOf(
                                        navArgument("lookIntoFuture") { type = NavType.IntType },
                                        navArgument("token") { type = NavType.StringType }
                                    )
                                ) { stack ->
                                val x = stack.arguments?.getInt("lookIntoFuture")!!
                                val y = stack.arguments?.getString("token")!!
                                ChatScreen(lookIntoFuture = x, token = y, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = chatsTab.title
                                header = chatsTab.title
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(settings) {
                                title = settings
                                header = settings
                                SettingsScreen()
                                tabBarVisible.value = true
                                breadcrumb = ""
                            }
                            composable(permissions) {
                                title = permissions
                                header = permissions
                                tabBarVisible.value = false
                                PermissionScreen { back() }
                                breadcrumb = ""
                            }
                            composable(export) {
                                title = export
                                header = export
                                ExportScreen(
                                    colorBackground = colorBackground,
                                    colorForeground = colorForeground
                                )
                                breadcrumb = ""
                            }
                            composable("log") {
                                title = "Log"
                                header = "Log"
                                LogScreen(
                                    colorBackground = colorBackground,
                                    colorForeground = colorForeground
                                )
                                breadcrumb = ""
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Menu(onExpanded: (Boolean) -> Unit, updateTheme: (Authentication?) -> Unit, updateNavBar: () -> Unit, expanded: Boolean, onSettings: () -> Unit, onPermissions: () -> Unit, onExport: () -> Unit, onLog: () -> Unit) {
    val context = LocalContext.current
    DropdownMenu(expanded = expanded, onDismissRequest = { onExpanded(false) }) {
        DropdownMenuItem(text = {
            Row {
                Column(Modifier.weight(1f)) {
                    Icon(Icons.Default.ImportExport, stringResource(R.string.export))
                }
                Column(Modifier.weight(9f)) {
                    Text(stringResource(R.string.export))
                }
            }
        }, onClick = {
            updateTheme(null)
            updateNavBar()
            onExport()
            onExpanded(false)
        })
        DropdownMenuItem(text = {
            Row {
                Column(Modifier.weight(1f)) {
                    Icon(Icons.Default.Settings, stringResource(R.string.settings))
                }
                Column(Modifier.weight(9f)) {
                    Text(stringResource(R.string.settings))
                }
            }
        }, onClick = {
            updateTheme(null)
            updateNavBar()
            onSettings()
            onExpanded(false)
        })
        HorizontalDivider()
        DropdownMenuItem(text = {
            Row {
                Column(Modifier.weight(1f)) {
                    Icon(Icons.Default.PermDeviceInformation, stringResource(R.string.permissions))
                }
                Column(Modifier.weight(9f)) {
                    Text(stringResource(R.string.permissions))
                }
            }
        }, onClick = {
            updateTheme(null)
            updateNavBar()
            onPermissions()
            onExpanded(false)
        })
        DropdownMenuItem(text = {
            Row {
                Column(Modifier.weight(1f)) {
                    Icon(Icons.Default.Sync, stringResource(de.domjos.cloudapp2.R.string.log))
                }
                Column(Modifier.weight(9f)) {
                    Text(stringResource(de.domjos.cloudapp2.R.string.log))
                }
            }
        }, onClick = {
            onLog()
        })
        HorizontalDivider()
        DropdownMenuItem(text = {
            Row {
                Column(Modifier.weight(1f)) {
                    Icon(Icons.Default.Star, stringResource(de.domjos.cloudapp2.R.string.donate))
                }
                Column(Modifier.weight(9f)) {
                    Text(stringResource(de.domjos.cloudapp2.R.string.donate))
                }
            }
        }, onClick = {
            try {
                openUrl("https://github.com/sponsors/domjos1994", context)
                updateNavBar()
                onExpanded(false)
            } catch (_: Exception) {}
        })
        DropdownMenuItem(text = {
            Row {
                Column(Modifier.weight(1f)) {
                    Icon(Icons.AutoMirrored.Outlined.Help, stringResource(R.string.documentations))
                }
                Column(Modifier.weight(9f)) {
                    Text(stringResource(R.string.documentations))
                }
            }
        }, onClick = {
            try {
                if(Locale.getDefault() == Locale.GERMANY) {
                    openUrl("https://dojodev.de/de/apps/cloudapp/dokumentation", context)
                } else run {
                    openUrl("https://dojodev.de/apps/cloudapp/documentation", context)
                }
                updateNavBar()
                onExpanded(false)
            } catch (_: Exception) {}
        })
    }
}

private fun openUrl(url: String, context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

@Composable
fun TabView(tabBarItems: List<TabBarItem>, navController: NavController, visible: MutableState<Boolean>, updateNavBar: () -> Unit) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    var height by remember { mutableStateOf(80.dp) }
    var showText by remember { mutableStateOf(true) }
    if(LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        height = 45.dp
        showText = false
    }

    if(visible.value) {
        NavigationBar(modifier = Modifier.height(height)) {
            // looping over each tab to generate the views and navigation for each item
            tabBarItems.forEachIndexed { index, tabBarItem ->
                if(tabBarItem.visible) {
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = {
                            updateNavBar()
                            selectedTabIndex = index
                            navController.navigate(tabBarItem.title)
                        },
                        icon = {
                            TabBarIconView(
                                isSelected = selectedTabIndex == index,
                                selectedIcon = tabBarItem.selectedIcon,
                                unselectedIcon = tabBarItem.unselectedIcon,
                                title = if(tabBarItem.header=="") tabBarItem.title else tabBarItem.header,
                                badgeAmount = tabBarItem.badgeAmount
                            )
                        },
                        label = {
                            if(showText && tabBarItems.filter { it.visible }.size < 7) {
                                Text(
                                    if (tabBarItem.header == "") tabBarItem.title else tabBarItem.header,
                                    fontSize = 10.sp
                                )
                            }
                        })
                }
            }
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
    BadgedBox(
        badge = { TabBarBadgeView(badgeAmount) }) {
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


@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewScreenSizes
@Composable
fun TabBarPreview() {
    val items = mutableListOf<TabBarItem>()
    items.add(TabBarItem("Test 1", "Test 1", Icons.Filled.Person, Icons.Filled.Person, null, "Test 1"))
    items.add(TabBarItem("Test 2", "Test 2", Icons.Filled.Person, Icons.Filled.Person, null, "Test 2"))
    items.add(TabBarItem("Test 3", "Test 3", Icons.Filled.Person, Icons.Filled.Person, null, "Test 3"))
    items.add(TabBarItem("Test 4", "Test 4", Icons.Filled.Person, Icons.Filled.Person, null, "Test 4"))
    items.add(TabBarItem("Test 5", "Test 5", Icons.Filled.Person, Icons.Filled.Person, null, "Test 5"))

    CloudAppTheme {
        TabView(tabBarItems = items, navController = rememberNavController(), visible = mutableStateOf(true)) {}
    }
}