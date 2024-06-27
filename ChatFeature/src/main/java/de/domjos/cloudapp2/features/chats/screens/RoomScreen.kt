package de.domjos.cloudapp2.features.chats.screens

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp2.rest.model.msg.Message
import de.domjos.cloudapp2.rest.model.room.Room
import de.domjos.cloudapp2.rest.model.room.Type
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.custom.AutocompleteTextField
import de.domjos.cloudapp2.appbasics.custom.DropDown
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.NoInternetItem
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.helper.ConnectionState
import de.domjos.cloudapp2.appbasics.helper.Separator
import de.domjos.cloudapp2.appbasics.helper.connectivityState
import de.domjos.cloudapp2.rest.model.user.User
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun RoomScreen(viewModel: RoomViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit, onChatScreen: (Int, String) -> Unit) {
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    val connection by connectivityState()
    val isConnected = connection === ConnectionState.Available

    if(isConnected) {
        viewModel.reload()
        viewModel.loadUsers()
    }

    val context = LocalContext.current
    viewModel.message.observe(LocalLifecycleOwner.current) {
        if(it != null) {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }

    RoomScreen(
        onSaveClick = {
            if(it.token == "") {
                viewModel.insertRoom(it, context)
            } else {
                viewModel.updateRoom(it)
            }
        },
        onDeleteClick = {
            viewModel.deleteRoom(it)
        },
        rooms = rooms, users, isConnected, viewModel.hasAuthentications(), toAuths, onChatScreen,
        colorBackground, colorForeground)
}

@Composable
fun RoomScreen(
    onSaveClick: (Room) -> Unit,
    onDeleteClick: (Room) -> Unit,
    rooms: List<Room>,
    users: List<User?>,
    isConnected: Boolean,
    hasAuths: Boolean, toAuths: () -> Unit,
    onChatScreen: (Int, String) -> Unit,
    colorBackground: Color,
    colorForeground: Color) {

    val showDialog =  remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf<Room?>(null) }

    if(showDialog.value) {
        EditDialog(
            room = selectedItem.value,
            users,
            setShowDialog = { showDialog.value = it },
            onSaveClick = {
                selectedItem.value = it
                onSaveClick(selectedItem.value!!)
            },
            onDeleteClick = {
                selectedItem.value = it
                showDeleteDialog.value = true
            }
        )
    }
    if(showDeleteDialog.value) {
        ShowDeleteDialog({showDeleteDialog.value = it}, {onDeleteClick(selectedItem.value!!)})
    }

    ConstraintLayout(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)) {
        val (list, control) = createRefs()

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .constrainAs(list) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                }) {
            if(hasAuths) {
                if(isConnected) {
                    rooms.forEach { room ->
                        RoomItem(room, colorBackground, colorForeground, {
                            selectedItem.value = it
                            onChatScreen(1, it.token)
                        }, {
                            selectedItem.value = it
                            showDialog.value = true
                        })
                    }
                } else {
                    NoInternetItem(colorForeground, colorBackground)
                }
            } else {
                NoAuthenticationItem(colorForeground, colorBackground, toAuths)
            }
        }

        if(isConnected && hasAuths) {
            FloatingActionButton(
                onClick = {
                    showDialog.value = true
                    selectedItem.value = null
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RoomItem(room: Room, colorBackground: Color, colorForeground: Color,
             onClick: (Room) -> Unit, onLongClick: (Room) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(colorBackground)
            .combinedClickable(
                onClick = { onClick(room) },
                onLongClick = { onLongClick(room) })
    ) {
        if(room.icon?.asImageBitmap()!=null) {
            Image(
                bitmap = room.icon?.asImageBitmap()!!,
                room.name,
                modifier = Modifier
                    .padding(5.dp)
                    .height(60.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(5.dp)))
        } else {
            Image(
                painterResource(R.drawable.baseline_person_24),
                room.name,
                modifier = Modifier
                    .padding(5.dp)
                    .height(60.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(5.dp)),
                colorFilter = ColorFilter.tint(colorForeground))
        }
        Column {
            val content: String = if(room.lastMessage.actorDisplayName!="") {
                "${room.lastMessage.actorDisplayName}: ${room.lastMessage.message}"
            } else {
                room.lastMessage.message
            }
            Text(room.displayName!!, fontWeight= FontWeight.Bold, modifier = Modifier.padding(1.dp),
                color = colorForeground)
            Text(
                room.lastMessage.getParameterizedMessage(content),
                modifier = Modifier
                    .padding(1.dp), maxLines = 1,
                fontStyle = if(room.unreadMessages==0) FontStyle.Normal else FontStyle.Italic,
                fontWeight = if(room.unreadMessages==0) FontWeight.Normal else FontWeight.Bold,
                color = colorForeground
            )
        }
    }
    Separator(color = colorForeground)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialog(
    room: Room?,
    users: List<User?>,
    setShowDialog: (Boolean) -> Unit,
    onSaveClick: (Room) -> Unit,
    onDeleteClick: (Room) -> Unit
) {
    var token by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(Type.OneToOne.name) }
    var invite by remember { mutableStateOf(room?.invite ?: "") }
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var expanded by remember { mutableStateOf(false) }

    if(room != null) {
        token = room.token
        type = Type.fromInt(room.type).name
        invite = room.invite
        name = TextFieldValue(room.displayName!!)
        description = TextFieldValue(room.description!!)
    }

    Dialog(onDismissRequest = {setShowDialog(false)}) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = {expanded=!expanded},
                        modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            readOnly = true,
                            value = type,
                            onValueChange = { },
                            label = { Text(stringResource(R.string.chats_rooms_type)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    expanded = expanded
                                )
                            },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                            }
                        ) {
                            Type.entries.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = {selectionOption.name},
                                    onClick = {
                                        type = selectionOption.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().height(70.dp)) {
                    AutocompleteTextField(
                        value = name,
                        onValueChange = {name = it},
                        label = { Text(stringResource(id = R.string.chats_rooms_name)) },
                        onAutoCompleteChange = {
                            val text = it.text
                            val lst = mutableListOf<String>()
                            users.filter { u -> u?.displayname?.contains(text)!! }.forEach { item ->
                                lst.add(item?.displayname!!)
                            }
                            lst
                        },
                        multi = true
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = {description = it},
                        label = {Text(stringResource(id = R.string.chats_rooms_description))},
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    val items = mutableListOf<String>()
                    users.forEach {items.add(it?.displayname?:"")}

                    DropDown(
                        items = items,
                        initial = invite,
                        label = stringResource(R.string.chats_rooms_participant),
                        onSelected = { selected ->
                        users.forEach {
                            if(it?.displayname == selected) {
                                invite = it.id
                            }
                        }
                    })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start) {

                    if(room?.token != null) {
                        Column(modifier = Modifier.weight(1F)) {
                            IconButton(onClick = {
                                onDeleteClick(room)
                                setShowDialog(false)
                            }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.login_delete))
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(9F)) {

                    }
                    Column(modifier = Modifier.weight(1F)) {
                        IconButton(onClick = { setShowDialog(false) }) {
                            Icon(Icons.Default.Close, stringResource(R.string.login_close))
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1F)) {
                        IconButton(onClick = {
                            val auth = Room(
                                0L,
                                token,
                                Type.valueOf(type).value,
                                invite,
                                name.text,
                                name.text,
                                description.text,
                                0, 0, "",
                                Message(0, "", "", "", "", 0L, "")
                            )

                            onSaveClick(auth)
                            setShowDialog(false)
                        }) {
                            Icon(Icons.Default.CheckCircle, stringResource(R.string.login_close))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialogNewPreview() {
    EditDialog(null, listOf(), {}, {}, {})
}

@Preview(showBackground = true)
@Composable
fun DialogUpdatePreview() {
    EditDialog(fakeRoom(1), listOf(), {}, {}, {})
}

@Preview(showBackground = true)
@Composable
fun RoomScreenPreview() {
    RoomScreen({}, {}, listOf(fakeRoom(0), fakeRoom(1), fakeRoom(2)), listOf(),
        isConnected = true,
        hasAuths = true, {},
        onChatScreen = { _, _->}, colorBackground = Color.Blue, colorForeground = Color.White
    )
}

@Preview(showBackground = true)
@Composable
fun RoomItemPreview() {
    RoomItem(fakeRoom(1), colorBackground = Color.Blue, colorForeground = Color.White, {}, {})
}

fun fakeRoom(no: Int): Room {
    val msg =
        Message(
            no,
            no.toString(),
            no.toString(),
            no.toString(),
            "test$no",
            no.toLong(),
            "This is a test$no!"
        )
    return Room(
        no.toLong(),
        "token$no",
        no, "",
        "Chat$no",
        "Group-Chat$no",
        "description$no",
        1,
        no,
        "dfsklghkgfd",
        msg
    )
}