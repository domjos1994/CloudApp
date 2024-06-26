package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.rest.model.msg.Message
import de.domjos.cloudapp2.rest.requests.ChatRequest
import javax.inject.Inject

interface ChatRepository {
    val request: ChatRequest
    var lookIntoFuture: Int
    var token: String

    fun initChats(): List<Message>
    fun postChat(msg: String): List<Message>
    fun getUserName(): String
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

    override fun getUserName(): String {
        return this.authenticationDAO.getSelectedItem()?.userName!!
    }
}