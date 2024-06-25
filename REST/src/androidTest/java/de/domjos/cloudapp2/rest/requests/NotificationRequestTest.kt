/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.requests

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.rest.BasicTest
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test getting notifications
 * @see de.domjos.cloudapp2.rest.requests.NotificationRequest
 */
@RunWith(AndroidJUnit4::class)
class NotificationRequestTest : BasicTest() {
    private var notificationRequest: NotificationRequest? = null

    /**
     * Initialize Request
     */
    @Before
    fun before() {
        notificationRequest = NotificationRequest(authentication)
    }

    /**
     * Test getting notifications
     */
    @Test
    fun testGettingNotifications() {
        runBlocking {

            // get notifications
            val asyncNotifications = notificationRequest?.getNotifications()
            assertNotNull(asyncNotifications)

            // notifications
            asyncNotifications?.collect { notifications ->
                assertNotNull(notifications)
            }
        }
    }
}