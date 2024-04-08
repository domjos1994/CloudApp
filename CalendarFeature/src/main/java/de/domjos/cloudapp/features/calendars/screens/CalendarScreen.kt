package de.domjos.cloudapp.features.calendars.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.helper.Validator
import de.domjos.cloudapp.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp.database.model.calendar.CalendarEvent
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.LinkedList
import java.util.Locale
import java.util.UUID

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val calendars by viewModel.calendars.collectAsStateWithLifecycle()
    val days by viewModel.days.collectAsStateWithLifecycle()
    viewModel.getCalendars()

    if(events.isEmpty()) {
        val dt = Calendar.getInstance()
        val baseStart = Calendar.getInstance()
        baseStart.set(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH), 1)
        val baseEnd = Calendar.getInstance()
        baseEnd.set(
            dt.get(Calendar.YEAR),
            dt.get(Calendar.MONTH),
            dt.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        viewModel.load(baseStart.time.time, baseEnd.time.time)
    }

    CalendarScreen(events, calendars, days, { mode, calendar ->
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
    }, { item: CalendarEvent -> viewModel.insertCalendar(item)},
        { item: CalendarEvent -> viewModel.deleteCalendar(item)})
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
    calendars: List<String>,
    countDays: List<Int>,
    onChange: (Int, Calendar) -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onDelete: (CalendarEvent) -> Unit) {

    var showDialog by remember { mutableStateOf(false) }
    var event by remember { mutableStateOf<CalendarEvent?>(null) }
    var dt by remember { mutableStateOf(Date()) }

    if(showDialog) {
        EditDialog(
            event = event, date = dt, calendars = calendars,
            onShowItem = {showDialog=it},
            onSave = {
                onSave(it)
                showDialog = false
             }, onDelete = {
                 onDelete(it)
                showDialog = false
             })
    }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (list, control) = createRefs()

        Column(Modifier.constrainAs(list) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        }) {
            Row {
                Calendar(onChange = onChange, countDays = countDays) {
                    dt = it
                    event = null
                    showDialog = true
                }
            }
            Row {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .verticalScroll(rememberScrollState())) {
                    calendarEvents.forEach { CalendarEventItem(it) { item: CalendarEvent ->
                        event = item
                        dt = Date()
                        showDialog = true
                    }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                dt = Date()
                event = null
                showDialog = true
            },
            modifier = Modifier.constrainAs(control) {
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
            }
                .padding(5.dp)) {
            Icon(Icons.Filled.Add, stringResource(R.string.chats_room))
        }
    }
}

@Composable
fun Calendar(calendar: Calendar = Calendar.getInstance(), onChange: (Int, Calendar) -> Unit, countDays: List<Int>, onClick: (Date) -> Unit) {
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
                        }, countDays, onClick)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Day(row: Int, col: Int, cal: Calendar, onSelected: (Calendar) -> Unit, countDays: List<Int>, onClick: (Date) -> Unit) {
    // get first day
    val firstDayCal = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1)
    val firstDay = firstDayCal.dayOfWeek.value

    val previous = cal.clone() as Calendar
    previous.add(Calendar.MONTH, -1)
    val lastDayOfLastMonth = previous.getActualMaximum(Calendar.DAY_OF_MONTH)
    val lastDayOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = Calendar.getInstance()

    var style = FontStyle.Normal
    var color = MaterialTheme.colorScheme.primary
    var day = (row * 7) + (col - firstDay + 1)
    var bgColor = Color.Transparent
    var weight = FontWeight.Normal

    if(day <= 0) {
        day += lastDayOfLastMonth
        style = FontStyle.Italic
        color = MaterialTheme.colorScheme.secondary
    }else if(lastDayOfMonth < day) {
        day -= lastDayOfMonth
        style = FontStyle.Italic
        color = MaterialTheme.colorScheme.secondary
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
            .combinedClickable(
                onClick = {
                    val tmp = cal.clone() as Calendar
                    tmp.set(Calendar.DAY_OF_MONTH, day)
                    onSelected(tmp)
                },
                onLongClick = {
                    val tmp = cal.clone() as Calendar
                    tmp.set(Calendar.DAY_OF_MONTH, day)
                    onClick(tmp.time)
                }
            )) {
        Text("$day", fontStyle = style, fontWeight = weight, color = color, modifier = Modifier.padding(5.dp))
    }
}

