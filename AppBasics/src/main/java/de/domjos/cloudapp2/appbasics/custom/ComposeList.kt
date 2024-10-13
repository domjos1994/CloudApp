/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.custom

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissDirection
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import java.util.UUID
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> ComposeList(
    onReload: () -> MutableList<ListItem<T>>,
    colorBackground: Color,
    colorForeground: Color,
    modifier: Modifier = Modifier,
    needsInternet: Boolean = false,
    onSwipeToStart: ActionItem<T>? = null,
    onSwipeToEnd: ActionItem<T>? = null,
    actions: List<ActionItem<T>> = listOf(),
    multiActions: List<MultiActionItem<T>> = listOf()) {

    var listItems = onReload()

    var showCheckBoxes by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(isLoading, {
        listItems.clear()
        isLoading = true
        listItems = onReload()
        isLoading = false
    })

    Column(modifier
        .background(colorBackground)) {
        if(showCheckBoxes) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp)) {
                multiActions.forEach { multiAction:MultiActionItem<T> ->
                    if(multiAction.visible) {
                        Column(Modifier.weight(1.0f)) {
                            IconButton(onClick = {
                                multiAction.action(listItems.filter { it.selected })
                            }) {
                                if(multiAction.icon != null) {
                                    Icon(multiAction.icon, multiAction.name, tint = colorForeground)
                                }
                                if(multiAction.painter != null) {
                                    Icon(multiAction.painter, multiAction.name, tint = colorForeground)
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = colorForeground)
            }
        }

        Box(
            modifier = modifier
                .pullRefresh(pullRefreshState)) {

            LazyColumn {
                items(items = listItems, key = {it.uid} ) { item ->
                    var selected by remember { mutableStateOf(item.selected) }

                    val onLongClick = {
                        showCheckBoxes = if(multiActions.isNotEmpty()) {
                            !showCheckBoxes
                        } else {
                            false
                        }
                    }
                    if(internet(needsInternet)) {
                        if(listItems.size != 0) {
                            if(onSwipeToStart != null || onSwipeToEnd != null) {
                                val currentItem = rememberUpdatedState(item)
                                val dismissState = rememberDismissState(
                                    confirmStateChange = { state ->
                                        when(state) {
                                            DismissValue.DismissedToStart -> {
                                                onSwipeToStart?.action?.let { it(currentItem.value) } ?: false
                                            }
                                            DismissValue.DismissedToEnd -> {
                                                onSwipeToEnd?.action?.let { it(currentItem.value) } ?: false
                                            }
                                            DismissValue.Default -> false
                                        }
                                    }
                                )

                                val directions = mutableSetOf<DismissDirection>()
                                if(onSwipeToEnd != null) {
                                    directions.add(DismissDirection.StartToEnd)
                                }
                                if(onSwipeToStart != null) {
                                    directions.add(DismissDirection.EndToStart)
                                }

                                SwipeToDismiss(
                                    state = dismissState,
                                    directions = directions,
                                    background = {
                                        SwipeBackground(dismissState, colorBackground, onSwipeToStart, onSwipeToEnd)
                                    }) {
                                    ListItem(item, selected, {
                                        selected = it
                                        listItems.find { tmp -> tmp.uid == item.uid }?.selected = selected
                                    }, actions, showCheckBoxes, colorForeground, colorBackground, onLongClick)
                                }
                            } else {
                                ListItem(item, selected, {
                                    selected = it
                                    listItems.find { tmp -> tmp.uid == item.uid }?.selected = selected
                                }, actions, showCheckBoxes, colorForeground, colorBackground, onLongClick)
                            }
                        } else {
                            ListItem(
                                item = ListItem<T>(
                                    stringResource(R.string.sys_no_entry),
                                    stringResource(R.string.sys_no_entry),
                                    Icons.AutoMirrored.Filled.List
                                ),
                                selected = false,
                                onSelectedChanged = {},
                                actions = listOf(),
                                colorForeground = colorForeground,
                                colorBackground = colorBackground,
                                showCheckboxes = false,
                                onLongClick = {}
                            )
                        }
                    } else {
                        ListItem(
                            item = ListItem<T>(
                                stringResource(R.string.sys_no_internet),
                                stringResource(R.string.sys_no_internet),
                                Icons.AutoMirrored.Filled.List
                            ),
                            selected = false,
                            onSelectedChanged = {},
                            actions = listOf(),
                            colorForeground = colorForeground,
                            colorBackground = colorBackground,
                            showCheckboxes = false,
                            onLongClick = {}
                        )
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = if (isLoading) Color.Red else Color.Green,
            )
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun internet(needs: Boolean): Boolean {
    if(needs) {
        val connectivity by connectivityState()
        return connectivity === ConnectionState.Available
    }
    return true
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> SwipeBackground(dismissState: DismissState, colorBackground: Color, onSwipeToStart: ActionItem<T>?, onSwipeToEnd: ActionItem<T>?) {
    val direction = dismissState.dismissDirection ?: return

    val color by animateColorAsState(
        when(dismissState.targetValue) {
            DismissValue.Default -> colorBackground
            DismissValue.DismissedToStart -> onSwipeToStart?.color ?: colorBackground
            DismissValue.DismissedToEnd -> onSwipeToEnd?.color ?: colorBackground
        }, label = ""
    )
    val alignment = when(direction) {
        DismissDirection.StartToEnd -> Alignment.CenterStart
        DismissDirection.EndToStart -> Alignment.CenterEnd
    }
    val icon = when(direction) {
        DismissDirection.StartToEnd -> onSwipeToEnd?.icon ?: Icons.Default.Archive
        DismissDirection.EndToStart -> onSwipeToStart?.icon ?: Icons.Default.Delete
    }
    val description = when(direction) {
        DismissDirection.StartToEnd -> onSwipeToStart?.name ?: ""
        DismissDirection.EndToStart -> onSwipeToEnd?.name ?: ""
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        if(dismissState.targetValue != DismissValue.Default) {
            Icon(icon, contentDescription = description)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ListItem(item: ListItem<T>, selected: Boolean, onSelectedChanged: (Boolean) -> Unit, actions: List<ActionItem<T>>, showCheckboxes: Boolean, colorForeground: Color, colorBackground: Color, onLongClick: () -> Unit) {
    Row {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {},
                        onLongClick = { onLongClick() }
                    )
                    .height(65.dp)) {
                if(showCheckboxes) {
                    Column(
                        Modifier
                            .weight(1.0f)
                            .fillMaxHeight()
                    ) {
                        Checkbox(checked = selected, onCheckedChange = {onSelectedChanged(it)})
                    }
                }
                Column(
                    Modifier
                        .weight(1.0f)
                        .fillMaxHeight()) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                        if(item.icon != null) {
                            Icon(item.icon, item.title, tint = colorForeground)
                        }
                        if(item.painter != null) {
                            Icon(item.painter, item.title, tint = colorForeground)
                        }
                        if(item.image != null) {
                            Image(bitmap = item.image, contentDescription = item.title)
                        }
                    }
                }
                Column(Modifier.weight(9.0f)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        Text(
                            item.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorForeground,
                            modifier = Modifier.basicMarquee())
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        Text(
                            item.description,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = colorForeground,
                            modifier = Modifier.basicMarquee())
                    }
                }
                actions.forEach { action:ActionItem<T> ->
                    if(action.visible(item)) {
                        Column(
                            Modifier
                                .weight(1.0f)
                                .fillMaxHeight()) {
                            ActionButton(
                                action = action,
                                item = item,
                                colorForeground = colorForeground,
                                colorBackground = colorBackground
                            )
                        }
                    }
                }
                item.actions.forEach {action: ActionItem<T> ->
                    if(action.visible(item)) {
                        Column(
                            Modifier
                                .weight(1.0f)
                                .fillMaxHeight()) {
                            ActionButton(
                                action = action,
                                item = item,
                                colorForeground = colorForeground,
                                colorBackground = colorBackground
                            )
                        }
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()) {
                HorizontalDivider(color = colorForeground)
            }
        }
    }
}

@Composable
fun <T> ActionButton(action: ActionItem<T>, item: ListItem<T>, colorForeground: Color, colorBackground: Color) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {

        if(action.icon != null || action.painter != null) {
            IconButton(onClick = { action.action(item) }) {
                if(action.icon != null) {
                    Icon(action.icon, action.name, tint = colorForeground)
                }
                if(action.painter != null) {
                    Icon(action.painter, action.name, tint = colorForeground)
                }
            }
        } else {
            Button(onClick = {action.action(item)}, colors = ButtonDefaults.buttonColors(containerColor = colorBackground)) {
                Text(action.name, color = colorForeground)
            }
        }
    }
}

data class ListItem<T>(val title: String, val description: String, val icon: ImageVector? = null, val painter: Painter? = null, val image: ImageBitmap? = null, var selected: Boolean = false, val deletable: Boolean = true) {
    val uid: String = UUID.randomUUID().toString()
    val actions: MutableList<ActionItem<T>> = mutableListOf()
    var id: T? = null
}

data class ActionItem<T>(val name: String, val icon: ImageVector? = null, val painter: Painter? = null, val action: (ListItem<T>) -> Boolean, val color: Color = Color.Transparent, val visible: (ListItem<T>) -> Boolean = {true})
data class MultiActionItem<T>(val name: String, val icon: ImageVector? = null, val painter: Painter? = null, val action: (List<ListItem<T>>) -> Boolean, val visible: Boolean = true)

@Preview(showBackground = true)
@Composable
fun ComposeListPreview() {
    CloudAppTheme {
        val items = remember { mutableStateListOf<ListItem<Long>>() }

        val delete = { item: ListItem<Long> ->
            val listItem = items.find { item.title == it.title }
            if(listItem != null) {
                items.remove(listItem)
            }
            true
        }
        val multiDelete = {listItems: List<ListItem<Long>> ->
            listItems.forEach { item -> delete(item) }
            true
        }

        ComposeList(
            onReload = {
                items.clear()
                items.add(fake(1))
                items.add(fake(2))
                items.add(fake(3))
                items.add(fake(4))
                items.add(fake(5))
                items
            }, onSwipeToStart =  ActionItem(
                name = "Delete item",
                icon = Icons.Filled.Delete,
                action = delete,
                color = Color.Red
            ),
            colorForeground = Color.White,
            colorBackground = Color.Blue,
            modifier = Modifier.fillMaxSize(),
            actions = listOf(ActionItem(name = "Action", icon = Icons.Filled.PendingActions, action = {true})),
            multiActions = listOf(MultiActionItem(
                name = "Delete multiple Items",
                icon = Icons.Default.Delete,
                action = multiDelete
            ))
        )
    }
}

fun fake(item: Int): ListItem<Long> {
    val listItem = ListItem<Long>("title $item", "description $item", Icons.Filled.Tag)
    return listItem
}