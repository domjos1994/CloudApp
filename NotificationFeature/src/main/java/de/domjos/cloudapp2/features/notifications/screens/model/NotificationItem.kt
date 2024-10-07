/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.features.notifications.screens.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import de.domjos.cloudapp2.rest.model.notifications.Notification
import java.util.Date

data class NotificationItem(
    val type: Type,
    val notification: Notification? = null,
    val date: Date = Date(),
    val title: String = "",
    val description: String = "",
    val icon: @Composable (Color) -> Unit = {}) {

    enum class Type {
        App,
        Server
    }
}