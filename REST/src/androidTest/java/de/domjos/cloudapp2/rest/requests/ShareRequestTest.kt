/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest.requests

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.BasicTest
import de.domjos.cloudapp2.rest.model.shares.InsertShare
import de.domjos.cloudapp2.rest.model.shares.Permissions
import de.domjos.cloudapp2.rest.model.shares.Types
import de.domjos.cloudapp2.rest.model.shares.UpdateShare
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Testing sharing data
 * @see de.domjos.cloudapp2.rest.requests.ShareRequest
 */
@RunWith(AndroidJUnit4::class)
class ShareRequestTest : BasicTest() {
    private var adminRequest: ShareRequest? = null
    private var domjosRequest: ShareRequest? = null
    private val sharePath = "/TestForShare"
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var expDate = ""

    /**
     * Initialize requests
     * and date
     */
    @Before
    fun before() {
        adminRequest = ShareRequest(authentication)
        domjosRequest = ShareRequest(
            Authentication(0L, "N28D", props!!["url"].toString(),
                props!!["user2"].toString(), props!!["pwd2"].toString(),
                true, "", null
            )
        )
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 2)
        expDate = sdf.format(cal.time)
    }

    /**
     * Test getting, adding and deleting shares for the user domjos
     */
    @Test
    fun testAddAndDeleteShareToUser() {
        runBlocking {

            // insert share
            val insertShare = InsertShare(
                "admin$sharePath",
                Types.User.value,
                "domjos",
                "false",
                "",
                Permissions.Read.value,
                expDate,
                "Test"
            )
            val result = adminRequest?.addShare(insertShare)
            result?.collect {res -> assertNotNull(res) }

            // check own share exists
            var asyncShares = adminRequest?.getShares(false)
            assertNotNull(asyncShares)
            var id = 0L
            asyncShares?.collect { shares ->
                assertNotNull(shares)
                val filtered = shares.filter { it.path == sharePath }
                assertEquals(1, filtered.size)
                id = filtered[0].id
            }

            // check folder was shared
            asyncShares = domjosRequest?.getShares(true)
            assertNotNull(asyncShares)
            asyncShares?.collect { shares ->
                assertNotNull(shares)
                assertNotEquals(0, shares.filter { it.path == sharePath }.size)
            }

            // delete
            adminRequest?.deleteShare(id.toInt())?.collect { msg -> assertEquals("", msg) }

            // check folder share not exists
            checkList(adminRequest!!, false)
            checkList(domjosRequest!!, true)
        }
    }

    /**
     * Test getting, adding and deleting shares for the group admin
     */
    @Test
    fun testAddAndDeleteShareToGroup() {
        runBlocking {

            // insert share
            val insertShare = InsertShare(
                "admin$sharePath",
                Types.Group.value,
                "admin",
                "false",
                "",
                Permissions.Read.value,
                expDate,
                "Test"
            )
            val result = adminRequest?.addShare(insertShare)
            result?.collect {res -> assertNotNull(res) }

            // check own share exists
            val asyncShares = adminRequest?.getShares(false)
            assertNotNull(asyncShares)
            var id = 0L
            asyncShares?.collect { shares ->
                assertNotNull(shares)
                val filtered = shares.filter { it.path == sharePath }
                assertEquals(1, filtered.size)
                id = filtered[0].id
            }

            // delete
            adminRequest?.deleteShare(id.toInt())?.collect { msg -> assertEquals("", msg) }

            // check folder share not exists
            checkList(adminRequest!!, false)
            checkList(domjosRequest!!, true)
        }
    }

    /**
     * Test updating shares for the user domjos
     */
    @Test
    fun testUpdateAndDeleteShareToUser() {
        runBlocking {

            // insert share
            val insertShare = InsertShare(
                "admin$sharePath",
                Types.User.value,
                "domjos",
                "false",
                "",
                Permissions.Read.value,
                expDate,
                "Test"
            )
            val result = adminRequest?.addShare(insertShare)
            result?.collect {res -> assertNotNull(res) }

            // check own share exists
            var asyncShares = adminRequest?.getShares(false)
            assertNotNull(asyncShares)
            var id = 0L
            asyncShares?.collect { shares ->
                assertNotNull(shares)
                val filtered = shares.filter { it.path == sharePath }
                assertEquals(1, filtered.size)
                id = filtered[0].id
            }

            // check folder was shared
            asyncShares = domjosRequest?.getShares(true)
            assertNotNull(asyncShares)
            asyncShares?.collect { shares ->
                assertNotNull(shares)
                assertNotEquals(0, shares.filter { it.path == sharePath }.size)
            }

            // update share
            val item = UpdateShare(Permissions.Read.value, "",  "false", "", "Test2")
            adminRequest?.updateShare(id.toInt(), item)?.collect { sh -> assertNotNull(sh) }

            // delete
            adminRequest?.deleteShare(id.toInt())?.collect { msg -> assertEquals("", msg) }

            // check folder share not exists
            checkList(adminRequest!!, false)
            checkList(domjosRequest!!, true)
        }
    }

    /**
     * Test insert public link with url
     */
    @Test
    fun testAddAndDeleteShareWithPublic() {
        runBlocking {

            // insert share
            val insertShare = InsertShare(
                "admin$sharePath",
                Types.Public.value,
                "",
                "false",
                "",
                Permissions.Read.value,
                expDate,
                "Test"
            )
            val result = adminRequest?.addShare(insertShare)
            result?.collect {res ->
                assertNotNull(res)
                assertNotNull(res?.url)
                assertNotEquals("", res?.url)
            }

            // check own share exists
            val asyncShares = adminRequest?.getShares(false)
            assertNotNull(asyncShares)
            var id = 0L
            asyncShares?.collect { shares ->
                assertNotNull(shares)
                val filtered = shares.filter { it.path == sharePath }
                assertEquals(1, filtered.size)
                id = filtered[0].id
            }

            // delete
            adminRequest?.deleteShare(id.toInt())?.collect { msg -> assertEquals("", msg) }

            // check folder share not exists
            checkList(adminRequest!!, false)
        }
    }

    /**
     * Check the list for the inserted share
     * @param request the share-request
     * @param sharedWithMe if own share
     */
    private suspend fun checkList(request: ShareRequest, sharedWithMe: Boolean) {
        val asyncShares = request.getShares(sharedWithMe)
        assertNotNull(asyncShares)
        asyncShares.collect { shares ->
            assertNotNull(shares)
            assertEquals(0, shares.filter { it.path == sharePath }.size)
        }
    }
}