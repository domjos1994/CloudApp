/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.requests

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.rest.BasicTest
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRequestTest : BasicTest() {
    private var userRequest: UserRequest? = null

    @Before
    fun before() {
        userRequest = UserRequest(authentication)
    }

    @Test
    fun testCheckConnection() {
        runBlocking {
            // check user
            var user = userRequest?.checkConnection()
            assertNotNull(user)

            // check inexistent user
            user = userRequest?.checkConnection("georg")
            assertNull(user)
        }
    }

    @Test
    fun testGetUsers() {
        runBlocking {
            // get users
            val users = userRequest?.getUsers()
            assertNotEquals(0, users?.size)
        }
    }

    @Test
    fun getCapabilities() {
        runBlocking {
            // get capabilities
            val capabilities = userRequest?.getCapabilities()
            assertNotNull(capabilities)

            assertNotEquals("", capabilities?.capabilities?.theming?.name)
        }
    }
}