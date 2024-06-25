package de.domjos.cloudapp2.rest.requests

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.rest.BasicTest
import de.domjos.cloudapp2.rest.model.shares.Types
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for the Autocomplete-Request
 * @see de.domjos.cloudapp2.rest.requests.AutocompleteRequest
 * @author Dominic Joas
 */
@RunWith(AndroidJUnit4::class)
class AutocompleteRequestTest : BasicTest() {
    private var request: AutocompleteRequest? = null

    /**
     * Open autocomplete-request
     */
    @Before
    fun before() {
        request = AutocompleteRequest(authentication)
    }

    /**
     * Test entering do and get domjos as user
     */
    @Test
    fun testAutocompleteUser() {
        // request shouldn't be null
        assertNotNull(request)

        // request user domjos
        runBlocking {
            val asyncItems = request?.getItem(Types.User, "do")
            assertNotNull(asyncItems)
            asyncItems!!.collect { items ->
                assertEquals(items.size, 1)
                assertEquals(items[0], "domjos")
            }
        }
    }

    /**
     * Test entering ad and get admin as group
     */
    @Test
    fun testAutocompleteGroup() {
        // request shouldn't be null
        assertNotNull(request)

        // request user domjos
        runBlocking {
            val asyncItems = request?.getItem(Types.Group, "ad")
            assertNotNull(asyncItems)
            asyncItems!!.collect { items ->
                assertEquals(items.size, 1)
                assertEquals(items[0], "admin")
            }
        }
    }
}