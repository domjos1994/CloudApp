package de.domjos.cloudapp2.rest

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.requests.RoomRequest

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Rule
    @JvmField
    var runtimePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.INTERNET)

    @Test
    fun testRoomRequest() {
        val authentication = Authentication(0, "OCS", "https://cloud.cz-dillingen.de", "domjos", "ePhgHOQOXwAp3tRNbwuP", false, null, null)
        val request = RoomRequest(authentication)
        val rooms = request.getRooms()
        assertNotNull(rooms)
    }
}