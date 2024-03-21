package de.domjos.cloudapp.caldav

import de.domjos.cloudapp.database.model.Authentication
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val cal = Calendar(Authentication(0L, "Cloud", "https://cloud.dojodev.de", "domjos", "wRsesbg2M9D7CW3Uc68E", true, "", null))
        cal.reloadCalendarEvents()
    }
}