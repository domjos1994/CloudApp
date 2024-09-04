/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.appbasics.custom

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import de.domjos.cloudapp2.appbasics.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked(date: Date, onValueChange: (Date) -> Unit, modifier: Modifier = Modifier, label: @Composable (() -> Unit)? = null, isError: Boolean = false, showTime: Boolean = true) {
    val cal = Calendar.getInstance(Locale.getDefault())
    cal.time = date
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(date.time)
    val timePickerState = rememberTimePickerState(cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), true)
    val selectedDate = datePickerState.selectedDateMillis?.let {
        val dateTime = it + (1000L * 60L * 60L * timePickerState.hour) + (1000L * 60L * timePickerState.minute)
        onValueChange(Date(dateTime))
        convertMillisToDate(context, dateTime)
    } ?: ""

    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {
                onValueChange(convertStringToDate(context, it))
            },
            label = label,
            readOnly = true,
            trailingIcon = {
                if(showTime) {
                    IconButton(onClick = { showTimePicker = !showTimePicker }) {
                        Icon(
                            painterResource(id = R.drawable.ic_time),
                            contentDescription = stringResource(R.string.date_picker_time)
                        )
                    }
                }
            },
            leadingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = stringResource(R.string.date_picker_date)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            isError = isError
        )

        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        showModeToggle = true
                    )
                }
            }
        }
        if (showTimePicker) {
            Popup(
                onDismissRequest = { showTimePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    TimePicker(
                        state = timePickerState
                    )
                }
            }
        }
    }
}

private fun convertMillisToDate(context: Context, millis: Long): String {
    val formatter = SimpleDateFormat(context.getString(R.string.sys_format), Locale.getDefault())
    return formatter.format(Date(millis))
}

private fun convertStringToDate(context: Context, date: String): Date {
    val formatter = SimpleDateFormat(context.getString(R.string.sys_format), Locale.getDefault())
    return formatter.parse(date) ?: Date()
}