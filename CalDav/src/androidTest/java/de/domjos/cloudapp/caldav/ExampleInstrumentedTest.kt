package de.domjos.cloudapp.caldav

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp.database.model.Authentication

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    //@Test
    fun useAppContext() {
        val auth = Authentication(0, "Cloud", "https://cloud.dojodev.de", "domjos", "wRsesbg2M9D7CW3Uc68E", null, null)

        val calDav = CalDav(auth)
        calDav.getMonth(2)
    }
}