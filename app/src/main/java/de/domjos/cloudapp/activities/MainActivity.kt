package de.domjos.cloudapp.activities

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp.appbasics.R
import androidx.navigation.compose.composable
import de.domjos.cloudapp.features.calendars.screens.CalendarScreen
import de.domjos.cloudapp.features.chats.screens.ChatScreen
import de.domjos.cloudapp.features.contacts.screens.ContactScreen
import de.domjos.cloudapp.features.data.screens.DataScreen
import de.domjos.cloudapp.features.notifications.screens.NotificationScreen

data class TabBarItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeAmount: Int? = null
)

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val notificationsTab = TabBarItem(title = stringResource(id = R.string.notifications), selectedIcon = Icons.Filled.Notifications, unselectedIcon = Icons.Outlined.Notifications)
            val dataTab = TabBarItem(title = stringResource(id = R.string.data), selectedIcon = Icons.AutoMirrored.Filled.List, unselectedIcon = Icons.AutoMirrored.Outlined.List)
            val calendarsTab = TabBarItem(title = stringResource(id = R.string.calendars), selectedIcon = Icons.Filled.DateRange, unselectedIcon = Icons.Outlined.DateRange)
            val contactsTab = TabBarItem(title = stringResource(id = R.string.contacts), selectedIcon = Icons.Filled.Person, unselectedIcon = Icons.Outlined.Person)
            val chatsTab = TabBarItem(title = stringResource(id = R.string.chats), selectedIcon = Icons.Filled.AccountBox, unselectedIcon = Icons.Outlined.AccountBox)

            // creating a list of all the tabs
            val tabBarItems = listOf(notificationsTab, dataTab, calendarsTab, contactsTab, chatsTab)

            // creating our navController
            val navController = rememberNavController()

            CloudAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(bottomBar = { TabView(tabBarItems, navController) }) {
                        NavHost(navController = navController, startDestination = notificationsTab.title) {
                            composable(notificationsTab.title) {
                                NotificationScreen()
                            }
                            composable(dataTab.title) {
                                DataScreen()
                            }
                            composable(calendarsTab.title) {
                                CalendarScreen()
                            }
                            composable(contactsTab.title) {
                                ContactScreen()
                            }
                            composable(chatsTab.title) {
                                ChatScreen()
                            }
                        }
                    }
                }
            }
        }
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
@Composable
fun GreetingPreview() {
    CloudAppTheme {

    }
}