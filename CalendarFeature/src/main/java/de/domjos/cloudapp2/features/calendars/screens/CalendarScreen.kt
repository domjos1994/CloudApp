package de.domjos.cloudapp2.features.calendars.screens

import android.content.res.Configuration
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import de.domjos.cloudapp2.appbasics.custom.ActionItem
import de.domjos.cloudapp2.appbasics.custom.ComposeList
import de.domjos.cloudapp2.appbasics.custom.DatePickerDocked
import de.domjos.cloudapp2.appbasics.custom.DropDown
import de.domjos.cloudapp2.appbasics.custom.FAB
import de.domjos.cloudapp2.appbasics.custom.ListItem
import de.domjos.cloudapp2.appbasics.custom.MultiActionItem
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.custom.SplitView
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.appbasics.helper.Separator
import de.domjos.cloudapp2.appbasics.helper.Validator
import de.domjos.cloudapp2.appbasics.helper.openEvent
import de.domjos.cloudapp2.appbasics.ui.theme.CloudAppTheme
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.data.repository.dateToString
import de.domjos.cloudapp2.data.repository.stringToDate
import de.domjos.cloudapp2.data.repository.stringToOtherFormat
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Calendar
import java.util.Date
import java.util.LinkedList
import java.util.Locale

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
    viewModel.count()

    LogViewModel.Init(viewModel)

    val format = stringResource(R.string.sys_format)
    CalendarScreen(events, {
        val items = mutableListOf<ListItem<Long>>()
        viewModel.load(selectedCalendar, start, end)
        events.forEach { event ->
            val listItem = ListItem<Long>(
                title = event.title,
                description = "${stringToOtherFormat(event.string_from, format)} - ${stringToOtherFormat(event.string_to, format)}",
                Icons.Default.DateRange,
                selected = false,
                deletable = true
            )
            listItem.id = event.id
            items.add(listItem)
        }
        items
    }, colorBackground, colorForeground, date, calendars, days, viewModel.hasAuthentications(), toAuths, { mode, calendar ->
        val calStart = updateTime(0, 0, 0, calendar.clone() as Calendar)
        val calEnd = updateTime(23, 59, 59, calendar.clone() as Calendar)
        if(mode==Calendar.MONTH) {
            calStart.set(Calendar.DAY_OF_MONTH, 1)
            calEnd.set(Calendar.DAY_OF_MONTH, calEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
        }
        start = calStart.time.time
        end = calEnd.time.time
        viewModel.load(selectedCalendar, start, end)
        viewModel.count()
    }, { item: CalendarEvent -> viewModel.insertCalendar(item) },
        { item: CalendarEvent -> viewModel.deleteCalendar(item)}, {selected -> selectedCalendar = selected})
}

