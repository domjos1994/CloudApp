/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.features.todofeature.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.database.dao.ListTuple
import de.domjos.cloudapp2.database.model.todo.ToDoItem
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.custom.DatePickerDocked
import de.domjos.cloudapp2.appbasics.custom.DropDown
import de.domjos.cloudapp2.appbasics.custom.FAB
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.database.converters.ToDoStatusConverter
import de.domjos.cloudapp2.database.model.todo.Status
import java.util.Date
import java.util.UUID

@Composable
fun ToDoScreen(viewModel: ToDoViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit) {
    val lists by viewModel.lists.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.loadLists()
        viewModel.loadToDos()
    }

    LogViewModel.Init(viewModel)

    ToDoScreen(
        lists, selected, items,
        {list:ListTuple? -> viewModel.select(list)},
        {list:ListTuple -> viewModel.updateList(list)},
        {list:ListTuple -> viewModel.deleteList(list)},
        {todo:ToDoItem? -> viewModel.insertOrUpdateToDo(todo)},
        {todo:ToDoItem -> viewModel.deleteToDo(todo)},
        colorBackground, colorForeground,
        {viewModel.hasNoAuthentications()},
        toAuths
    )
}

@Composable
fun importToDoAction(viewModel: ToDoViewModel = hiltViewModel()): (updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit) -> Unit {
    val context = LocalContext.current
    return {onProgress, onFinish ->
        val lbl = context.getString(R.string.todos_import)
        viewModel.import(onProgress, lbl, onFinish)
    }
}

