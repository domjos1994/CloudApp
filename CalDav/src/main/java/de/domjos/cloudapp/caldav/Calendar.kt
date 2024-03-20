package de.domjos.cloudapp.caldav

import com.thegrizzlylabs.sardineandroid.Sardine
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp.database.model.Authentication
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import okhttp3.Credentials
import okhttp3.Headers
import java.util.LinkedList

class Calendar(private val authentication: Authentication) {
    private val sardine: OkHttpSardine;
    private val calendars = LinkedHashMap<String, LinkedList<Calendar>>()

    init {
        this.sardine = OkHttpSardine()
        this.sardine.setCredentials(authentication.userName, authentication.password)
        val basePath = "${authentication.url}/remote.php/dav/calendars/${authentication.userName}"
        var first = true
        this.sardine.list(basePath).forEach { davResource ->
            if(first) {
                first = false
            } else {
                if(davResource.isDirectory) {
                    var firstSub = true
                    this.sardine.list("${authentication.url}${davResource.path}").forEach { event ->
                        try {
                            val tmp = "${davResource.path}-".replace("/-", "")
                            val name = tmp.split("/")[tmp.split("/").size-1]
                            if(firstSub) {
                                firstSub = false
                            } else {
                                val basic = Credentials.basic(this.authentication.userName, this.authentication.password)

                                val mp = LinkedHashMap<String, String>()
                                mp["Authorization"] = basic
                                val inputStream = this.sardine.get("${authentication.url}${event.path}", mp)
                                val builder = CalendarBuilder()
                                val calendar = builder.build(inputStream)

                                if(calendars.containsKey(name)) {
                                    calendars[name]?.add(calendar)
                                } else {
                                    val lst = LinkedList<Calendar>()
                                    lst.add(calendar)
                                    calendars[name] = lst
                                }
                            }
                        } catch (ex: Exception) {
                            println(ex.message)
                        }
                    }
                }
            }
        }
        println()
    }
}