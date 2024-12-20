package de.domjos.cloudapp2.caldav

import android.util.Log
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.caldav.utils.Helper
import de.domjos.cloudapp2.caldav.utils.Helper.Companion.readPropertyToString
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.CalendarComponent
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.Categories
import net.fortuna.ical4j.model.property.Color
import net.fortuna.ical4j.model.property.Description
import net.fortuna.ical4j.model.property.DtEnd
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.LastModified
import net.fortuna.ical4j.model.property.Location
import net.fortuna.ical4j.model.property.Status
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.model.property.RRule
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.time.Duration
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class CalendarCalDav(private val authentication: Authentication?) {
    private var sardine: OkHttpSardine? = null
    private var basePath = ""

    init {
        if(this.authentication != null) {
            val client = OkHttpClient.Builder()
            client.callTimeout(Duration.ofMinutes(3))
            client.readTimeout(Duration.ofMinutes(3))
            client.writeTimeout(Duration.ofMinutes(5))
            client.connectTimeout(Duration.ofMinutes(3))
            this.sardine = OkHttpSardine(client.build())
            this.sardine?.setCredentials(authentication.userName, authentication.password)
            basePath = "${authentication.url}/remote.php/dav/calendars/${authentication.userName}"
        }
    }

    fun getCalendars(): List<CalendarModel> {
        val lst = mutableListOf<CalendarModel>()
        if(this.sardine != null) {
            var first = true
            this.sardine?.list(this.basePath)?.forEach { davResource ->
                if (first) {
                    first = false
                } else {
                    if (davResource.isDirectory) {
                        val path = "${authentication?.url}${davResource.path}"
                        val tmp = "${davResource.path}-".replace("/-", "")
                        val name = tmp.split("/")[tmp.split("/").size-1]
                        val label = if(davResource.displayName != null) davResource.displayName else name
                        if(davResource.displayName != null) {
                            lst.add(CalendarModel(name, label, path))
                        }
                    }
                }
            }
        }
        return lst
    }

    fun insertCalendar(calendarModel: CalendarModel): String {
        val url = "${this.basePath}/${calendarModel.name}"
        val okHttp = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", Credentials.basic(authentication!!.userName, authentication.password))
            .method("MKCALENDAR", null)
            .build()
        val response = okHttp.newCall(request).execute()
        if(response.code >= 400) {
            throw Exception(response.message)
        }
        calendarModel.path = url
        updateCalendar(calendarModel)
        return url
    }

    fun updateCalendar(calendarModel: CalendarModel) {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val document = builder.newDocument()


        val propertyUpdateElement = document.createElementNS("DAV:", "propertyupdate")
        propertyUpdateElement.prefix = "d"
        document.appendChild(propertyUpdateElement)

        val setElement = document.createElement("d:set")
        propertyUpdateElement.appendChild(setElement)

        val propElement = document.createElement("d:prop")
        setElement.appendChild(propElement)

        val displayNameElement = document.createElement("d:displayname")
        displayNameElement.textContent = calendarModel.label
        propElement.appendChild(displayNameElement)

        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, "yes")
        val writer = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(writer))
        val output = writer.buffer.toString()

        val okHttp = OkHttpClient()
        val request = Request.Builder()
            .url(calendarModel.path)
            .addHeader("Authorization", Credentials.basic(authentication!!.userName, authentication.password))
            .method("PROPPATCH", output.toRequestBody())
            .build()
        val response = okHttp.newCall(request).execute()
        if(response.code >= 400) {
            throw Exception(response.message)
        }
    }

    fun deleteCalendar(calendarModel: CalendarModel) {
        this.sardine!!.delete(calendarModel.path)
    }

    fun loadCalendarEvents(calendarModel: CalendarModel, updateProgress: (Float, String) -> Unit, progressLabel: String): List<CalendarEvent> {
        val items = mutableListOf<CalendarEvent>()
        if(this.sardine != null) {
            val lst = this.sardine?.list(calendarModel.path)

            val name = calendarModel.name
            val percentage = 100.0f / lst?.size!!
            var item = 0
            lst.drop(1).forEach { element ->
                try {
                    val inputStream =
                        this.sardine?.get(
                            "${authentication?.url}${element.path}", Helper.buildHeaders(this.authentication!!)
                        )

                    val builder = CalendarBuilder()
                    val calendar = builder.build(inputStream)
                    items.addAll(this.iCalToModel(calendar, name, element.path))
                } catch (ex: Throwable) {
                    Log.e("Error", ex.message, ex)
                } finally {
                    item += 1
                    updateProgress((percentage*item)/100.0f, String.format(progressLabel, calendarModel.label))
                }
            }
        }
        return items
    }

    fun reloadCalendarEvents(updateProgress: (Float, String) -> Unit, progressLabel: String): Map<String, List<CalendarEvent>> {
        val mp = mutableMapOf<String, List<CalendarEvent>>()
        this.getCalendars().forEach { calendar ->
            mp[calendar.name] = this.loadCalendarEvents(calendar, updateProgress, progressLabel)
        }
        return mp
    }

    fun updateCalendarEvent(calendarEvent: CalendarEvent) {
        if(this.sardine != null) {
            val calendar = this.modelToICal(calendarEvent)
            if(calendar != null) {
                this.sardine?.put("${authentication?.url}${calendarEvent.path}", Helper.getData(calendar))
            }
        }
    }

    fun newCalendarEvent(calendarModel: CalendarModel, calendarEvent: CalendarEvent): String {
        if(this.sardine != null) {
            val uid = UUID.randomUUID().toString()
            calendarEvent.uid = uid
            val calendar = this.modelToICal(calendarEvent)
            if(calendar != null) {
                this.sardine?.put("${basePath}/${calendarModel.name}/$uid.ics", Helper.getData(calendar))
            }
            return uid
        }
        return ""
    }

    fun deleteCalendarEvent(calendarEvent: CalendarEvent) {
        if(this.sardine != null) {
            this.sardine?.delete("${authentication?.url}/${calendarEvent.path}")
        }
    }

    fun fileToModels(data: ByteArray, cm: CalendarModel): List<CalendarEvent> {
        val baStream = ByteArrayInputStream(data)
        var calendar: Calendar?
        baStream.use { stream ->
            val builder = CalendarBuilder()
            calendar = builder.build(stream)
        }
        if(calendar != null) {
            return iCalToModel(calendar!!, cm.name, "")
        }
        return listOf()
    }

    private fun iCalToModel(calendar: Calendar, name: String, path: String) : List<CalendarEvent> {
        val items = mutableListOf<CalendarEvent>()
        try {
            val components = calendar.components
            components.forEach { component ->
                try {
                    if(component is VEvent) {
                        val uid = readPropertyToString<Uid>(component)
                        val title = readPropertyToString<Summary>(component)
                        val description = readPropertyToString<Description>(component)
                        val location = readPropertyToString<Location>(component)
                        val categories = readPropertyToString<Categories>(component)
                        val confirmation = readPropertyToString<Status>(component)
                        val color = readPropertyToString<Color>(component)

                        var lastModified: Long = 0
                        try {
                            lastModified = component.getProperty<LastModified>("LAST-MODIFIED").dateTime.time
                        } catch (_: Exception) {}

                        val from: String = readPropertyToString<DtStart>(component)
                        val to: String = readPropertyToString<DtEnd>(component)

                        items.add(
                            CalendarEvent(
                                0L, uid, from, to, title, location,
                                description, confirmation, categories, color,
                                name, "", -1L, lastModified,
                                authentication?.id!!, path, buildRecurrence(component)
                            )
                        )
                    }
                } catch (ex:Exception) {
                    Log.e("Error", ex.message, ex)
                }
            }
        } catch (ex: Exception) {
            Log.e("error importing", ex.message, ex)
        }
        return items
    }

    private fun buildRecurrence(component: CalendarComponent): String {
        try {
            val itemList = mutableListOf<Int>()
            val frequency = component.getProperty<RRule>(RRule::class.simpleName?.uppercase())
            val type = frequency.recur.frequency.name.lowercase()
            var freq: Frequency = Frequency.None
            when(type) {
                "yearly" -> {
                    freq = Frequency.Yearly
                    frequency.recur.monthList.forEach {m -> itemList.add(m.monthOfYear)}
                }
                "monthly" -> {
                    freq = Frequency.Monthly
                    frequency.recur.dayList.forEach {d -> itemList.add(d.day.ordinal)}
                }
                "weekly" -> {
                    freq = Frequency.Weekly
                    frequency.recur.weekNoList.forEach {w -> itemList.add(w)}
                }
                "daily" -> {freq = Frequency.Daily}
                else -> Frequency.None
            }
            val interval = frequency.recur.interval
            val repeats = frequency.recur.count
            val untilDate = frequency.recur.until?.time ?: 0L

            return "${freq.name}(${itemList.joinToString(",")}), $interval, $repeats, $untilDate"
        } catch (_: Exception) {}
        return ""
    }

    private fun modelToICal(event: CalendarEvent): Calendar? {
        try {
            val vEvent =
                VEvent(
                    dt(event.string_from),
                    dt(event.string_to),
                    event.title
                )


            vEvent.properties.add(Description(event.description))
            vEvent.properties.add(Location(event.location))
            vEvent.properties.add(Status(event.confirmation))
            vEvent.properties.add(Uid(event.uid))
            val cal = Calendar()
            cal.components.add(vEvent)
            return cal
        } catch (ex: Exception) {
            Log.e("Error", ex.message, ex)
        }
        return null
    }

    private fun dt(str: String): net.fortuna.ical4j.model.Date {
        return try {
            net.fortuna.ical4j.model.DateTime(str)
        } catch (_: Exception) {
            net.fortuna.ical4j.model.Date(str)
        }
    }
}

private enum class Frequency {
    Yearly,
    Monthly,
    Weekly,
    Daily,
    None
}