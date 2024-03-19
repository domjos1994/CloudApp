package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.webrtc.model.msg.Message
import de.domjos.cloudapp.webrtc.requests.ChatRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ChatRepository {
    val request: ChatRequest
    var lookIntoFuture: Int
    var token: String

    fun initChats(): List<Message>
    fun postChat(msg: String): List<Message>
}

class DefaultChatRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : ChatRepository {
    override val request: ChatRequest
        get() = ChatRequest(authenticationDAO.getSelectedItem())
    override var lookIntoFuture: Int = 0
    override var token: String = ""

    override fun initChats(): List<Message> {
        return request.getChats(lookIntoFuture, token)
    }

    override fun postChat(msg: String): List<Message> {
        return request.insertChats(token, msg)
    }
}