@Composable
fun ToDoScreen(
    lists: List<ListTuple>, selected: ListTuple?, items: List<ToDoItem>,
    onSelectList: (ListTuple?) -> Unit,
    onUpdateList: (ListTuple) -> Unit,
    onDeleteList: (ListTuple) -> Unit,
    onInsertOrUpdateToDo: (ToDoItem?) -> Unit,
    onDeleteToDo: (ToDoItem) -> Unit,
    colorBackground: Color, colorForeground: Color,
    hasNoAuthentications: () -> Boolean,
    toAuths: () -> Unit) {

    var dialog by remember { mutableStateOf(false) }
    var item by remember { mutableStateOf<ToDoItem?>(null) }

    if(dialog) {
        ToDoDialog({dialog=it}, item, selected, onInsertOrUpdateToDo, colorBackground, colorForeground)
    }

    Column(Modifier.fillMaxSize()) {
        Row {
            ConstraintLayout(Modifier.fillMaxSize()) {
                val (listDropDown, toDoItems, controls) = createRefs()

                Row(Modifier.constrainAs(listDropDown) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                }) {
                    ListDropDown(lists, selected, onSelectList, onUpdateList, onDeleteList, colorBackground, colorForeground)
                }

                Row(Modifier.constrainAs(toDoItems) {
                    top.linkTo(listDropDown.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                }.padding(5.dp)) {
                    if(hasNoAuthentications()) {
                        NoAuthenticationItem(colorForeground, colorBackground, toAuths)
                    } else {
                        ToDoList(
                            items,
                            colorBackground,
                            colorForeground,
                            onDeleteToDo
                        ) {
                            item = it
                            dialog = true
                        }
                    }
                }

                Row(Modifier.constrainAs(controls) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                },
                    horizontalArrangement = Arrangement.End) {
                    if(selected != null) {
                        FAB(
                            Icons.Default.Add,
                            stringResource(R.string.login_add),
                            colorBackground,
                            colorForeground) {
                            item = null
                            dialog = true
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToDoList(
    items: List<ToDoItem>,
    colorBackground: Color,
    colorForeground: Color,
    onDelete: (ToDoItem) -> Unit,
    onClick: (ToDoItem) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(colorBackground)) {
        Column(Modifier.padding(5.dp)) {
            items.forEach { item ->
                Row(
                    Modifier
                        .padding(2.dp)
                        .height(44.dp)
                        .clickable { onClick(item) }
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        if(item.checked) {
                            Icon(Icons.Default.Check, item.summary, tint = colorForeground)
                        } else {
                            Icon(Icons.Default.Close, item.summary, tint = colorForeground)
                        }
                    }
                    Column(
                        Modifier
                            .weight(1f)
                            .height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        Column(Modifier.height(10.dp).width(10.dp).background(stringToColor(item.listColor))) {}
                    }
                    Column(
                        Modifier.weight(6f).height(40.dp),
                        verticalArrangement = Arrangement.Center) {
                        Text(item.summary, Modifier.basicMarquee(), color = colorForeground)
                    }
                    Column(
                        Modifier.weight(2f).height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(
                            {(item.completed.toFloat() / 100.0).toFloat()},
                            trackColor = colorBackground,
                            color = colorForeground
                        )
                    }
                    Column(
                        Modifier.weight(1f).height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        IconButton({onDelete(item)}) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.sys_delete_item),
                                tint = colorForeground
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.height(1.dp), color = colorForeground)
            }
        }
    }
}



@Composable
fun ListDropDown(
    lists: List<ListTuple>,
    selected: ListTuple?,
    onSelectList: (ListTuple?) -> Unit,
    onSave: (ListTuple) -> Unit,
    onDelete: (ListTuple) -> Unit,
    colorBackground: Color,
    colorForeground: Color) {

    var dialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.height(70.dp)) {
        Row(Modifier.fillMaxWidth().height(69.dp).background(colorBackground)) {
            Column(Modifier.weight(9f).padding(5.dp)) {
                OutlinedTextField(
                    selected?.name ?: "",
                    { text ->
                        val find = lists.find { tmp -> tmp.name == text }
                        if(find?.uid != null) {
                            onSelectList(find)
                        } else {
                            onSelectList(null)
                        }
                    },
                    label = {
                        Text(
                            stringResource(R.string.todos_list),
                            color = colorForeground
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = colorForeground,
                        unfocusedTextColor = colorForeground,
                        focusedSupportingTextColor = colorForeground,
                        unfocusedSupportingTextColor = colorForeground,
                        focusedBorderColor = colorForeground,
                        unfocusedBorderColor = colorForeground
                    ),
                    trailingIcon = {
                        IconButton({
                            expanded = !expanded
                        }) {
                            Icon(Icons.Default.ArrowDropDown, "Open Dropdown")
                        }
                    },
                    readOnly = true
                )
                DropdownMenu(expanded, onDismissRequest = {expanded=false}) {
                    lists.filter { it.toString().isNotEmpty() }.forEach { listItem ->
                        DropdownMenuItem({Text(listItem.toString())}, {
                            if(listItem.uid != null) {
                                onSelectList(listItem)
                            } else {
                                onSelectList(null)
                            }
                            expanded = !expanded
                        })
                    }
                }
            }
            Column(
                Modifier.weight(1f).height(69.dp).padding(5.dp),
                verticalArrangement = Arrangement.Center) {
                if(selected != null) {
                    Row(
                        Modifier
                            .height(10.dp).width(10.dp)
                            .background(stringToColor(selected.color ?: "")),
                        verticalAlignment = Alignment.CenterVertically) {}
                }
            }
            Column(
                Modifier.weight(1f).height(69.dp).padding(5.dp),
                verticalArrangement = Arrangement.Center) {
                IconButton({
                    dialog = true
                }) {
                    Icon(
                        Icons.Filled.Edit,
                        stringResource(R.string.sys_list_edit),
                        tint = colorForeground
                    )
                }
            }
        }
        HorizontalDivider(Modifier.fillMaxWidth().height(1.dp), color = colorForeground)
    }

    if(dialog) {
        ListDialog({dialog=it}, selected, onSave, onDelete, colorBackground, colorForeground)
    }
}

@Composable
fun ListDialog(
    onShow: (Boolean) -> Unit,
    listTuple: ListTuple?,
    onSave: (ListTuple) -> Unit,
    onDelete: (ListTuple) -> Unit,
    colorBackground: Color,
    colorForeground: Color) {


    Dialog({onShow(false)}, DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )) {
        Surface(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, colorForeground, RoundedCornerShape(5.dp))) {
            var name by remember { mutableStateOf(TextFieldValue(listTuple?.name ?: "")) }
            var isNameValid by remember { mutableStateOf(name.text.isNotEmpty()) }
            var color by remember { mutableStateOf(TextFieldValue(listTuple?.color ?: "")) }
            var isColorValid by remember { mutableStateOf(colorValid(color.text)) }

            Column(Modifier
                .background(colorBackground)
                .padding(5.dp)) {
                Row {
                    OutlinedTextField(
                        name,
                        {
                            name = it
                            isNameValid = it.text.isNotEmpty()
                        },
                        label = {
                            Text(
                                stringResource(R.string.todos_list_name),
                                color = colorForeground
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        ),
                        isError = !isNameValid,
                    )
                }
                Row {
                    OutlinedTextField(
                        color,
                        {
                            color = it
                            isColorValid = colorValid(it.text)
                        },
                        label = {
                            Text(
                                stringResource(R.string.todos_list_color),
                                color = colorForeground
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        ),
                        isError = !isColorValid,
                    )
                }

                Row {
                    Column(Modifier.weight(1f)) {
                        if(listTuple != null) {
                            IconButton({
                                onDelete(listTuple)
                                onShow(false)
                            }) { Icon(Icons.Filled.Delete, stringResource(R.string.sys_delete_item), tint = colorForeground) }
                        }
                    }
                    Column(Modifier.weight(7f)) {}
                    Column(Modifier.weight(1f)) {
                        IconButton({
                            onShow(false)
                        }) { Icon(Icons.Filled.Close, stringResource(R.string.sys_clear_text), tint = colorForeground) }
                    }
                    Column(Modifier.weight(1f)) {
                        IconButton({
                            val uid = listTuple?.uid ?: ""
                            val tmp = ListTuple(uid, name.text, color.text)

                            onSave(tmp)
                            onShow(false)
                        }, enabled = (isNameValid && isColorValid))
                        { Icon(Icons.Filled.Check, stringResource(R.string.sys_list_edit), tint = colorForeground) }
                    }
                }
            }
        }
    }
}

private fun colorValid(color: String): Boolean {
    return stringToColor(color) != Color.Transparent
}

@Composable
fun ToDoDialog(
    onShow: (Boolean) -> Unit,
    toDoItem: ToDoItem?,
    list: ListTuple?,
    onSave: (ToDoItem) -> Unit,
    colorBackground: Color,
    colorForeground: Color) {

    Dialog({onShow(false)}, DialogProperties(
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )) {
        Surface(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, colorForeground, RoundedCornerShape(5.dp))
        ) {
            var summary by remember { mutableStateOf(TextFieldValue(toDoItem?.summary ?: "")) }
            var isSummaryValid by remember { mutableStateOf(summary.text.isNotEmpty()) }
            var start by remember { mutableStateOf(toDoItem?.start ?: Date()) }
            var end by remember { mutableStateOf(toDoItem?.end ?: Date()) }
            val statusLabelList = mutableMapOf<String, String>()
            statusLabelList[stringResource(R.string.todos_status_tentative)] = Status.TENTATIVE.name
            statusLabelList[stringResource(R.string.todos_status_confirmed)] = Status.CONFIRMED.name
            statusLabelList[stringResource(R.string.todos_status_cancelled)] = Status.CANCELLED.name
            statusLabelList[stringResource(R.string.todos_status_needs_action)] = Status.NEEDS_ACTION.name
            statusLabelList[stringResource(R.string.todos_status_completed)] = Status.COMPLETED.name
            statusLabelList[stringResource(R.string.todos_status_in_process)] = Status.IN_PROCESS.name
            statusLabelList[stringResource(R.string.todos_status_draft)] = Status.DRAFT.name
            statusLabelList[stringResource(R.string.todos_status_final)] = Status.FINAL.name
            var value = ""
            if((toDoItem?.status?.name ?: "") != "") {
                statusLabelList.entries.forEach { entry ->
                    if(entry.value == (toDoItem?.status?.name ?: "")) {
                        value = entry.key
                    }
                }
            }
            var status by remember { mutableStateOf(value) }
            var priority by remember { mutableFloatStateOf(toDoItem?.priority?.toFloat() ?: 4f) }
            var completed by remember { mutableFloatStateOf(toDoItem?.completed?.toFloat() ?: 30f) }
            var location by remember { mutableStateOf(TextFieldValue(toDoItem?.location ?: "")) }
            var url by remember { mutableStateOf(TextFieldValue(toDoItem?.url ?: "")) }
            var categories by remember { mutableStateOf(TextFieldValue(toDoItem?.categories ?: "")) }

            Column(Modifier
                .background(colorBackground)
                .height(500.dp)
                .verticalScroll(rememberScrollState())) {
                Row(Modifier.padding(5.dp)) {
                    OutlinedTextField(
                        value = summary,
                        onValueChange = {
                            summary = it
                            isSummaryValid = it.text.isNotEmpty()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.todos_summary), color = colorForeground)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        ),
                        isError = !isSummaryValid
                    )
                }
                Row(Modifier.padding(5.dp)) {
                    DatePickerDocked(
                        date = start,
                        onValueChange = { start = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.todos_start), color = colorForeground)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        ),
                        showTime = false
                    )
                }
                Row(Modifier.padding(5.dp)) {
                    DatePickerDocked(
                        date = end,
                        onValueChange = { end = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.todos_end), color = colorForeground)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        ),
                        showTime = false
                    )
                }
                Row(Modifier.padding(5.dp)) {
                    DropDown(
                        items = statusLabelList.keys.toList(),
                        initial = status,
                        onSelected = { status = it },
                        label = stringResource(R.string.todos_status),
                        colorBackground = colorBackground,
                        colorForeground = colorForeground
                    )
                }
                Row(Modifier.padding(5.dp)) {
                    Text(stringResource(R.string.todos_priority), color = colorForeground)
                }
                HorizontalDivider(
                    Modifier.padding(5.dp).fillMaxWidth(),
                    color = colorForeground
                )
                Row(Modifier.padding(5.dp)) {
                    Column(
                        Modifier.weight(8f).height(40.dp)
                    ) {
                        Slider(
                            value = priority,
                            onValueChange = { priority = it },
                            steps = 10,
                            valueRange = 0f..9f
                        )
                    }
                    Column(
                        Modifier.weight(2f).height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("%.0f".format(priority), color = colorForeground)
                    }
                }
                Row(Modifier.padding(5.dp)) {
                    Text(stringResource(R.string.todos_completed), color = colorForeground)
                }
                HorizontalDivider(
                    Modifier.padding(5.dp).fillMaxWidth(),
                    color = colorForeground
                )
                Row(Modifier.padding(5.dp)) {
                    Column(
                        Modifier.weight(8f).height(40.dp)
                    ) {
                        Slider(
                            value = completed,
                            onValueChange = { completed = it },
                            steps = 101,
                            valueRange = 0f..100f
                        )
                    }
                    Column(
                        Modifier.weight(2f).height(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("%.0f".format(completed) + "%", color = colorForeground)
                    }
                }
                Row(Modifier.padding(5.dp)) {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.todos_location), color = colorForeground)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        )
                    )
                }
                Row(Modifier.padding(5.dp)) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.todos_url), color = colorForeground)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        )
                    )
                }
                Row(Modifier.padding(5.dp)) {
                    OutlinedTextField(
                        value = categories,
                        onValueChange = { categories = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(stringResource(R.string.todos_categories), color = colorForeground)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colorForeground,
                            unfocusedTextColor = colorForeground,
                            focusedSupportingTextColor = colorForeground,
                            unfocusedSupportingTextColor = colorForeground,
                            focusedBorderColor = colorForeground,
                            unfocusedBorderColor = colorForeground
                        )
                    )
                }
                HorizontalDivider(
                    Modifier.padding(5.dp).fillMaxWidth(),
                    color = colorForeground
                )
                Row(Modifier.padding(5.dp)) {
                    Column(Modifier.weight(8f)) {}
                    Column(Modifier.weight(1f)) {
                        IconButton({onShow(false)}) {
                            Icon(
                                Icons.Default.Close,
                                stringResource(R.string.login_close),
                                tint = colorForeground
                            )
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        IconButton(
                            {
                                val id = toDoItem?.id ?: 0L
                                val uid = toDoItem?.uid ?: ""
                                val path = toDoItem?.path ?: ""
                                val listUid = list?.uid ?: toDoItem?.listUid ?: ""
                                val listName = list?.name ?: toDoItem?.listName ?: ""
                                val listColor = list?.color ?: toDoItem?.listColor ?: ""

                                val item = ToDoItem(
                                    id = id,
                                    uid = uid,
                                    listUid = listUid,
                                    listName = listName,
                                    listColor = listColor,
                                    summary = summary.text,
                                    start = start,
                                    end = end,
                                    status = ToDoStatusConverter().fromString(statusLabelList.getOrDefault(status, "")),
                                    completed = completed.toInt(),
                                    priority = priority.toInt(),
                                    location = location.text,
                                    url = url.text,
                                    categories = categories.text,
                                    path = path
                                )
                                onSave(item)
                                onShow(false)
                            }, enabled = isSummaryValid) {
                            Icon(
                                Icons.Default.Check,
                                stringResource(R.string.login_close),
                                tint = colorForeground
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun stringToColor(color: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(color))
    } catch (_: Exception) { Color.Transparent}
}

@Preview
@Composable
fun DropDownPreview() {
    CloudAppTheme {
        var selected: ListTuple? = ListTuple("2", "Test-2", "#ffff00")
        val items = mutableListOf<ListTuple>()
        items.add(ListTuple("1", "Test-1", "#ff0000"))
        items.add(selected!!)
        items.add(ListTuple("3", "Test-3", "#ffffff"))

        ListDropDown(items, selected, {selected = it}, {}, {}, Color.Blue, Color.White)
    }
}

@Preview
@Composable
fun ListDialogPreview() {
    CloudAppTheme {
        val selected = ListTuple("2", "Test-2", "#ffff00")

        ListDialog({}, selected, {}, {}, Color.Blue, Color.White)
    }
}

@Preview
@Composable
fun ToDoDialogPreview() {
    CloudAppTheme {
        val selected = ToDoItem(
            id = 1L,
            uid = UUID.randomUUID().toString(),
            listUid = UUID.randomUUID().toString(),
            "List - 1",
            "#ff0000",
            "This is a test!",
            start = Date(),
            end = Date(),
            status = Status.IN_PROCESS,
            completed = 30,
            priority = 3,
            location = "Test",
            url = "Test",
            categories = "test, test2",
            path = "",
            authId = 1L
        )
        val list = ListTuple("2", "Test-2", "#ffff00")

        ToDoDialog({}, selected, list, {}, Color.Blue, Color.White)
    }
}

@Preview
@Composable
fun ToDoListPreview() {
    CloudAppTheme {
        val toDoLists = mutableListOf<ToDoItem>()
        toDoLists.add(ToDoItem(1L, "", "", "List 1", "#ff0000", "Test 1", completed = 10))
        toDoLists.add(ToDoItem(2L, "", "", "List 2", "#ffff00", "Test 2", completed = 30))
        toDoLists.add(ToDoItem(3L, "", "", "List 2", "#ffff00", "Test 3", completed = 50))

        ToDoList(toDoLists, Color.Blue, Color.White, {}) {}
    }
}

@Preview
@Composable
fun ToDoScreenPreview() {
    CloudAppTheme {
        val listTuples = mutableListOf<ListTuple>()
        listTuples.add(ListTuple("", "List 1", "#ff0000"))
        listTuples.add(ListTuple("", "List 2", "#ffff00"))

        val toDoLists = mutableListOf<ToDoItem>()
        toDoLists.add(ToDoItem(1L, "", listTuples[0].uid!!, listTuples[0].name!!, listTuples[0].color!!, "Test 1", completed = 10))
        toDoLists.add(ToDoItem(2L, "", listTuples[1].uid!!, listTuples[1].name!!, listTuples[1].color!!, "Test 2", completed = 30))
        toDoLists.add(ToDoItem(3L, "", listTuples[1].uid!!, listTuples[1].name!!, listTuples[1].color!!, "Test 3", completed = 50))

        ToDoScreen(listTuples, listTuples[0], toDoLists, {}, {}, {}, {}, {}, Color.Blue, Color.White, {false}) { }
    }
}