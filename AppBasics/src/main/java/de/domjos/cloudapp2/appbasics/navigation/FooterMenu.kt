/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import de.domjos.cloudapp2.appbasics.helper.rememberWindowSize
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme

@Composable
fun FooterMenu(view: View, footerItems: List<FooterItem>, navController: NavController, updateNavBar: () -> Unit, visible: Boolean = true) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }
    val config = rememberWindowSize()

    if(visible) {
        val modifier = if(config.landscape) {
            Modifier.padding(2.dp).height(if(view==View.IconAndText) 70.dp else 50.dp)
        } else {
            Modifier
        }

        NavigationBar(
            modifier = modifier,
            windowInsets = WindowInsets(0.dp)
        ) {
            // looping over each tab to generate the views and navigation for each item
            footerItems.forEachIndexed { index, footerItem ->
                if(footerItem.visible) {
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = {
                            updateNavBar()
                            selectedTabIndex = index
                            navController.navigate(footerItem.title)
                        },
                        icon = {
                            if(view == View.Icon || view == View.IconAndText) {
                                TabBarIconView(
                                    isSelected = selectedTabIndex == index,
                                    selectedIcon = footerItem.selectedIcon,
                                    unselectedIcon = footerItem.unselectedIcon,
                                    title = if(footerItem.header=="") footerItem.title else footerItem.header
                                )
                            }
                        },
                        label = {
                            if(view == View.Text || view == View.IconAndText) {
                                Text(
                                    if (footerItem.header == "") footerItem.title else footerItem.header,
                                    fontSize = 10.sp
                                )
                            }
                        },
                        modifier = Modifier.padding(0.dp)
                    )
                }
            }
        }
    }
}

enum class View {
    Icon,
    Text,
    IconAndText
}

data class FooterItem(
    val id: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val header: String = "",
    var visible: Boolean = true
)

@Composable
fun TabBarIconView(
    isSelected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    title: String
) {
    Icon(
        imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
        contentDescription = title
    )
}

@PreviewScreenSizes
@Composable
private fun TabBarIcon() {
    val items = mutableListOf<FooterItem>()
    items.add(FooterItem("1", "Test-1", Icons.Filled.Place, Icons.Outlined.Place, "Test-1"))
    items.add(FooterItem("2", "Test-2", Icons.Filled.Place, Icons.Outlined.Place, "Test-2"))
    items.add(FooterItem("3", "Test-3", Icons.Filled.Place, Icons.Outlined.Place, "Test-3"))
    CloudAppTheme {
        FooterMenu(View.Icon, items, rememberNavController(), {})
    }
}
