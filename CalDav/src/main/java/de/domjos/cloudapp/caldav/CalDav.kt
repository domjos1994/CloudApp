package de.domjos.cloudapp.caldav

import at.bitfire.dav4jvm.BasicDigestAuthHandler
import at.bitfire.dav4jvm.DavCalendar
import de.domjos.cloudapp.database.model.Authentication
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.time.Instant
import java.time.LocalDate
import java.util.Calendar
import java.util.Date

class CalDav(authentication: Authentication) {
    private val davCalendar: DavCalendar

    init {
        val authHandler = BasicDigestAuthHandler(
            domain = authentication.url, // Optional, to only authenticate against hosts with this domain.
            username = authentication.title,
            password = authentication.password
        )
        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(false)
            .authenticator(authHandler)
            .addNetworkInterceptor(authHandler)
            .build()

        this.davCalendar = DavCalendar(okHttpClient, HttpUrl.Builder().host(authentication.url).build())
    }

    fun getMonth(month: Int, year: Int = LocalDate.now().year) {
        val cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        val max = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        return this.getRange(1, month, year, max, month, year)
    }

    private fun getRange(startDay: Int, startMonth: Int, startYear: Int, endDay: Int, endMonth: Int, endYear: Int) {
        val start = Date.from(Instant.parse("$startYear-$startMonth-$startDay"))
        val end = Date.from(Instant.parse("$endYear-$endMonth-$endDay"))

        this.davCalendar.calendarQuery("VEVENT", start = start, end = end) { a, b -> b.name}
    }
}