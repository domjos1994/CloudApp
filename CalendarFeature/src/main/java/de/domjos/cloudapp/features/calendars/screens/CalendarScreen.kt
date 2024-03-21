package de.domjos.cloudapp.features.calendars.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val calendarEvents by produceState<CalendarUiState>(
        initialValue = CalendarUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }

    if (
        calendarEvents is CalendarUiState.Success) {

        val context = LocalContext.current

        CalendarScreen((calendarEvents as CalendarUiState.Success).data) { onProgress, onFinish ->
            viewModel.reload(onProgress, onFinish, context.getString(R.string.calendar_import_calendar), context.getString(R.string.calendar_save_calendar))
        }
    }
}

@Composable
fun CalendarScreen(calendarEvents: List<CalendarEvent>, onReload: (updateProgress: (Float, String) -> Unit, onFinish: () -> Unit) -> Unit) {
    LinearDeterminateIndicator(onReload)
    Calendar()
}

@Composable
fun Calendar(calendar: Calendar = Calendar.getInstance()) {
    var dt by remember { mutableStateOf(calendar) }
    val sdfMonth = SimpleDateFormat("MM.yyyy", Locale.GERMAN)
    val next = dt.clone() as Calendar
    val previous = dt.clone() as Calendar
    next.add(Calendar.MONTH, 1)
    previous.add(Calendar.MONTH, -1)
    val firstDay = LocalDate.of(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH), 1)
    val start = firstDay.dayOfWeek.value
    val lastDay = dt.getActualMaximum(Calendar.DAY_OF_MONTH)

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH), dt.get(Calendar.DAY_OF_MONTH))
                    cal.add(Calendar.MONTH, -1)
                    dt = cal
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, sdfMonth.format(next.time))
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    dt = cal
                }) {
                    Text(sdfMonth.format(dt.time))
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                IconButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH), dt.get(Calendar.DAY_OF_MONTH))
                    cal.add(Calendar.MONTH, 1)
                    dt = cal
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, sdfMonth.format(previous.time))
                }
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.calendar_mo), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.calendar_tu), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.calendar_we), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.calendar_th), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.calendar_fr), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.calendar_sa), fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.calendar_su), fontWeight = FontWeight.Bold)
            }
        }
        Row(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color.Black)) {}
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(start==0) "1" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(start<=1) "${2 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(start<=2) "${3 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(start<=3) "${4 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(start<=4) "${5 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(start<=5) "${6 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(start<=6) "${7 - start}" else "")
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(7-start<lastDay) "${8 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(8-start<lastDay) "${9 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(9-start<lastDay) "${10 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(10-start<lastDay) "${11 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(11-start<lastDay) "${12 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(12-start<lastDay) "${13 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(13-start<lastDay) "${14 - start}" else "")
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(14-start<lastDay) "${15 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(15-start<lastDay) "${16 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(16-start<lastDay) "${17 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(17-start<lastDay) "${18 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(18-start<lastDay) "${19 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(19-start<lastDay) "${20 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(20-start<lastDay) "${21 - start}" else "")
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(21-start<lastDay) "${22 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(22-start<lastDay) "${23 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(23-start<lastDay) "${24 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(24-start<lastDay) "${25 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(25-start<lastDay) "${26 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(26-start<lastDay) "${27 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(27-start<lastDay) "${28 - start}" else "")
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(28-start<lastDay) "${29 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(29-start<lastDay) "${30 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(30-start<lastDay) "${31 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(31-start<lastDay) "${32 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(32-start<lastDay) "${33 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(33-start<lastDay) "${34 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(34-start<lastDay) "${35 - start}" else "")
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(35-start<lastDay) "${36 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(36-start<=lastDay) "${37 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(37-start<=lastDay) "${38 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(38-start<=lastDay) "${39 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(39-start<=lastDay) "${40 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(40-start<=lastDay) "${41 - start}" else "")
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if(41-start<=lastDay) "${42 - start}" else "")
            }
        }
        Row(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color.Black)) {}
    }
}

@Composable
fun LinearDeterminateIndicator(onReload: (updateProgress: (Float, String) -> Unit, () -> Unit) -> Unit) {
    var currentProgress by remember { mutableFloatStateOf(0f) }
    var currentCalendar by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(onClick = {
            loading = true
            onReload({ progress, calendar ->
                currentProgress = progress
                currentCalendar = calendar
            }, {loading = false})
        }, enabled = !loading) {
            Text(stringResource(R.string.calendar_import))
        }

        if (loading) {
            Text(currentCalendar, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    Calendar()
}