@Composable
fun CalendarEventItem(calendarEvent: CalendarEvent, onClick: (CalendarEvent)->Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { onClick(calendarEvent) }) {
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
        val sdfFullDay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val from = GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(calendarEvent.from), ZoneId.systemDefault()))
        val to = GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(calendarEvent.to), ZoneId.systemDefault()))

        val start: String
        val end: String
        if(from.get(Calendar.HOUR) == 0 && from.get(Calendar.MINUTE) == 0 && to.get(Calendar.HOUR) == 0 && to.get(Calendar.MINUTE) == 0) {
            start = sdfFullDay.format(Date.from(Instant.ofEpochMilli(calendarEvent.from)))
            end = sdfFullDay.format(Date.from(Instant.ofEpochMilli(calendarEvent.to)))
        } else {
            start = sdf.format(Date.from(Instant.ofEpochMilli(calendarEvent.from)))
            end = sdf.format(Date.from(Instant.ofEpochMilli(calendarEvent.to)))
        }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialog(event: CalendarEvent?, date: Date?, calendars: List<String>, onShowItem: (Boolean) -> Unit, onSave: (CalendarEvent) -> Unit, onDelete: (CalendarEvent) -> Unit) {
    val fullDay = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val inDay = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    var def = date
    if(def == null) {
        def = Date()
    }

    var from by remember {mutableStateOf(TextFieldValue(fullDay.format(def)))}
    var isFromValid by remember { mutableStateOf(true) }
    var to by remember {mutableStateOf(TextFieldValue(fullDay.format(def)))}
    var isToValid by remember { mutableStateOf(true) }
    var title by remember {mutableStateOf(TextFieldValue(""))}
    var isTitleValid by remember { mutableStateOf(false) }
    var location by remember {mutableStateOf(TextFieldValue(""))}
    var description by remember {mutableStateOf(TextFieldValue(""))}
    var confirmation by remember {mutableStateOf(TextFieldValue(""))}
    var categories by remember {mutableStateOf(TextFieldValue(""))}
    var calendar by remember { mutableStateOf("") }

    if(event != null) {
        val calFrom = GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.from), ZoneId.systemDefault()))
        val calTo = GregorianCalendar.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(event.to), ZoneId.systemDefault()))
        if(calFrom.get(Calendar.HOUR) == 0 && calFrom.get(Calendar.MINUTE)==0 && calTo.get(Calendar.HOUR) == 0 && calTo.get(Calendar.MINUTE) == 0) {
            from = TextFieldValue(fullDay.format(calFrom.time))
            to = TextFieldValue(fullDay.format(calTo.time))
        } else {
            from = TextFieldValue(inDay.format(calFrom.time))
            to = TextFieldValue(inDay.format(calTo.time))
        }
        isFromValid = from.text.isNotEmpty()
        isToValid = to.text.isNotEmpty()

        title = TextFieldValue(event.title)
        isTitleValid = title.text.isNotEmpty()
        location = TextFieldValue(event.location)
        description = TextFieldValue(event.description)
        confirmation = TextFieldValue(event.confirmation)
        categories = TextFieldValue(event.categories)
        calendar = event.calendar
    }

    Dialog(onDismissRequest = {onShowItem(false)},
        DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)) {
        Surface(
            Modifier
                .padding(5.dp)
                .verticalScroll(rememberScrollState()),
            color = Color.White) {
            Column {
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title=it
                                isTitleValid = Validator.check(false, 3, 255, it.text)
                            },
                            label = {Text(stringResource(id = R.string.calendar_title))},
                            isError = !isTitleValid
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        OutlinedTextField(
                            value = from,
                            onValueChange = {
                                from=it
                                isFromValid = Validator.checkDate(it.text, "dd.MM.yyyy")
                            },
                            label = {Text(stringResource(id = R.string.calendar_from))},
                            isError = !isFromValid
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        OutlinedTextField(
                            value = to,
                            onValueChange = {
                                to=it
                                isToValid = Validator.checkDate(it.text, "dd.MM.yyyy")
                            },
                            label = {Text(stringResource(id = R.string.calendar_to))},
                            isError = !isToValid
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = {description=it},
                            label = {Text(stringResource(id = R.string.calendar_description))}
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        OutlinedTextField(
                            value = confirmation,
                            onValueChange = {confirmation=it},
                            label = {Text(stringResource(id = R.string.calendar_confirmation))}
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        OutlinedTextField(
                            value = categories,
                            onValueChange = {categories=it},
                            label = {Text(stringResource(id = R.string.calendar_categories))}
                        )
                    }
                }
                var expanded by remember { mutableStateOf(false) }

                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            }
                        ) {
                            TextField(
                                value = calendar,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                calendars.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            calendar = item
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Row {
                    if(event != null) {
                        Column(Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = { onDelete(event) }) {
                                Icon(Icons.Default.Delete, stringResource(id = R.string.calendar_delete))
                            }
                        }
                        Column(Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                var fromDate = isDate(inDay, from.text)
                                if(fromDate != null) {
                                    fromDate = isDate(fullDay, from.text)
                                }
                                var toDate = isDate(inDay, to.text)
                                if(toDate != null) {
                                    toDate = isDate(fullDay, to.text)
                                }
                                event.from = fromDate?.time ?: 0L
                                event.to = toDate?.time ?: 0L
                                event.title = title.text
                                event.description = description.text
                                event.confirmation = confirmation.text
                                event.location = location.text
                                event.categories = categories.text
                                event.calendar = calendar

                                onSave(event)
                            }, enabled = isTitleValid && isFromValid && isToValid) {
                                Icon(Icons.Default.Check, stringResource(id = R.string.calendar_save))
                            }
                        }
                    } else {
                        Column(Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = {
                                var fromDate = isDate(inDay, from.text)
                                if(fromDate != null) {
                                    fromDate = isDate(fullDay, from.text)
                                }
                                var toDate = isDate(inDay, to.text)
                                if(toDate != null) {
                                    toDate = isDate(fullDay, to.text)
                                }

                                val nEvent = CalendarEvent(
                                    0L, UUID.randomUUID().toString(),
                                    fromDate?.time ?: 0L, toDate?.time ?: 0L,
                                    title.text, location.text, description.text,
                                    confirmation.text, categories.text, "", calendar, 0L)

                                onSave(nEvent)
                            }, enabled = isTitleValid && isFromValid && isToValid) {
                                Icon(Icons.Default.Check, stringResource(id = R.string.calendar_save))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun isDate(sdf: SimpleDateFormat, dt: String): Date? {
    try {
        sdf.isLenient = false
        return sdf.parse(dt)
    } catch (_: Exception) {}
    return null
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    CloudAppTheme {
        Calendar(onChange = {_,_->}, countDays = listOf(), onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarItemPreview() {
    CalendarEventItem(fakeEvent(1)) {}
}

@Preview(showBackground = true)
@Composable
fun ScreenPreview() {
    CalendarScreen(listOf(fakeEvent(1), fakeEvent(2), fakeEvent(3)), listOf(), listOf(), {_,_->}, {}, {})
}

@Preview(showBackground = true)
@Composable
fun EditDialogPreview() {
    CloudAppTheme {
        EditDialog(event = fakeEvent(0L), date = Date(), onShowItem = {}, onSave = {}, calendars = listOf("Test1", "Test2")) {

        }
    }
}

private fun fakeEvent(id: Long):CalendarEvent {
    return CalendarEvent(id,
        "$id",
        Calendar.getInstance().time.time,
        Calendar.getInstance().time.time,
        "Test $id",
        "New York! $id",
        "This is a Test! $id",
        "Test $id",
        "tag 1, tags 2",
        "Green",
        "Calendar $id",
        0L
    )
}