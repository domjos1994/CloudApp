package de.domjos.cloudapp2.features.calendars.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.custom.DropDown
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.helper.Separator
import de.domjos.cloudapp2.appbasics.helper.Validator
import de.domjos.cloudapp2.appbasics.helper.openEvent
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
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
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val calendars by viewModel.calendars.collectAsStateWithLifecycle()
    val days by viewModel.days.collectAsStateWithLifecycle()
    val date by viewModel.date.collectAsStateWithLifecycle()

    val dt = Calendar.getInstance()
    dt.time = date
    val baseStart = Calendar.getInstance()
    baseStart.set(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH), 1)
    val baseEnd = Calendar.getInstance()
    baseEnd.set(
        dt.get(Calendar.YEAR),
        dt.get(Calendar.MONTH),
        dt.getActualMaximum(Calendar.DAY_OF_MONTH)
    )
    var start by remember { mutableLongStateOf(baseStart.time.time) }
    var end by remember { mutableLongStateOf(baseEnd.time.time) }
    var selectedCalendar by remember { mutableStateOf("") }
    viewModel.getCalendars()
    viewModel.load(selectedCalendar, start, end)
    viewModel.count(selectedCalendar, baseStart)
    val context = LocalContext.current

    viewModel.message.observe(LocalLifecycleOwner.current) {
        if(it != null) {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }

    CalendarScreen(events, colorBackground, colorForeground, date, calendars, days, viewModel.hasAuthentications(), toAuths, { mode, calendar ->
        val calStart = updateTime(0, 0, 0, calendar.clone() as Calendar)
        val calEnd = updateTime(23, 59, 59, calendar.clone() as Calendar)
        if(mode==Calendar.MONTH) {
            calStart.set(Calendar.DAY_OF_MONTH, 1)
            calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        start = calStart.time.time
        end = calEnd.time.time
        viewModel.load(selectedCalendar, start, end)
        viewModel.count(selectedCalendar, calStart)
    }, { item: CalendarEvent -> viewModel.insertCalendar(item) },
        { item: CalendarEvent -> viewModel.deleteCalendar(item)}, {selected -> selectedCalendar = selected})
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
    colorBackground: Color,
    colorForeground: Color,
    currentDate: Date,
    calendars: List<String>,
    countDays: List<Int>,
    hasAuths: Boolean, toAuths: () -> Unit,
    onChange: (Int, Calendar) -> Unit,
    onSave: (CalendarEvent) -> Unit,
    onDelete: (CalendarEvent) -> Unit,
    onCalendarSelected: (String) -> Unit) {

    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var event by remember { mutableStateOf<CalendarEvent?>(null) }
    var dt by remember { mutableStateOf(currentDate) }
    val initial = stringResource(id = R.string.calendars_all)
    var showEventView by remember { mutableStateOf(false) }
    val config = LocalConfiguration.current
    val isLandScape by remember { mutableStateOf(config.orientation == Configuration.ORIENTATION_LANDSCAPE) }


    if(showDialog) {
        EditDialog(
            event = event, date = dt, calendars = calendars,
            onShowItem = {showDialog=it},
            onSave = {
                onSave(it)
                showDialog = false
             }, onDelete = {
                 event = it
                 showDeleteDialog = true
                 showDialog = false
             })
    }
    if(showDeleteDialog) {
        ShowDeleteDialog({showDeleteDialog = it}, {onDelete(event!!)})
    }

    if(showEventView && event != null) {
        EventView(event = event!!) {showEventView=it}
    }

    if(!isLandScape) {
        ConstraintLayout(Modifier.fillMaxSize()) {
            val (dropdown, list, control) = createRefs()

            Column(Modifier.constrainAs(dropdown) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                width = Dimension.fillToConstraints
            }) {
                DropDown(calendars, initial, onCalendarSelected)
                Separator(colorForeground)
            }

            Column(Modifier.constrainAs(list) {
                top.linkTo(dropdown.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            }) {
                Row {
                    Calendar(
                        colorBackground,
                        colorForeground,
                        currentDate,
                        onChange = onChange,
                        countDays = countDays
                    ) {
                        //dt = it
                        event = null
                        showDialog = true
                    }
                }
                Row {
                    DateHeader(colorForeground, currentDate)
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (hasAuths) {
                            calendarEvents.forEach {
                                CalendarEventItem(
                                    it,
                                    colorBackground,
                                    colorForeground,
                                    { item: CalendarEvent ->
                                        event = item
                                        //dt = Date()
                                        showDialog = true
                                    }) { item: CalendarEvent ->
                                    event = item
                                    showEventView = true
                                }
                            }
                        } else {
                            NoAuthenticationItem(colorForeground, colorBackground, toAuths)
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
                modifier = Modifier
                    .constrainAs(control) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
                    .padding(5.dp)) {
                Icon(Icons.Filled.Add, stringResource(R.string.chats_room))
            }
        }
    } else {
        ConstraintLayout(Modifier.fillMaxSize()) {
            val (dropdown, list, control) = createRefs()

            Column(Modifier.constrainAs(dropdown) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.percent(0.5f)
            }) {
                Row {
                    DropDown(calendars, initial, onCalendarSelected)
                    Separator(colorForeground)
                }
                Row {
                    Calendar(
                        colorBackground,
                        colorForeground,
                        currentDate,
                        onChange = onChange,
                        countDays = countDays
                    ) {
                        //dt = it
                        event = null
                        showDialog = true
                    }
                }
            }

            Column(Modifier.constrainAs(list) {
                top.linkTo(parent.top)
                start.linkTo(dropdown.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.percent(0.5f)
            }) {
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row {
                            DateHeader(colorForeground, currentDate)
                        }

                        if (hasAuths) {
                            calendarEvents.forEach {
                                CalendarEventItem(
                                    it,
                                    colorBackground,
                                    colorForeground,
                                    { item: CalendarEvent ->
                                        event = item
                                        //dt = Date()
                                        showDialog = true
                                    }) { item: CalendarEvent ->
                                    event = item
                                    showEventView = true
                                }
                            }
                        } else {
                            NoAuthenticationItem(colorForeground, colorBackground, toAuths)
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
                modifier = Modifier
                    .constrainAs(control) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
                    .padding(5.dp)) {
                Icon(Icons.Filled.Add, stringResource(R.string.chats_room))
            }
        }
    }
}

@Composable
fun Calendar(
    colorBackground: Color,
    colorForeground: Color,
    currentDate: Date,
    calendar: Calendar = Calendar.getInstance(), onChange: (Int, Calendar) -> Unit, countDays: List<Int>, onClick: (Date) -> Unit) {
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
                }, colors = ButtonDefaults.buttonColors(containerColor = colorBackground, contentColor = colorForeground) ) {
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
                    Column(
                        Modifier
                            .weight(1f)
                            .height(50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Day(row, col, currentDate, dt, colorBackground, colorForeground, {selected ->
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
    }
}

@Composable
fun DateHeader(
    colorForeground: Color,
    date: Date
) {
    val format by remember { mutableStateOf("MM.yyyy") }
    val fsdf = SimpleDateFormat(format, Locale.getDefault())
    Column(
        Modifier
            .padding(5.dp)
            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(fsdf.format(date), fontWeight = FontWeight.Bold)
    }
    Separator(color = colorForeground)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Day(row: Int, col: Int, currentDate: Date, cal: Calendar, colorBackground: Color, colorForeground: Color, onSelected: (Calendar) -> Unit, countDays: List<Int>, onClick: (Date) -> Unit) {
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
    var borderWidth = 0.dp
    var borderColor = Color.Transparent

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

            bgColor = colorBackground
            color = colorForeground
        }
        val currentCal = Calendar.getInstance()
        currentCal.time = currentDate
        if(
            cal.get(Calendar.YEAR)==currentCal.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH)==currentCal.get(Calendar.MONTH) &&
            day==currentCal.get(Calendar.DAY_OF_MONTH)
        ) {
            borderWidth = 1.dp
            borderColor = colorBackground
        } else {
            borderWidth = 0.dp
            borderColor = Color.Transparent
        }
    }

    Column(
        Modifier
            .padding(1.dp)
            .background(bgColor)
            .width(55.dp)
            .height(55.dp)
            .border(borderWidth, borderColor)
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
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("$day", fontStyle = style, fontWeight = weight, color = color, modifier = Modifier
            .padding(5.dp)
            .semantics { contentDescription = cal.time.toString() })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarEventItem(calendarEvent: CalendarEvent, colorBackground: Color, colorForeground: Color, onClick: (CalendarEvent)->Unit, onLongClick: (CalendarEvent) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(5.dp)
            .background(colorBackground)
            .combinedClickable(
                onClick = { onLongClick(calendarEvent) },
                onLongClick = { onClick(calendarEvent) })) {
        Column(Modifier.weight(1f)) {
            Icon(Icons.Default.DateRange, calendarEvent.title, tint = colorForeground)
        }
        Column(Modifier.weight(if(calendarEvent.categories=="") 6f else 4f)) {
            Row {
                Text(calendarEvent.title, fontWeight = FontWeight.Bold, color = colorForeground)
            }
            Row {
                Text(calendarEvent.description, fontWeight = FontWeight.Normal, fontSize = 10.sp, color = colorForeground)
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
                                .background(colorForeground)) {
                            Text(tag.trim(),
                                Modifier.padding(2.dp),
                                color = colorBackground,
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
                    Modifier.padding(2.dp), colorForeground)
            }
            Row {
                Text(end,
                    Modifier.padding(2.dp), colorForeground)
            }
        }
    }
    Separator(color = colorForeground)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventView(event: CalendarEvent,showBottomSheet: (Boolean) -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { showBottomSheet(false) }) {

        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text(event.title, fontSize = 16.sp, fontStyle = FontStyle.Italic, fontWeight = FontWeight.Bold)
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(5.dp)) {
            Column(
                modifier = Modifier.weight(4f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(getFormattedDate(ts = event.from), fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("-", fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
            }
            Column(
                modifier = Modifier.weight(4f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(getFormattedDate(ts = event.to), fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
            }
        }
        if(event.location != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp)) {
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.LocationOn, event.location)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {}
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center) {
                    Text(event.location, fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
                }
            }
        }
        if(event.calendar != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp)) {
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.DateRange, event.calendar)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {}
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center) {
                    Text(event.calendar, fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
                }
            }
        }
        if(event.categories != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp)) {
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Info, event.categories)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {}
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center) {
                    Text(event.categories, fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
                }
            }
        }
        if(event.description != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Text(event.description, fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
                }
            }
        }

        if(event.eventId != "") {
            val context = LocalContext.current
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {openEvent(context, event.eventId)}) {
                        Text(stringResource(R.string.calendar_open))
                    }
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .padding(5.dp)) {}
    }
}

