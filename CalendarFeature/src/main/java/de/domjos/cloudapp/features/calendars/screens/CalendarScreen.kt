package de.domjos.cloudapp.features.calendars.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

}

@Composable
fun Calendar(dt: Calendar = Calendar.getInstance()) {
    val sdfMonth = SimpleDateFormat("MM.yyyy", Locale.getDefault())
    val next = dt.clone() as Calendar
    val previous = dt.clone() as Calendar
    next.add(Calendar.MONTH, 1)
    previous.add(Calendar.MONTH, -1)
    val firstDay = LocalDate.of(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH), 1)
    val start = firstDay.dayOfWeek.value

    Column(Modifier.verticalScroll(rememberScrollState())) {
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, sdfMonth.format(next.time))
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = { /*TODO*/ }) {
                    Text(sdfMonth.format(dt.time))
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                IconButton(onClick = { /*TODO*/ }) {
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
