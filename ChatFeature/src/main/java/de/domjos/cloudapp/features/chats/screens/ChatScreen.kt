package de.domjos.cloudapp.features.chats.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import de.domjos.cloudapp.features.chats.R
import de.domjos.cloudapp.webrtc.model.msg.Message
import de.domjos.cloudapp.webrtc.model.room.Room

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val chats by produceState<RoomUiState>(
        initialValue = RoomUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }

    if (
        chats is RoomUiState.Success) {

        ChatScreen((chats as RoomUiState.Success).data)
    }
}

@Composable
fun ChatScreen(rooms: List<Room>) {
    Column(modifier = Modifier.fillMaxSize().padding(1.dp)) {

        rooms.forEach { room -> ChatItem(room)}
    }
}

@Composable
fun ChatItem(room: Room) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.primaryContainer)) {
        Image(painterResource(R.drawable.baseline_person_24), room.name, modifier = Modifier.padding(5.dp))
        Column {
            Text(room.displayName, fontWeight= FontWeight.Bold, modifier = Modifier.padding(5.dp))
            Text(
                "${room.lastMessage.actorDisplayName}: ${room.lastMessage.message}",
                modifier = Modifier.padding(5.dp),
                fontStyle = if(room.unreadMessages==0) FontStyle.Normal else FontStyle.Italic,
                fontWeight = if(room.unreadMessages==0) FontWeight.Normal else FontWeight.Bold
            )
        }
    }
    Row(modifier = Modifier.fillMaxWidth().height(1.dp)) {}
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen(listOf(fake(0), fake(1), fake(2)))
}

@Preview(showBackground = true)
@Composable
fun ChatItemPreview() {
    ChatItem(fake(1))
}

fun fake(no: Int): Room {
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
        no,
        "Chat$no",
        "Group-Chat$no",
        "description$no",
        1,
        no,
        "dfsklghkgfd",
        msg
    )
}