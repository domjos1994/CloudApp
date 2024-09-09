package de.domjos.cloudapp2.features.chats.screens

import android.content.Context
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.data.repository.ChatRepository
import de.domjos.cloudapp2.rest.model.msg.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.appbasics.helper.LogViewModel
import de.domjos.cloudapp2.data.Settings
import java.util.Locale

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settings: Settings
    ) : LogViewModel() {
    private val _messages = MutableStateFlow(listOf<Message>())
    val messages: StateFlow<List<Message>> get() = _messages

    fun initChats(lookIntoFuture: Int, token: String) {
        chatRepository.lookIntoFuture = lookIntoFuture
        chatRepository.token = token

        viewModelScope.launch(Dispatchers.IO) {
            try {
                while (true) {
                    _messages.value = chatRepository.initChats()
                    val timeSpan = (settings.getTimeSpanSetting() * 1000L).toLong()
                    delay(timeSpan)
                }
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun sendMessage(msg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _messages.value = chatRepository.postChat(msg)
            } catch (ex: Exception) {
                printException(ex, this)
            }
        }
    }

    fun getDate(ts: Long, context: Context): String {
        val dt = Date(ts * 1000)
        val sdf = SimpleDateFormat(context.getString(R.string.sys_format), Locale.ENGLISH)
        return sdf.format(dt)
    }

    fun getUserName(): String {
        return chatRepository.getUserName()
    }
}