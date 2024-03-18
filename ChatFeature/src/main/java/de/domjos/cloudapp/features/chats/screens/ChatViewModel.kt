package de.domjos.cloudapp.features.chats.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.ChatRepository
import de.domjos.cloudapp.webrtc.model.msg.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
    ) : ViewModel() {
    var uiState: StateFlow<ChatUiState> = chatRepository.initChats()
        .map<List<Message>, ChatUiState> {ChatUiState.Success(data = it)}
        .catch { emit(ChatUiState.Error(it)) }
        .stateIn(viewModelScope.plus(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), ChatUiState.Loading)

    fun initChats(lookIntoFuture: Int, token: String) {
        chatRepository.lookIntoFuture = lookIntoFuture
        chatRepository.token = token
    }
}


sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Error(val throwable: Throwable) : ChatUiState
    data class Success(val data: List<Message>) : ChatUiState
}