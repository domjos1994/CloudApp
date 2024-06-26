/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.custom

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Button(icon: ImageVector?, text: String, onClick: (()->Unit)?, onLongClick: (()->Unit)?) {
    if(icon != null) {
        if(onClick != null && onLongClick != null) {
            Icon(
                icon, text,
                Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            )
        } else if(onClick == null && onLongClick != null) {
            Icon(
                icon, text,
                Modifier
                    .height(50.dp)
                    .width(50.dp)
                    .combinedClickable(onClick = { }, onLongClick = onLongClick)
            )
        } else if(onClick != null) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    Modifier
                        .height(50.dp)
                        .width(50.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = text,
                Modifier
                    .height(50.dp)
                    .width(50.dp)
            )
        }
    } else {
        if(onClick != null && onLongClick != null) {
            Text(
                text,
                Modifier
                    .height(50.dp)
                    .wrapContentWidth()
                    .padding(2.dp)
                    .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            )
        } else if(onClick == null && onLongClick != null) {
            Text(
                text,
                Modifier
                    .height(50.dp)
                    .wrapContentWidth()
                    .padding(2.dp)
                    .combinedClickable(onClick = { }, onLongClick = onLongClick)
            )
        } else if(onClick != null) {
            Text(
                text,
                Modifier
                    .height(50.dp)
                    .wrapContentWidth()
                    .padding(2.dp)
                    .combinedClickable(onClick = onClick, onLongClick = {})
            )
        } else {
            Text(
                text,
                Modifier
                    .height(50.dp)
                    .wrapContentWidth()
                    .padding(2.dp)
            )
        }
    }
}