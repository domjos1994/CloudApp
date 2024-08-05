package de.domjos.cloudapp2.caldav

import android.util.Log
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp2.caldav.model.CalendarModel
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.calendar.CalendarEvent
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.Component
import net.fortuna.ical4j.model.Property
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
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.time.Duration
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class CalDav(private val authentication: Authentication?) {
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
                            "${authentication?.url}${element.path}", this.buildHeaders()
                        )

                    val builder = CalendarBuilder()
                    val calendar = builder.build(inputStream)
                    items.addAll(this.iCalToModel(calendar, name, element.path))
                } catch (ex: Throwable) {
                    Log.e("Error", ex.message, ex)
                } finally {
                    item += 1
                    updateProgress((percentage*item)/100.0f, String.format(progressLabel, name))
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
                this.sardine?.put("${basePath}/${calendarEvent.calendar}/${calendarEvent.uid}.ics", this.getData(calendar))
            }
        }
    }

    fun newCalendarEvent(calendarModel: CalendarModel, calendarEvent: CalendarEvent) {
        if(this.sardine != null) {
            val uid = UUID.randomUUID().toString()
            calendarEvent.uid = uid
            val calendar = this.modelToICal(calendarEvent)
            if(calendar != null) {
                this.sardine?.put("${basePath}/${calendarModel.name}/$uid.ics", this.getData(calendar))
            }
        }
    }

    fun deleteCalendarEvent(calendarEvent: CalendarEvent) {
        if(this.sardine != null) {
            this.sardine?.delete("${authentication?.url}/${calendarEvent.path}")
        }
    }

    private fun getData(calendar: Calendar): ByteArray {
        val fOut = ByteArrayOutputStream()
        val output = CalendarOutputter()
        output.isValidating = false
        output.output(calendar, fOut)
        val result = fOut.toByteArray()
        fOut.close()
        return result
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

                        var from: Long
                        var to: Long
                        try {
                            from = net.fortuna.ical4j.model.Date(readPropertyToString<DtStart>(component)).time
                            to = net.fortuna.ical4j.model.Date(readPropertyToString<DtEnd>(component)).time
                        } catch (_: Exception) {
                            from = net.fortuna.ical4j.model.DateTime(readPropertyToString<DtStart>(component)).time
                            to = net.fortuna.ical4j.model.DateTime(readPropertyToString<DtEnd>(component)).time
                        }
                        items.add(CalendarEvent(0L, uid, from, to, title, location,
                            description, confirmation, categories, color,
                            name, "", -1L, lastModified,
                            authentication?.id!!, path))
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

    private fun modelToICal(event: CalendarEvent): Calendar? {
        try {
            val vEvent =
                VEvent(
                    net.fortuna.ical4j.model.DateTime(event.from),
                    net.fortuna.ical4j.model.DateTime(event.to),
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

    private inline fun <reified T: Property> readPropertyToString(component: Component): String {
        try {
            val name = T::class.simpleName?.uppercase()
            return (component.getProperty<T>(name).value.toString())
        } catch (_: Exception) {}
        return ""
    }

    private fun buildHeaders(): Map<String, String> {
        val headers = LinkedHashMap<String, String>()
        val auth = Credentials.basic(this.authentication?.userName!!, this.authentication.password)
        headers["Authorization"] = auth
        return headers
    }
}