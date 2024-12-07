/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.helper

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun rememberWindowSize(): WindowSizeInfo {
    val configuration = LocalConfiguration.current
    return WindowSizeInfo(
        widthSize = configuration.screenWidthDp.dp,
        heightSize = configuration.screenHeightDp.dp,
        widthInfo = when {
            configuration.screenWidthDp < 600 -> WindowType.Compact
            configuration.screenWidthDp < 840 -> WindowType.Medium
            else -> WindowType.Expanded
        },
        heightInfo = when {
            configuration.screenHeightDp < 480 -> WindowType.Compact
            configuration.screenHeightDp < 900 -> WindowType.Medium
            else -> WindowType.Expanded
        },
        landscape = (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
    )
}

data class WindowSizeInfo(
    val widthSize: Dp,
    val heightSize: Dp,
    val widthInfo: WindowType,
    val heightInfo: WindowType,
    val landscape: Boolean
)

sealed class WindowType {
    data object Compact: WindowType()
    data object Medium: WindowType()
    data object Expanded: WindowType()
}