package de.domjos.cloudapp.features.chats.screens

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
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
import de.domjos.cloudapp.data.Settings
import java.util.Locale

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settings: Settings
    ) : ViewModel() {
    private val _messages = MutableStateFlow(listOf<Message>())
    val messages: StateFlow<List<Message>> get() = _messages
    val message = MutableLiveData<String?>()

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
                Log.e(this.javaClass.name, ex.message, ex)
                message.postValue(ex.message)
            }
        }
    }

    fun sendMessage(msg: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _messages.value = chatRepository.postChat(msg)
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
                message.postValue(ex.message)
            }
        }
    }

    fun getDate(ts: Long, context: Context): String {
        val dt = Date(ts * 1000)
        val sdf = SimpleDateFormat(context.getString(R.string.sys_format), Locale.ENGLISH)
        return sdf.format(dt)
    }
}