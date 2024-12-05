package de.domjos.cloudapp2.activities

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
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
import de.domjos.cloudapp2.adapter.getOrCreateSyncAccount
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.ConnectionType
import de.domjos.cloudapp2.appbasics.helper.Notifications
import de.domjos.cloudapp2.appbasics.helper.ProgressDialog
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.appbasics.helper.connectivityType
import de.domjos.cloudapp2.appbasics.navigation.FooterItem
import de.domjos.cloudapp2.appbasics.navigation.FooterMenu
import de.domjos.cloudapp2.appbasics.navigation.View
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
import de.domjos.cloudapp2.widgets.CalendarWidget
import de.domjos.cloudapp2.widgets.ContactsWidget
import de.domjos.cloudapp2.widgets.NewsWidget
import de.domjos.cloudapp2.worker.CalendarWorker
import de.domjos.cloudapp2.worker.ContactWorker
import java.util.Locale

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create splash-Screen
        installSplashScreen()

        setContent {

            // create tabs
            val notificationsTab = FooterItem(id="notifications", title = stringResource(id = R.string.notifications), selectedIcon = Icons.Filled.Notifications, unselectedIcon = Icons.Outlined.Notifications)
            val dataTab = FooterItem(id="data", title = stringResource(id = R.string.data), selectedIcon = Icons.AutoMirrored.Filled.List, unselectedIcon = Icons.AutoMirrored.Outlined.List)
            val notesTab = FooterItem(id="notes", title = stringResource(id = R.string.notes), selectedIcon = Icons.Filled.Create, unselectedIcon = Icons.Outlined.Create)
            val calendarsTab = FooterItem(id="calendars", title = stringResource(id = R.string.calendars), selectedIcon = Icons.Filled.DateRange, unselectedIcon = Icons.Outlined.DateRange)
            val contactsTab = FooterItem(id="contacts", title = stringResource(id = R.string.contacts), selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
            val todosTab = FooterItem(id="todos", title = stringResource(R.string.todos), selectedIcon = Icons.Filled.Check, unselectedIcon = Icons.Outlined.Check, header = stringResource(R.string.todos))
            val roomTab = FooterItem(id="rooms", title = stringResource(id = R.string.chats_room), selectedIcon = Icons.AutoMirrored.Filled.Message, unselectedIcon = Icons.AutoMirrored.Outlined.Message, header = stringResource(id = R.string.chats))
            val chatsTab = FooterItem(id="chats", title = stringResource(id = R.string.chats), selectedIcon = Icons.AutoMirrored.Filled.Message, unselectedIcon = Icons.AutoMirrored.Outlined.Message)
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
            var tabBarVisible by remember { mutableStateOf(true) }
            var viewMode by remember { mutableStateOf(View.Icon) }
            val viewModeUpdate: () -> Unit = {viewModel.setViewMode { l -> viewMode = l }}
            val back = {
                navController.navigate(notificationsTab.title)
                tabBarVisible = true
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
                    viewModel.getAuthentication {
                        val account = getOrCreateSyncAccount(context, it)
                        ContentResolver.removePeriodicSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
                        ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true)
                        ContentResolver.addPeriodicSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY, contactPollFrequency)
                        ContentResolver.requestSync(account, ContactsContract.AUTHORITY, Bundle.EMPTY)
                    }
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
                    viewModel.getAuthentication {
                        val account = getOrCreateSyncAccount(context, it)
                        ContentResolver.removePeriodicSync(account, CalendarContract.AUTHORITY, Bundle.EMPTY)
                        ContentResolver.setSyncAutomatically(account, CalendarContract.AUTHORITY, true)
                        ContentResolver.addPeriodicSync(account, CalendarContract.AUTHORITY, Bundle.EMPTY, contactPollFrequency)
                        ContentResolver.requestSync(account, CalendarContract.AUTHORITY, extras)
                    }
                } catch (ex: Exception) {
                    viewModel.message.postValue(ex.message)
                }
                try {
                    viewModel.getAuthentication {
                        val account = getOrCreateSyncAccount(context, it)
                        ContentResolver.removePeriodicSync(account, FileSyncAdapter.AUTHORITY, Bundle.EMPTY)
                        ContentResolver.setSyncAutomatically(account, FileSyncAdapter.AUTHORITY, true)
                        ContentResolver.requestSync(account, FileSyncAdapter.AUTHORITY, Bundle.EMPTY)
                    }
                } catch (ex: Exception) {
                    viewModel.message.postValue(ex.message)
                }
            }

            val updateNavBar: () -> Unit = {
                viewModel.setVisibility(tabBarItems) { items -> tabBarItems = items }
                viewModeUpdate()
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

            val view = viewModel.viewMode()

            LaunchedEffect(isConnected) {
                viewModeUpdate()
                updateTheme(null)
                initWorker()
                initSyncAdapter()
                updateNavBar()
                updateWidgets()
                if(viewModel.getFirstStart()) {
                    toPermissions()
                }
            }

            LaunchedEffect(view) {
                viewModeUpdate()
            }

            CloudAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Scaffold(
                        bottomBar = { FooterMenu(viewMode, tabBarItems, navController, updateNavBar, tabBarVisible) },
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
                                                   updateTheme,
                                                   viewModeUpdate,
                                                   updateNavBar,
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
                                AuthenticationScreen(
                                    colorForeground = colorForeground,
                                    colorBackground = colorBackground,
                                    onSelectedChange = updateTheme
                                )
                                title = authentications
                                header = authentications
                                refreshVisible = false
                                breadcrumb = ""
                            }
                            composable(notificationsTab.title) {
                                NotificationScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground, onChatScreen = { x, y ->
                                    navController.navigate("android-app://androidx.navigation/Chats/$x/$y".toUri())
                                })
                                title = notificationsTab.title
                                header = notificationsTab.title
                                refreshVisible = false
                                tabBarVisible = true
                                breadcrumb = ""
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
                                tabBarVisible = true
                                breadcrumb = ""
                            }
                            composable(notesTab.title) {
                                NotesScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = notesTab.title
                                header = notesTab.title
                                refreshVisible = false
                                tabBarVisible = true
                                breadcrumb = ""
                            }
                            composable(calendarsTab.title) {
                                CalendarScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = calendarsTab.title
                                header = calendarsTab.title
                                refreshVisible = true
                                progress = importCalendarAction()
                                tabBarVisible = true
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
                                tabBarVisible = true
                                breadcrumb = ""
                            }
                            composable(todosTab.title) {
                                ToDoScreen(toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = todosTab.title
                                header = todosTab.title
                                refreshVisible = true
                                progress = importToDoAction()
                                tabBarVisible = true
                                breadcrumb = ""
                            }
                            composable(roomTab.title) {
                                RoomScreen(onChatScreen = { x, y ->
                                    navController.navigate("${chatsTab.id}/$x/$y")
                                }, toAuths = toAuths, colorBackground = colorBackground, colorForeground = colorForeground)
                                title = chatsTab.title
                                header = roomTab.title
                                refreshVisible = false
                                tabBarVisible = true
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
                                tabBarVisible = true
                                breadcrumb = ""
                            }
                            composable(settings) {
                                title = settings
                                header = settings
                                SettingsScreen()
                                tabBarVisible = true
                                breadcrumb = ""
                            }
                            composable(permissions) {
                                title = permissions
                                header = permissions
                                tabBarVisible = false
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
fun Menu(
    onExpanded: (Boolean) -> Unit,
    updateTheme: (Authentication?) -> Unit,
    viewModeUpdate: () -> Unit,
    updateNavBar: () -> Unit,
    expanded: Boolean, onSettings: () -> Unit,
    onPermissions: () -> Unit,
    onExport: () -> Unit,
    onLog: () -> Unit) {

    val context = LocalContext.current
    DropdownMenu(expanded = expanded, onDismissRequest = {
        onExpanded(false)
        viewModeUpdate()
    }) {
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
            viewModeUpdate()
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
            viewModeUpdate()
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
            viewModeUpdate()
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