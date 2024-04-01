package de.domjos.cloudapp.features.calendars.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.helper.Dialogs
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val days by viewModel.days.collectAsStateWithLifecycle()

    CalendarScreen(events, days) { mode, calendar ->
        val start = updateTime(0, 0, 0, calendar.clone() as Calendar)
        val end = updateTime(23, 59, 59, calendar.clone() as Calendar)
        if(mode==Calendar.MONTH) {
            start.set(Calendar.DAY_OF_MONTH, 1)
            end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
        }

        val startTime = start.time.time
        val endTime = end.time.time
        viewModel.load(startTime, endTime)
        viewModel.count(start)
    }
}

@Composable
fun importCalendarAction(viewModel: CalendarViewModel = hiltViewModel()): (updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit) -> Unit {
    val context = LocalContext.current
    return {onProgress, onFinish ->
        viewModel.reload(onProgress, onFinish,context.getString(R.string.calendar_import_calendar), context.getString(R.string.calendar_save_calendar))
    }
}

fun updateTime(hour: Int, minute: Int, seconds: Int, cal: Calendar) : Calendar {
    cal.set(Calendar.HOUR_OF_DAY, hour)
    cal.set(Calendar.MINUTE, minute)
    cal.set(Calendar.SECOND, seconds)
    return cal
}

@Composable
fun CalendarScreen(
    calendarEvents: List<CalendarEvent>,
    countDays: List<Int>,
    onChange: (Int, Calendar) -> Unit) {

    Column(Modifier.fillMaxSize()) {
        Row {
            Calendar(onChange = onChange, countDays = countDays)
        }
        Row {
            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())) {
                calendarEvents.forEach { CalendarEventItem(it) }
            }
        }
    }
}

@Composable
fun Calendar(calendar: Calendar = Calendar.getInstance(), onChange: (Int, Calendar) -> Unit, countDays: List<Int>) {
    var dt by remember { mutableStateOf(calendar) }
    var format by remember { mutableStateOf("MM.yyyy") }
    val sdfMonth = SimpleDateFormat("MM.yyyy", Locale.getDefault())
    val next = dt.clone() as Calendar
    val previous = dt.clone() as Calendar
    next.add(Calendar.MONTH, 1)
    previous.add(Calendar.MONTH, -1)

    Column {
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
                    format = "MM.yyyy"
                    onChange(Calendar.MONTH, dt)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, sdfMonth.format(next.time))
                }
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = {
                    val cal = Calendar.getInstance()
                    dt = cal
                    format = "MM.yyyy"
                    onChange(Calendar.MONTH, dt)
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
                    format = "MM.yyyy"
                    onChange(Calendar.MONTH, dt)
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, sdfMonth.format(previous.time))
                }
            }
        }
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            val days = LinkedList<DayOfWeek>()
            for(i in 0L..6L) {
                days.add(firstDayOfWeek.plus(i))
            }

            days.forEach { day ->
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day.name.substring(0, 2), fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color.Black)) {}
        var selectedDate by remember { mutableIntStateOf(dt.get(Calendar.DAY_OF_MONTH)) }
        for(row in 0..5) {Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()) {
                for (col in 0..6) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Day(row, col, dt, {selected ->
                            selectedDate = selected.get(Calendar.DAY_OF_MONTH)
                            format = "dd.MM.yyyy"
                            onChange(Calendar.DAY_OF_MONTH, selected)
                        }, countDays)
                    }
                }
            }
        }
        Row(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color.Black)) {}
        val fsdf = SimpleDateFormat(format, Locale.getDefault())
        val tmp = dt.clone() as Calendar
        tmp.set(Calendar.DAY_OF_MONTH, selectedDate)
        Column(
            Modifier
                .padding(5.dp)
                .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(fsdf.format(tmp.time), fontWeight = FontWeight.Bold)
        }
        Row(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(color = Color.Black)) {}
    }
}

@Composable
fun Day(row: Int, col: Int, cal: Calendar, onSelected: (Calendar) -> Unit, countDays: List<Int>) {
    // get first day
    val firstDayCal = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1)
    val firstDay = firstDayCal.dayOfWeek.value

    val previous = cal.clone() as Calendar
    previous.add(Calendar.MONTH, -1)
    val lastDayOfLastMonth = previous.getActualMaximum(Calendar.DAY_OF_MONTH)
    val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = Calendar.getInstance()

    var style = FontStyle.Normal
    var color = Color.Black
    var day = (row * 7) + (col - firstDay + 1)
    var bgColor = Color.Transparent
    var weight = FontWeight.Normal

    if(day <= 0) {
        day += lastDayOfLastMonth
        style = FontStyle.Italic
        color = Color.DarkGray
    }else if(lastDayOfMonth < day) {
        day -= lastDayOfMonth
        style = FontStyle.Italic
        color = Color.DarkGray
    } else {
        val temp = cal.clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, day)
        if(countDays.contains(day)) {
            weight = FontWeight.Bold
        }

        if(
            cal.get(Calendar.YEAR)==today.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH)==today.get(Calendar.MONTH) &&
            day==today.get(Calendar.DAY_OF_MONTH)) {

            bgColor = MaterialTheme.colorScheme.primaryContainer
        }
    }

    Column(
        Modifier
            .padding(1.dp)
            .background(bgColor)
            .clickable {
                val tmp = cal.clone() as Calendar
                tmp.set(Calendar.DAY_OF_MONTH, day)
                onSelected(tmp)
            }) {
        Text("$day", fontStyle = style, fontWeight = weight, color = color, modifier = Modifier.padding(5.dp))
    }
}

@Composable
fun CalendarEventItem(calendarEvent: CalendarEvent) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, calendarEvent.title)
        }
        Column(Modifier.weight(if(calendarEvent.categories=="") 6f else 4f)) {
            Row {
                Text(calendarEvent.title, fontWeight = FontWeight.Bold)
            }
            Row {
                Text(calendarEvent.description, fontWeight = FontWeight.Normal, fontSize = 10.sp)
            }
        }
        if(calendarEvent.categories != "") {
            Column(Modifier.weight(2f)) {
                Row {
                    calendarEvent.categories.split(",").forEach { tag ->
                        Column(
                            Modifier
                                .padding(1.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MaterialTheme.colorScheme.primary)) {
                            Text(tag.trim(),
                                Modifier.padding(2.dp),
                                color = MaterialTheme.colorScheme.inversePrimary,
                                fontSize = 8.sp)
                        }
                    }
                }
            }
        }
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val start = sdf.format(Date.from(Instant.ofEpochMilli(calendarEvent.from)))
        val end = sdf.format(Date.from(Instant.ofEpochMilli(calendarEvent.to)))


        Column(Modifier.weight(3f)) {
            Row {
                Text(start,
                    Modifier.padding(2.dp))
            }
            Row {
                Text(end,
                    Modifier.padding(2.dp))
            }
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.Black)) {}
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    Calendar(onChange = {_,_->}, countDays = listOf())
}

@Preview(showBackground = true)
@Composable
fun CalendarItemPreview() {
    CalendarEventItem(fakeEvent(1))
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    CalendarScreen(listOf(fakeEvent(1), fakeEvent(2), fakeEvent(3)), listOf()) { _, _ -> }
}


private fun fakeEvent(id: Long):CalendarEvent {
    return CalendarEvent(
        "$id",
        Calendar.getInstance().time.time,
        Calendar.getInstance().time.time,
        "Test $id",
        "New York! $id",
        "This is a Test! $id",
        "Test $id",
        "tag 1, tags 2",
        "Green",
        "Calendar $id"
    )
}