@Composable
private fun getFormattedDate(ts: Long): String {
    val dt = Date(ts)
    val zdt = dt.toInstant().atZone(ZoneId.systemDefault())
    val dFormat = SimpleDateFormat(stringResource(R.string.sys_format_date), Locale.getDefault())
    val dtFormat = SimpleDateFormat(stringResource(R.string.sys_format), Locale.getDefault())

    return if(zdt.hour == 0 && zdt.minute == 0 && zdt.second == 0) {
        dFormat.format(dt)
    } else {
        dtFormat.format(dt)
    }
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
                .verticalScroll(rememberScrollState())) {
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
                                    confirmation.text, categories.text, "", calendar, "", -1L, -1L, 0L)

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
        Calendar(onChange = {_,_->}, colorBackground = Color.Blue, colorForeground = Color.White, countDays = listOf(), onClick = {}, currentDate = Date())
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarItemPreview() {
    CalendarEventItem(fakeEvent(1), colorBackground = Color.Blue, colorForeground = Color.White, {}) {}
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EventViewPreview() {
    CloudAppTheme {
        EventView(event = fakeEvent(1L)) {

        }
    }
}

@Preview(showBackground = true)
@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420,isRound=false,chinSize=0dp,orientation=landscape"
)
@Composable
fun ScreenPreview() {
    CalendarScreen(listOf(fakeEvent(1), fakeEvent(2), fakeEvent(3)), colorBackground = Color.Blue, colorForeground = Color.White, Date(), listOf(), listOf(), true, {}, {_,_->}, {}, {}, {})
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
        "$id", id, id,
        0L
    )
}