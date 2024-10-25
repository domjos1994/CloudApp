package de.domjos.cloudapp2.features.chats.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import de.domjos.cloudapp2.appbasics.custom.ActionItem
import de.domjos.cloudapp2.appbasics.custom.AutocompleteTextField
import de.domjos.cloudapp2.appbasics.custom.ComposeList
import de.domjos.cloudapp2.appbasics.custom.DropDown
import de.domjos.cloudapp2.appbasics.custom.FAB
import de.domjos.cloudapp2.appbasics.custom.ListItem
import de.domjos.cloudapp2.appbasics.custom.NoAuthenticationItem
import de.domjos.cloudapp2.appbasics.custom.NoInternetItem
import de.domjos.cloudapp2.appbasics.custom.ShowDeleteDialog
import de.domjos.cloudapp2.appbasics.helper.ConnectivityViewModel
import de.domjos.cloudapp2.rest.model.user.User

@Composable
fun RoomScreen(viewModel: RoomViewModel = hiltViewModel(), colorBackground: Color, colorForeground: Color, toAuths: () -> Unit, onChatScreen: (Int, String) -> Unit) {
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    ConnectivityViewModel.Init(viewModel)

    RoomScreen(
        onSaveClick = {
            if(it.token == "") {
                viewModel.insertRoom(it)
            } else {
                viewModel.updateRoom(it)
            }
        },
        onDeleteClick = {
            viewModel.deleteRoom(it)
        },
        onReload = {
            viewModel.reload()
            val items = mutableListOf<ListItem<Long>>()
            rooms.forEach {
                var icon: ImageVector? = null
                var image: ImageBitmap? = null
                if(it.icon != null) {
                    image = it.icon!!.asImageBitmap()
                } else {
                    icon = Icons.Default.AccountBox
                }

                val item = ListItem<Long>(it.name ?: "", "${it.lastMessage.actorDisplayName}: ${it.lastMessage.message}", icon = icon, image = image)
                item.id = it.id
                items.add(item)
            }
            items
        },
        rooms = rooms, users, viewModel.isConnected(), viewModel.hasAuthentications(), toAuths, onChatScreen,
        colorBackground, colorForeground)
}

@Composable
fun RoomScreen(
    onSaveClick: (Room) -> Unit,
    onDeleteClick: (Room) -> Unit,
    onReload: () -> MutableList<ListItem<Long>>,
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
        .fillMaxSize()) {
        val (list, control) = createRefs()

        Column(
            Modifier
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
                    val painter = painterResource(R.drawable.ic_eye)
                    ComposeList(
                        onReload = onReload,
                        colorBackground = colorBackground,
                        colorForeground = colorForeground,
                        modifier = Modifier
                            .fillMaxSize(),
                        needsInternet = true,
                        onSwipeToStart = ActionItem(
                            name = stringResource(R.string.sys_list_delete),
                            icon = Icons.Default.Delete,
                            action = {item ->
                                val room = rooms.find { item.id == it.id }
                                if(room != null) {
                                    selectedItem.value = room
                                    showDeleteDialog.value = true
                                    true
                                } else { false }
                            }
                        ),
                        actions = listOf(
                            ActionItem(
                                name = stringResource(R.string.sys_list_show),
                                painter = painter,
                                action = { item ->
                                    val room = rooms.find { item.id == it.id }
                                    if(room != null) {
                                        selectedItem.value = room
                                        onChatScreen(1, room.token)
                                        true
                                    } else { false }
                                }
                            ),
                            ActionItem(
                                name = stringResource(R.string.sys_list_edit),
                                icon = Icons.Default.Edit,
                                action = { item ->
                                    val room = rooms.find { item.id == it.id }
                                    if(room != null) {
                                        selectedItem.value = room
                                        showDialog.value = true
                                        true
                                    } else { false }
                                }
                            )
                        )
                    )
                } else {
                    NoInternetItem(colorForeground, colorBackground)
                }
            } else {
                NoAuthenticationItem(colorForeground, colorBackground, toAuths)
            }
        }

        if(isConnected && hasAuths) {
            FAB(
                Icons.Filled.Add,
                stringResource(R.string.chats_room),
                colorBackground,
                colorForeground,
                Modifier
                    .constrainAs(control) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                        width = Dimension.fillToConstraints
                    }

            ) {
                showDialog.value = true
                selectedItem.value = null
            }
        }
    }
}

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
                    DropDown(
                        items = Type.entries.toList().map { it.name },
                        initial = Type.FormerOneToOne.name,
                        onSelected = { type = it },
                        label = stringResource(R.string.chats_rooms_type)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth()) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.Start) {

                    if(room?.token != null) {
                        Column(modifier = Modifier
                            .weight(2F)
                            .height(60.dp)
                            .width(60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            IconButton(onClick = {
                                onDeleteClick(room)
                                setShowDialog(false)
                            },
                                Modifier
                                    .height(50.dp)
                                    .width(50.dp)) {
                                Icon(
                                    Icons.Default.Delete,
                                    stringResource(R.string.login_delete),
                                    Modifier
                                        .height(50.dp)
                                        .width(50.dp)
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(9F)) {}
                    Column(modifier = Modifier
                        .weight(2F)
                        .height(60.dp)
                        .width(60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                        IconButton(onClick = {
                            setShowDialog(false)
                        },
                            Modifier
                                .height(50.dp)
                                .width(50.dp)) {
                            Icon(
                                Icons.Default.Close,
                                stringResource(R.string.login_close),
                                Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .weight(2F)
                            .height(60.dp)
                            .width(60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
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
                        },
                            Modifier
                                .height(50.dp)
                                .width(50.dp)) {
                            Icon(
                                Icons.Default.CheckCircle,
                                stringResource(R.string.login_close),
                                Modifier
                                    .height(50.dp)
                                    .width(50.dp)
                            )
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
    RoomScreen({}, {}, { mutableListOf() }, listOf(fakeRoom(0), fakeRoom(1), fakeRoom(2)), listOf(),
        isConnected = true,
        hasAuths = true, {},
        onChatScreen = { _, _->}, colorBackground = Color.Blue, colorForeground = Color.White
    )
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
        "version 1",
        msg
    )
}