@Composable
fun importCalendarAction(viewModel: CalendarViewModel = hiltViewModel()): (updateProgress: (Float, String) -> Unit, finishProgress: () -> Unit) -> Unit {
    val context = LocalContext.current
    return {onProgress, onFinish ->
        viewModel.import(onProgress, onFinish, context)
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
    onReload: () -> MutableList<ListItem<Long>>,
    colorBackground: Color,
    colorForeground: Color,
    currentDate: Date,
    calendars: List<CalendarModel>,
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

    var showMultipleDeleteDialog by remember { mutableStateOf(false) }
    var events by remember { mutableStateOf<List<CalendarEvent>>(listOf()) }

    if(showDialog) {
        NewEditDialog(
            {showDialog=it},
            event = event,
            calendars = calendars,
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

            Column(
                Modifier
                    .constrainAs(dropdown) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
                    .padding(5.dp)) {
                DropDown(
                    calendars.map { it.label }, initial,
                    {onCalendarSelected(calendars.find { elem -> elem.label == it }!!.name)},
                    stringResource(R.string.calendars)
                )
            }

            Column(Modifier.constrainAs(list) {
                top.linkTo(dropdown.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            }) {
                SplitView(topView = {
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
                }, bottomView = {
                    Row(Modifier.padding(5.dp)) {
                        DateHeader(colorForeground, currentDate)
                    }
                    Row(Modifier.padding(5.dp)) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        ) {
                            if (hasAuths) {
                                val painter = painterResource(R.drawable.ic_eye)
                                ComposeList(
                                    onReload = onReload,
                                    colorBackground = colorBackground,
                                    colorForeground = colorForeground,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(5.dp),
                                    needsInternet = false,
                                    onSwipeToStart = ActionItem(
                                        name = "Delete Item",
                                        Icons.Default.Delete,
                                        action = {listItem ->
                                            val calendarEvent = calendarEvents.find { it.id == listItem.id }
                                            if(calendarEvent != null) {
                                                event = calendarEvent
                                                showDeleteDialog = true
                                                true
                                            } else {false}
                                        },
                                        color = Color.Red
                                    ),
                                    actions = listOf(
                                        ActionItem(
                                            name = "Show Item",
                                            painter = painter,
                                            action = {listItem ->
                                                val calendarEvent = calendarEvents.find { it.id == listItem.id }
                                                if(calendarEvent != null) {
                                                    event = calendarEvent
                                                    showEventView = true
                                                    true
                                                } else {false}
                                            }
                                        ),
                                        ActionItem(
                                            name = "Edit Item",
                                            icon = Icons.Default.Edit,
                                            action = {listItem ->
                                                val calendarEvent = calendarEvents.find { it.id == listItem.id }
                                                if(calendarEvent != null) {
                                                    event = calendarEvent
                                                    showDialog = true
                                                    true
                                                } else {false}
                                            }
                                        )
                                    ),
                                    multiActions = listOf(
                                        MultiActionItem(
                                            name = "Delete Items",
                                            icon = Icons.Default.Delete,
                                            action = { selectedItems ->
                                                val items = calendarEvents.filter {
                                                    it.id == (selectedItems.find { ite -> ite.id == it.id }?.id
                                                        ?: 0)
                                                }
                                                events = items
                                                showMultipleDeleteDialog = true
                                                true
                                            }
                                        )
                                    )
                                )
                            } else {
                                NoAuthenticationItem(colorForeground, colorBackground, toAuths)
                            }
                        }
                    }
                })
            }

            FAB(
                Icons.Filled.Add,
                stringResource(R.string.calendars),
                colorBackground,
                colorForeground,
                modifier = Modifier
                    .constrainAs(control) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }
            ) {
                dt = Date()
                event = null
                showDialog = true
            }
        }
    } else {
        ConstraintLayout(Modifier.fillMaxSize()) {
            val (dropdown, list, control) = createRefs()

            Column(
                Modifier
                    .constrainAs(dropdown) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                        height = Dimension.fillToConstraints
                        width = Dimension.percent(0.5f)
                    }
                    .verticalScroll(rememberScrollState())) {
                Row {
                    DropDown(calendars.map { it.name }, initial, onCalendarSelected, stringResource(R.string.calendars))
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
                            val painter = painterResource(R.drawable.ic_eye)
                            ComposeList(
                                onReload = onReload,
                                colorBackground = colorBackground,
                                colorForeground = colorForeground,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(5.dp),
                                needsInternet = false,
                                onSwipeToStart = ActionItem(
                                    name = "Delete Item",
                                    Icons.Default.Delete,
                                    action = {listItem ->
                                        val calendarEvent = calendarEvents.find { it.id == listItem.id }
                                        if(calendarEvent != null) {
                                            event = calendarEvent
                                            showDeleteDialog = true
                                            true
                                        } else {false}
                                    },
                                    color = Color.Red
                                ),
                                actions = listOf(
                                    ActionItem(
                                        name = "Show Item",
                                        painter = painter,
                                        action = {listItem ->
                                            val calendarEvent = calendarEvents.find { it.id == listItem.id }
                                            if(calendarEvent != null) {
                                                event = calendarEvent
                                                showEventView = true
                                                true
                                            } else {false}
                                        }
                                    ),
                                    ActionItem(
                                        name = "Edit Item",
                                        icon = Icons.Default.Edit,
                                        action = {listItem ->
                                            val calendarEvent = calendarEvents.find { it.id == listItem.id }
                                            if(calendarEvent != null) {
                                                event = calendarEvent
                                                showDialog = true
                                                true
                                            } else {false}
                                        }
                                    )
                                ),
                                multiActions = listOf(
                                    MultiActionItem(
                                        name = "Delete Items",
                                        icon = Icons.Default.Delete,
                                        action = { selectedItems ->
                                            val items = calendarEvents.filter {
                                                it.id == (selectedItems.find { ite -> ite.id == it.id }?.id
                                                    ?: 0)
                                            }
                                            events = items
                                            showMultipleDeleteDialog = true
                                            true
                                        }
                                    )
                                )
                            )
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
                    .padding(5.dp),
                containerColor = colorForeground) {
                Icon(
                    Icons.Filled.Add,
                    stringResource(R.string.chats_room),
                    tint = colorBackground)
            }
        }
    }
}

