package de.domjos.cloudapp.features.chats.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavArgument
import androidx.navigation.compose.rememberNavController
import de.domjos.cloudapp.webrtc.model.msg.Message


@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    lookIntoFuture: Int,
    token: String) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val chats by produceState<ChatUiState>(
        initialValue = ChatUiState.Loading,
        key1 = lifecycle,
        key2 = viewModel
    ) {
        lifecycle.repeatOnLifecycle(state = Lifecycle.State.STARTED) {
            viewModel.uiState.collect { value = it }
        }
    }
    viewModel.initChats(lookIntoFuture, token)

    if (
        chats is ChatUiState.Success) {

        ChatScreen(
            onSaveClick = {},
            onDeleteClick = {},
            messages = (chats as ChatUiState.Success).data)

    }
}

@Composable
fun ChatScreen(
    onSaveClick: (Message) -> Unit,
    onDeleteClick: (Message) -> Unit,
    messages: List<Message>) {

    Row(Modifier.background(color = Color.Black)) {
        Text("Test")
    }
}

@Preview
@Composable
fun ScreenPreview() {
    ChatScreen({},{}, listOf())
}