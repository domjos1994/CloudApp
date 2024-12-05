/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.navigation

import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FooterMenu(view: View, footerItems: List<FooterItem>, navController: NavController, updateNavBar: () -> Unit, visible: Boolean = true) {
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    if(visible) {
        NavigationBar {
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
                        })
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
    BadgedBox(
        badge = {}) {
        Icon(
            imageVector = if (isSelected) {selectedIcon} else {unselectedIcon},
            contentDescription = title
        )
    }
}
