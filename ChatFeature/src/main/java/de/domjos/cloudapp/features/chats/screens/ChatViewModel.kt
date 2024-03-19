package de.domjos.cloudapp.features.chats.screens

import android.content.Context
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.ChatRepository
import de.domjos.cloudapp.webrtc.model.msg.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import de.domjos.cloudapp.appbasics.R
import java.util.Locale

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
    ) : ViewModel() {
    private val _messages = MutableStateFlow(listOf<Message>())
    val messages: StateFlow<List<Message>> get() = _messages

    fun initChats(lookIntoFuture: Int, token: String) {
        chatRepository.lookIntoFuture = lookIntoFuture
        chatRepository.token = token

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                _messages.value = chatRepository.initChats()
                delay(20000L)
            }
        }
    }

    fun sendMessage(msg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _messages.value = chatRepository.postChat(msg)
        }
    }

    fun getDate(ts: Long, context: Context): String {
        val dt = Date(ts * 1000)
        val sdf = SimpleDateFormat(context.getString(R.string.sys_format), Locale.ENGLISH)
        return sdf.format(dt)
    }
}