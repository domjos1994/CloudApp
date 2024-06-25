package de.domjos.cloudapp2.rest.requests

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.rest.BasicTest
import de.domjos.cloudapp2.rest.model.room.RoomInput
import kotlinx.coroutines.runBlocking

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Tests for the Getting, Inserting and updating rooms
 * @see de.domjos.cloudapp2.rest.requests.RoomRequest
 * @see de.domjos.cloudapp2.rest.requests.AvatarRequest
 * @author Dominic Joas
 */
@RunWith(AndroidJUnit4::class)
class RoomRequestTest : BasicTest() {
    private var roomRequest: RoomRequest? = null
    private var avatarRequest: AvatarRequest? = null

    /**
     * Initialize Requests
     */
    @Before
    fun before() {
        roomRequest = RoomRequest(authentication)
        avatarRequest = AvatarRequest(authentication)
    }

    /**
     * Test Getting the Rooms
     * @see de.domjos.cloudapp2.rest.model.room.Room
     */
    @Test
    fun testGettingRooms() {
        runBlocking {

            // room-list shouldn't be null
            val asyncRooms = roomRequest?.getRooms()
            assertNotNull(asyncRooms)

            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // search for domjos
                var hasDomjos = false
                rooms.forEach { room ->
                    if(room.name == "domjos") {
                        hasDomjos = true
                    }
                }
                assertTrue(hasDomjos)
            }
        }
    }

    /**
     * Test Getting Avatars
     */
    @Test
    fun testGettingAvatar() {
        runBlocking {

            // room-list shouldn't be null
            val asyncRooms = roomRequest?.getRooms()
            assertNotNull(asyncRooms)

            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // compare bitmap
                rooms.forEach { room ->
                    val icon = avatarRequest?.getAvatar(room.token)
                    assertEquals(icon?.width, room.icon?.width)
                }
            }
        }
    }

    /**
     * Test insertion and deletion
     */
    @Test
    fun testInsertingDeletingRoom() {
        runBlocking {

            // insert new room
            val inputRoom = RoomInput(2, "admin", "", "Test")
            roomRequest?.addRoom(inputRoom)

            // room-list shouldn't be null
            var asyncRooms = roomRequest?.getRooms()
            assertNotNull(asyncRooms)

            var token = ""
            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // compare item has token
                var hasRoom = false
                rooms.forEach { room ->
                    if(inputRoom.invite == room.name) {
                        hasRoom = true
                        token = room.token
                    }
                }
                assertTrue(hasRoom)
            }

            // delete room
            roomRequest?.deleteRoom(token)

            // getting a new list
            asyncRooms = roomRequest?.getRooms()

            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // list doesn't contain tokens
                var hasRoom = false
                rooms.forEach { room ->
                    if(inputRoom.roomName == room.name) {
                        hasRoom = true
                        token = room.token
                    }
                }
                assertFalse(hasRoom)
            }
        }
    }

    /**
     * Test updating
     */
    @Test
    fun testUpdateRoom() {
        runBlocking {

            // insert new room
            val inputRoom = RoomInput(2, "admin", "", "Test")
            roomRequest?.addRoom(inputRoom)

            // room-list shouldn't be null
            var asyncRooms = roomRequest?.getRooms()
            assertNotNull(asyncRooms)

            var token = ""
            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // compare item has token
                var hasRoom = false
                rooms.forEach { room ->
                    if(inputRoom.invite == room.name) {
                        hasRoom = true
                        token = room.token
                    }
                }
                assertTrue(hasRoom)
            }

            // change description
            roomRequest?.updateDescription(token, "This is a Test!")

            // getting a new list
            asyncRooms = roomRequest?.getRooms()
            assertNotNull(asyncRooms)

            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // item has new description
                var hasRoom = false
                rooms.forEach { room ->
                    if(room.description == "This is a Test!") {
                        hasRoom = true
                    }
                }
                assertTrue(hasRoom)
            }

            // change name
            roomRequest?.renameRoom(token, "Test!")

            // getting a new list
            asyncRooms = roomRequest?.getRooms()
            assertNotNull(asyncRooms)

            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // item has new name
                var hasRoom = false
                rooms.forEach { room ->
                    if(room.displayName == "Test!") {
                        hasRoom = true
                    }
                }
                assertTrue(hasRoom)
            }

            // delete room
            roomRequest?.deleteRoom(token)

            // getting a new list
            asyncRooms = roomRequest?.getRooms()

            asyncRooms?.collect { rooms ->
                assertNotEquals(0, rooms.size)

                // list doesn't contain tokens
                var hasRoom = false
                rooms.forEach { room ->
                    if(inputRoom.roomName == room.name) {
                        hasRoom = true
                        token = room.token
                    }
                }
                assertFalse(hasRoom)
            }
        }
    }
}