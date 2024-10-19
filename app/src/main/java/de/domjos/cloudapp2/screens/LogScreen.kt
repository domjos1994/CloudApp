/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp2.database.model.Log
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun LogScreen(
    viewModel: LogViewModel = hiltViewModel(),
    colorBackground: Color,
    colorForeground: Color) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        viewModel.loadLogs()
    }

    LogScreen(
        logs,
        {a:String?,b:String? -> viewModel.loadLogs(a ?: "", b ?: "")},
        {a:String? -> viewModel.deleteLogs(a ?: "")},
        colorBackground,
        colorForeground
    )
}

@Composable
fun LogScreen(
    logs: List<Log>,
    onItemTypeChange: (String?, String?) -> Unit,
    onDeleteLog: (String?) -> Unit,
    colorBackground: Color,
    colorForeground: Color) {

    var itemType by remember { mutableStateOf(TextFieldValue("")) }
    var itemTypeDropDown by remember { mutableStateOf(false) }
    var msgType by remember { mutableStateOf(TextFieldValue("")) }
    var msgTypeDropDown by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row {
            Column(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = itemType,
                    onValueChange = {
                        itemType = it
                        onItemTypeChange(it.text, "")
                        itemTypeDropDown = false
                    },
                    label = {Text("Item-Type")},
                    trailingIcon = {
                        IconButton({itemTypeDropDown=!itemTypeDropDown}) {
                            Icon(Icons.Default.ArrowDropDown, "Open Item-Types")
                        }
                    }
                )
                DropdownMenu(itemTypeDropDown, {itemTypeDropDown=false}) {
                    DropdownMenuItem({Text("")}, {itemType = TextFieldValue("")})
                    DropdownMenuItem({Text("Contacts")}, {itemType = TextFieldValue("contacts")})
                    DropdownMenuItem({Text("Calendars")}, {itemType = TextFieldValue("calendars")})
                }
            }
            Column(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = msgType,
                    onValueChange = {
                        msgType = it
                        onItemTypeChange("", it.text)
                        msgTypeDropDown = false
                    },
                    label = {Text("Message-Type")},
                    trailingIcon = {
                        IconButton({msgTypeDropDown=!msgTypeDropDown}) {
                            Icon(Icons.Default.ArrowDropDown, "Open Message-Types")
                        }
                    }
                )
                DropdownMenu(msgTypeDropDown, {msgTypeDropDown=false}) {
                    DropdownMenuItem({Text("")}, {msgType = TextFieldValue("")})
                    DropdownMenuItem({Text("Error")}, {msgType = TextFieldValue("error")})
                    DropdownMenuItem({Text("Info")}, {msgType = TextFieldValue("info")})
                }
            }
        }
        Row {
            IconButton({onDeleteLog(itemType.text)}) {
                Icon(Icons.Default.Delete, "Delete log")
            }
        }
        logs.forEach { log ->
            Row(Modifier.background(colorBackground).padding(5.dp)) {
                Column(Modifier.weight(1f)) {
                    if(log.messageType == "error") {
                        Icon(Icons.Default.ErrorOutline, log.message, tint = colorForeground)
                    } else {
                        Icon(Icons.Default.Info, log.message, tint = colorForeground)
                    }
                }
                Column(Modifier.weight(1f)) {
                    if(log.itemType == "contacts") {
                        Icon(Icons.Default.Person, log.message, tint = colorForeground)
                    } else {
                        Icon(Icons.Default.CalendarMonth, log.message, tint = colorForeground)
                    }
                }
                Column(Modifier.weight(8f)) {
                    Row {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        Text(sdf.format(log.date), color = colorForeground)
                    }
                    Row {
                        Text(log.message, maxLines = 100, modifier = Modifier.basicMarquee(), color = colorForeground)
                    }
                }
                Column(Modifier.weight(2f)) {
                    Row {
                        Text(log.object1 ?: "", Modifier.basicMarquee(), color = colorForeground)
                    }
                    Row {
                        Text(log.object2 ?: "", Modifier.basicMarquee(), color = colorForeground)
                    }
                }
            }
        }

    }
}