@Composable
fun Calendar(
    colorBackground: Color,
    colorForeground: Color,
    currentDate: Date,
    calendar: Calendar = Calendar.getInstance(Locale.getDefault()), onChange: (Int, Calendar) -> Unit, countDays: List<Int>, onClick: (Date) -> Unit) {
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
                }, colors = IconButtonDefaults.iconButtonColors(containerColor = colorBackground, contentColor = colorForeground) ) {
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
                }, colors = IconButtonDefaults.iconButtonColors(containerColor = colorBackground, contentColor = colorForeground) ) {
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
                    val label = try {
                        day.getDisplayName(TextStyle.SHORT, Locale.getDefault()).substring(0, 2)
                    } catch (_: Exception) {""}
                    Text(label, fontWeight = FontWeight.Bold)
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
    val dtMonth = SimpleDateFormat(format, Locale.getDefault())
    Column(
        Modifier
            .padding(5.dp)
            .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(dtMonth.format(date), fontWeight = FontWeight.Bold)
    }
    Separator(color = colorForeground)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Day(row: Int, col: Int, currentDate: Date, cal: Calendar, colorBackground: Color, colorForeground: Color, onSelected: (Calendar) -> Unit, countDays: List<Int>, onClick: (Date) -> Unit) {
    // get first day
    val firstDayCal = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, 1)
    val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek.value
    val firstDay = firstDayCal.dayOfWeek.value - (firstDayOfWeek % 7)

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
            //cal.get(Calendar.YEAR)==currentCal.get(Calendar.YEAR) &&
            //cal.get(Calendar.MONTH)==currentCal.get(Calendar.MONTH) &&
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
                    if (style != FontStyle.Italic) {
                        val tmp = cal.clone() as Calendar
                        tmp.set(Calendar.DAY_OF_MONTH, day)
                        onSelected(tmp)
                    }
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

fun readableDate(data: String): String {
    val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val dateTimeFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault())
    var date: Date? = null
    try {
        date = dateTimeFormat.parse(data)
    } catch (_: Exception) {
        try {
            date = dateFormat.parse(data)
        } catch (_: Exception) {}
    }
    if(date != null) {
        var time = " HH:mm:ss"
        val cal = Calendar.getInstance(Locale.getDefault())
        cal.time = date
        if(cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
            time = ""
        }

        if(Locale.getDefault() == Locale.GERMANY) {
            val readableFormat = SimpleDateFormat("dd.MM.yyyy$time", Locale.getDefault())
            return readableFormat.format(date)
        } else {
            val readableFormat = SimpleDateFormat("yyyy-MM-dd$time", Locale.getDefault())
            return readableFormat.format(date)
        }
    }
    return data
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
            val start = readableDate(event.string_from)
            val end = readableDate(event.string_to)
            if(start == end) {
                Column(
                    modifier = Modifier.weight(9f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(start, fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
                }
            } else {
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        start,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "-",
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal
                    )
                }
                Column(
                    modifier = Modifier.weight(4f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        end,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Normal,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
        HorizontalDivider()
        if(event.location != "") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(5.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.LocationOn, event.location)
                }
                Column(
                    modifier = Modifier.weight(9f),
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
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.DateRange, event.calendar)
                }
                Column(
                    modifier = Modifier.weight(9f),
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
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Filled.Info, event.categories)
                }
                Column(
                    modifier = Modifier.weight(9f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center) {
                    Text(event.categories, fontSize = 14.sp, fontStyle = FontStyle.Normal, fontWeight = FontWeight.Normal)
                }
            }
        }
        HorizontalDivider()
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
        HorizontalDivider()
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
fun NewEditDialog(
    onShowDialog: (Boolean) -> Unit,
    event: CalendarEvent?,
    calendars: List<CalendarModel>,
    onSave: (CalendarEvent) -> Unit,
    onDelete: (CalendarEvent) -> Unit) {

    val id = event?.id ?: 0L
    val uid = event?.uid ?: ""
    val path = event?.path ?: ""

    var title by remember { mutableStateOf(TextFieldValue(event?.title ?: "")) }
    var isTitleValid by remember { mutableStateOf(event?.title?.isNotEmpty() ?: false ) }

    val dtFrom = stringToDate(event?.string_from ?: "")
    val dtTo= stringToDate(event?.string_to ?: "")

    var wholeDay by remember { mutableStateOf(false) }
    var from by remember { mutableStateOf(dtFrom) }
    var isFromValid by remember { mutableStateOf(true) }

    var to by remember { mutableStateOf(dtTo) }
    var isToValid by remember { mutableStateOf(true) }

    var description by remember { mutableStateOf(TextFieldValue(event?.description ?: "")) }
    var location by remember { mutableStateOf(TextFieldValue(event?.location ?: "")) }
    var confirmation by remember { mutableStateOf(TextFieldValue(event?.confirmation ?: "")) }
    var categories by remember { mutableStateOf(TextFieldValue(event?.categories ?: "")) }
    var calendar by remember {
        mutableStateOf(
            TextFieldValue(calendars.find { it.name == (event?.calendar ?: "") }?.label ?: "")
        )
    }
    var isCalendarValid by remember { mutableStateOf(calendar.text.isNotEmpty()) }
    var showDropDown by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = {onShowDialog(false)},
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
        Surface(
            Modifier
                .padding(5.dp)
                .verticalScroll(rememberScrollState())) {

            Column {
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            trailingIcon = {
                                IconButton(onClick = { title = TextFieldValue("") }) {
                                    Icon(Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.sys_clear_text)
                                    )
                                }
                            },
                            onValueChange = {
                                title = it
                                isTitleValid = Validator.check(false, 3, 255, it.text)
                            },
                            label = { Text(stringResource(id = R.string.calendar_title)) },
                            isError = !isTitleValid
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        DatePickerDocked(
                            date = from,
                            onValueChange = {
                                from = it
                                isFromValid = if(!wholeDay) {
                                    from.before(to)
                                } else {
                                    true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = {Text(stringResource(R.string.calendar_from))},
                            isError = !isFromValid
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        if(!wholeDay) {
                            DatePickerDocked(
                                date = to,
                                onValueChange = {
                                    to = it
                                    isToValid = to.after(from)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.calendar_to)) },
                                isError = !isToValid
                            )
                        } else {
                            isToValid = true
                        }
                    }
                }
                Row {
                    Column(
                        Modifier
                            .weight(8f)
                            .padding(5.dp)) {
                        Text(stringResource(R.string.calendar_whole_day))
                    }
                    Column(
                        Modifier
                            .weight(2f)
                            .padding(5.dp)
                    ) {
                        Switch(
                            checked = wholeDay,
                            onCheckedChange = {wholeDay = it}
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        OutlinedTextField(
                            value = description,
                            trailingIcon = {
                                IconButton(onClick = { description = TextFieldValue("") }) {
                                    Icon(Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.sys_clear_text)
                                    )
                                }
                            },
                            onValueChange = {
                                description = it
                            },
                            label = { Text(stringResource(id = R.string.calendar_description)) },
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        OutlinedTextField(
                            value = location,
                            trailingIcon = {
                                IconButton(onClick = { location = TextFieldValue("") }) {
                                    Icon(Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.sys_clear_text)
                                    )
                                }
                            },
                            onValueChange = {
                                location = it
                            },
                            label = { Text(stringResource(id = R.string.calendar_location)) },
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        OutlinedTextField(
                            value = confirmation,
                            trailingIcon = {
                                IconButton(onClick = { confirmation = TextFieldValue("") }) {
                                    Icon(Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.sys_clear_text)
                                    )
                                }
                            },
                            onValueChange = {
                                confirmation = it
                            },
                            label = { Text(stringResource(id = R.string.calendar_confirmation)) },
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        OutlinedTextField(
                            value = categories,
                            trailingIcon = {
                                IconButton(onClick = { categories = TextFieldValue("") }) {
                                    Icon(Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.sys_clear_text)
                                    )
                                }
                            },
                            onValueChange = {
                                categories = it
                            },
                            label = { Text(stringResource(id = R.string.calendar_categories)) },
                        )
                    }
                }
                Row {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(5.dp)
                    ) {
                        OutlinedTextField(
                            value = calendar,
                            trailingIcon = {
                                IconButton(onClick = { showDropDown = !showDropDown }) {
                                    Icon(Icons.Default.ArrowDropDown,
                                        contentDescription = stringResource(R.string.calendars)
                                    )
                                }
                            },
                            onValueChange = {
                                calendar = it
                            },
                            label = {Text(stringResource(id = R.string.calendars))},
                            isError = !isCalendarValid
                        )
                        DropdownMenu(showDropDown, {showDropDown = false}) {
                            calendars.forEach {
                                DropdownMenuItem({Text(it.label)}, onClick = {
                                    calendar = TextFieldValue(it.label)
                                    isCalendarValid = calendar.text.isNotEmpty()
                                    showDropDown = false
                                })
                            }
                        }
                    }
                }
                HorizontalDivider()
                Row {
                    Column(Modifier.weight(1f)) {
                        if(id != 0L && uid.isNotEmpty() && isTitleValid && isFromValid && isToValid) {
                            IconButton(onClick = {
                                val eventId = event?.eventId ?: ""
                                val authId = event?.authId ?: 0L
                                val lastUpdatedApp = event?.lastUpdatedEventApp ?: -1L
                                val lastUpdatedPhone = event?.lastUpdatedEventPhone ?: -1L
                                val lastUpdatedServer = event?.lastUpdatedEventServer ?: -1
                                val cName = calendars.find { it.label == calendar.text }?.name ?: ""

                                val calendarEvent = CalendarEvent(
                                    id, uid, dateToString(from), dateToString(to), title.text,
                                    location.text, description.text, confirmation.text,
                                    categories.text, "", cName, eventId,
                                    lastUpdatedPhone, lastUpdatedServer,
                                    authId, path
                                )
                                calendarEvent.lastUpdatedEventApp = lastUpdatedApp

                                onDelete(calendarEvent)
                                onShowDialog(false)
                            }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.sys_delete_item))
                            }
                        }
                    }
                    Column(Modifier.weight(7f)) {}
                    Column(Modifier.weight(1f)) {
                        IconButton(onClick = {
                            onShowDialog(false)
                        }) {
                            Icon(Icons.Default.Clear, stringResource(R.string.sys_delete_item))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        IconButton(onClick = {
                            if(wholeDay) {
                                val cal = Calendar.getInstance(Locale.getDefault())
                                cal.timeInMillis = from.time
                                cal.set(Calendar.HOUR, 0)
                                cal.set(Calendar.MINUTE, 0)
                                from = cal.time
                                to  = cal.time
                            }
                            val eventId = event?.eventId ?: ""
                            val authId = event?.authId ?: 0L
                            val lastUpdatedApp = event?.lastUpdatedEventApp ?: -1L
                            val lastUpdatedPhone = event?.lastUpdatedEventPhone ?: -1L
                            val lastUpdatedServer = event?.lastUpdatedEventServer ?: -1
                            val cName = calendars.find { it.label == calendar.text }?.name ?: ""

                            val calendarEvent = CalendarEvent(
                                id, uid, dateToString(from), dateToString(to), title.text,
                                location.text, description.text, confirmation.text,
                                categories.text, "", cName, eventId,
                                lastUpdatedPhone, lastUpdatedServer,
                                authId, path
                            )
                            calendarEvent.lastUpdatedEventApp = lastUpdatedApp

                            onSave(calendarEvent)
                            onShowDialog(false)
                        }, enabled = isTitleValid && isFromValid && isToValid && isCalendarValid) {
                            Icon(Icons.Default.Check, stringResource(R.string.sys_delete_item))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarPreview() {
    CloudAppTheme {
        Calendar(onChange = {_,_->}, colorBackground = Color.Blue, colorForeground = Color.White, countDays = listOf(), onClick = {}, currentDate = Date())
    }
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
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScreenPreview() {
    val lst = mutableListOf(fakeEvent(1), fakeEvent(2), fakeEvent(3))
    CalendarScreen(lst, { mutableListOf() }, colorBackground = Color.Blue, colorForeground = Color.White, Date(), listOf(), listOf(), true, {}, { _, _->}, {}, {}, {})
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditDialogPreview() {
    CloudAppTheme {
        NewEditDialog(
            onShowDialog = {},
            event = fakeEvent(0L), onSave = {}, calendars =
            listOf(
                CalendarModel("Test1", "", ""),
                CalendarModel("Test2", "", "")
            )
        ) {

        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BottomSheetPreview() {
    CloudAppTheme {
        EventView(fakeEvent(0L)) { }
    }
}

private fun fakeEvent(id: Long):CalendarEvent {
    return CalendarEvent(id,
        "$id",
        dateToString(Calendar.getInstance().time),
        dateToString(Calendar.getInstance().time),
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