package de.domjos.cloudapp.features.chats.screens


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.domjos.cloudapp.appbasics.R
import de.domjos.cloudapp.appbasics.helper.Separator
import de.domjos.cloudapp.webrtc.model.msg.Message
import de.domjos.cloudapp.webrtc.model.msg.ParameterArray


@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    lookIntoFuture: Int,
    token: String, colorBackground: Color, colorForeground: Color) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    viewModel.initChats(lookIntoFuture, token)

    val context = LocalContext.current

    viewModel.message.observe(LocalLifecycleOwner.current) {
        if(it != null) {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.message.value = null
        }
    }

    ChatScreen(messages, colorBackground, colorForeground, token, {viewModel.getDate(it, context)}) {
        viewModel.sendMessage(it)
    }
}


@Composable
fun ChatScreen(messages: List<Message>, colorBackground: Color, colorForeground: Color, token: String, onDate: (Long) -> String, onSend: (String) -> Unit) {
    var msg by remember { mutableStateOf(TextFieldValue("")) }

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (list, control) = createRefs()

        Row(Modifier.constrainAs(list) {
            top.linkTo(parent.top)
            start.linkTo(parent.start)
            end.linkTo(parent.end)
            bottom.linkTo(control.top)
            height = Dimension.fillToConstraints
            width = Dimension.fillToConstraints
        }) {
            LazyColumn(Modifier.fillMaxHeight()) {
                itemsIndexed(messages, {_, item -> item.id}) { _, message ->
                    ChatItem(token, colorBackground, colorForeground, message, onDate)
                }
            }
        }

        Row(
            Modifier
                .wrapContentHeight()
                .constrainAs(control) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    top.linkTo(list.bottom)
                    width = Dimension.fillToConstraints
                }
                .background(colorBackground)) {
            Column {
                Separator(color = colorForeground)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(2.dp)) {
                    Column(
                        Modifier
                            .weight(18f)) {
                        OutlinedTextField(
                            value = msg,
                            onValueChange = {msg = it},
                            label = {Text(stringResource(R.string.chats_msg),
                            fontSize = 12.sp,
                            color = colorForeground)}
                        )
                    }
                    Column(
                        Modifier
                            .weight(2f),
                        verticalArrangement = Arrangement.Center) {
                        IconButton(onClick = {
                            onSend(msg.text)
                            msg = TextFieldValue("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Send, stringResource(R.string.chats_send), tint = colorForeground)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(token: String, colorBackground: Color, colorForeground: Color, message: Message, onDate: (Long) -> String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(2.dp)) {
        if(message.actorType == "bots" || message.actorType == "system") {
            Column(modifier =
            Modifier
                .weight(3f)
                .clip(RoundedCornerShape(5.dp))
                .background(colorBackground)
                .padding(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                MsgItem(message, colorForeground, onDate)
            }
        } else {
            if(message.token==token) {
                Column(modifier =
                Modifier
                    .weight(2f)
                    .clip(RoundedCornerShape(5.dp))
                    .background(colorBackground)
                    .padding(2.dp),
                    horizontalAlignment = Alignment.Start) {
                    MsgItem(message, colorForeground, onDate)
                }
                Column(modifier = Modifier.weight(1f)) {}
            } else {
                Column(modifier = Modifier.weight(1f)) {}
                Column(modifier =
                Modifier
                    .weight(2f)
                    .clip(RoundedCornerShape(5.dp))
                    .background(colorBackground)
                    .padding(2.dp),
                    horizontalAlignment = Alignment.End) {
                    MsgItem(message, colorForeground, onDate)
                }
            }
        }
    }
}

@Composable
fun MsgItem(message: Message, colorForeground: Color, onDate: (Long) -> String) {
    Row {
        Text(if(message.actorDisplayName=="") "System" else message.actorDisplayName, fontWeight = FontWeight.Bold, color = colorForeground)
    }
    Row {
        Text(message.message, color = colorForeground)
    }
    Row {
        Text(
            onDate(message.timestamp),
            modifier = Modifier.padding(2.dp),
            fontSize = 10.sp,
            fontStyle = FontStyle.Italic,
            color = colorForeground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ChatScreen(listOf(fakeMessage(1), fakeMessage(2), fakeMessage(3, true)), Color.Blue, Color.White, "Test1", { "2023-03-19 11:24:36" }) {}
}

@Preview(showBackground = true)
@Composable
fun OwnChatItemPreview() {
    ChatItem("Test1", Color.Blue, Color.White, fakeMessage(1)) { "2023-03-19 11:24:36" }
}

@Preview(showBackground = true)
@Composable
fun ForeignChatItemPreview() {
    ChatItem("Test1", Color.Blue, Color.White, fakeMessage(2)) { "2023-03-19 11:24:36" }
}

@Preview(showBackground = true)
@Composable
fun BotChatItemPreview() {
    ChatItem("Test1", Color.Blue, Color.White, fakeMessage(2, true)) { "2023-03-19 11:24:36" }
}

fun fakeMessage(no: Int, isBot: Boolean = false): Message {
    return Message(no, "Test$no", if(isBot) "bots" else "person", " $no", "Test", 0L, "This is a test $no!")
}