/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.features.notifications.screens.model

import android.graphics.drawable.Icon
import androidx.compose.runtime.Composable
import de.domjos.cloudapp2.rest.model.notifications.Notification
import java.util.Date

data class NotificationItem(
    val type: Type,
    val notification: Notification? = null,
    val date: Date = Date(),
    val title: String = "",
    val description: String = "",
    val icon: @Composable () -> Unit = {}) {

    enum class Type {
        App,
        Server
    }
}