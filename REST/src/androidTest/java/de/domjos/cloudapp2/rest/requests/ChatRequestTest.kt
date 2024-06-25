/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.requests

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.BasicTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Test chatting with users
 */
@RunWith(AndroidJUnit4::class)
class ChatRequestTest : BasicTest() {

    @Test
    fun testChatting() {

        // initialize requests
        val roomRequest = RoomRequest(authentication)
        val adminRequest = ChatRequest(authentication)
        val domjosRequest = ChatRequest(
            Authentication(0L, "N28D", props!!["url"].toString(),
                props!!["user2"].toString(), props!!["pwd2"].toString(),
                true, "", null
            )
        )

        runBlocking {

            // get token of room
            var token = ""
            val asyncRooms = roomRequest.getRooms()
            asyncRooms.collect { rooms ->
                rooms.forEach { room ->
                    if(room.name == "domjos") {
                        token = room.token
                    }
                }
            }
            assertNotEquals("", token)

            // create message and send to domjos
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val chat = "Hello, this is a test! ${sdf.format(Date())}"
            adminRequest.insertChats(token, chat)

            // compare with chats from domjos
            var chats = domjosRequest.getChats(token = token)
            var lastMessage = chats.get(0).message
            assertEquals(chat, lastMessage)

            // create message and send to admin
            val chatR = "Hello! ${sdf.format(Date())}"
            domjosRequest.insertChats(token, chatR)

            // compare with chats from admin
            chats = adminRequest.getChats(token = token)
            lastMessage = chats.get(0).message
            assertEquals(chatR, lastMessage)
        }

    }
}