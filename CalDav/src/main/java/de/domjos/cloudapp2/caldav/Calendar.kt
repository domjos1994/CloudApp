package de.domjos.cloudapp2.caldav

import android.util.Log
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
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
import java.io.ByteArrayOutputStream
import java.util.LinkedList


class Calendar(private val authentication: Authentication?) {
    private var sardine: OkHttpSardine? = null
    val calendars = LinkedHashMap<String, LinkedList<CalendarEvent>>()
    private var basePath = ""

    init {
        if(this.authentication != null) {
            this.sardine = OkHttpSardine()
            this.sardine?.setCredentials(authentication.userName, authentication.password)
            basePath = "${authentication.url}/remote.php/dav/calendars/${authentication.userName}"
        }
    }

    fun reloadCalendarEvents(updateProgress: (Float, String) -> Unit, progressLabel: String) {
        this.calendars.clear()

        if(this.sardine != null) {
            var first = true
            this.sardine?.list(this.basePath)?.forEach { davResource ->
                if(first) {
                    first = false
                } else {
                    if(davResource.isDirectory) {
                        var firstSub = true
                        val lst = this.sardine?.list("${authentication?.url}${davResource.path}")
                        val tmp = "${davResource.path}-".replace("/-", "")
                        val name = tmp.split("/")[tmp.split("/").size-1]
                        val percentage = 100.0f / lst?.size!!
                        var item = 0
                        lst.forEach { event ->
                            try {
                                if(firstSub) {
                                    firstSub = false
                                } else {
                                    val inputStream = this.sardine?.get("${authentication?.url}${event.path}", this.buildHeaders())
                                    val builder = CalendarBuilder()
                                    val calendar = builder.build(inputStream)

                                    if(calendars.containsKey(name)) {
                                        val model = this.iCalToModel(calendar, name)
                                        if(model != null) {
                                            calendars[name]?.add(model)
                                        }
                                    } else {
                                        val list = LinkedList<CalendarEvent>()
                                        val model = this.iCalToModel(calendar, name)
                                        if(model != null) {
                                            list.add(model)
                                        }
                                        calendars[name] = list
                                    }
                                }
                            } catch (ex: Throwable) {
                                println(ex.message)
                            }
                            item += 1
                            updateProgress((percentage*item)/100.0f, String.format(progressLabel, name))
                        }
                    }
                }
            }
        }
    }

    fun updateCalendarEvent(calendarEvent: CalendarEvent) {
        if(this.sardine != null) {
            var first = true
            this.sardine?.list(this.basePath)?.forEach { davResource ->
                if(first) {
                    first = false
                } else {
                    if(davResource.isDirectory) {
                        var firstSub = true
                        this.sardine?.list("${authentication?.url}${davResource.path}")?.forEach { event ->
                            try {
                                if(firstSub) {
                                    firstSub = false
                                } else {
                                    val inputStream = this.sardine?.get("${authentication?.url}${event.path}", this.buildHeaders())
                                    val builder = CalendarBuilder()
                                    var calendar = builder.build(inputStream)
                                    val uid = calendar.getComponents<VEvent>(VEvent::class.simpleName?.uppercase())[0].uid
                                    if(uid.value == calendarEvent.uid) {
                                        calendar = this.modelToICal(calendarEvent)
                                        if(calendar != null) {
                                            this.sardine?.put("${authentication?.url}${event.path}", this.getData(calendar))
                                        }
                                        return
                                    }

                                }
                            } catch (ex: Exception) {
                                println(ex.message)
                            }
                        }
                    }
                }
            }
        }
    }

    fun newCalendarEvent(calendarEvent: CalendarEvent) {
        if(this.sardine != null) {
            var first = true
            this.sardine?.list(this.basePath)?.forEach { davResource ->
                if(first) {
                    first = false
                } else {
                    if(davResource.isDirectory) {
                        val tmp = "${davResource.path}-".replace("/-", "")
                        val name = tmp.split("/")[tmp.split("/").size-1]
                        if(name==calendarEvent.calendar) {
                            val calendar = this.modelToICal(calendarEvent)
                            if(calendar != null) {
                                this.sardine?.put("${authentication?.url}${davResource.path}/${calendarEvent.uid}.ics", this.getData(calendar))
                            }
                            return
                        }
                    }
                }
            }
        }
    }

    fun deleteCalendarEvent(calendarEvent: CalendarEvent) {
        if(this.sardine != null) {
            var first = true
            this.sardine?.list(this.basePath)?.forEach { davResource ->
                if(first) {
                    first = false
                } else {
                    if(davResource.isDirectory) {
                        var firstSub = true
                        this.sardine?.list("${authentication?.url}${davResource.path}")?.forEach { event ->
                            try {
                                if(firstSub) {
                                    firstSub = false
                                } else {
                                    val inputStream = this.sardine?.get("${authentication?.url}${event.path}", this.buildHeaders())
                                    val builder = CalendarBuilder()
                                    val calendar = builder.build(inputStream)
                                    val uid = calendar.getComponents<VEvent>(VEvent::class.simpleName?.uppercase())[0].uid
                                    if(uid.value == calendarEvent.uid) {
                                        this.sardine?.delete("${authentication?.url}${event.path}")
                                        return
                                    }

                                }
                            } catch (ex: Exception) {
                                println(ex.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getData(calendar: Calendar): ByteArray {
        val fOut = ByteArrayOutputStream()
        val output = CalendarOutputter()
        output.output(calendar, fOut)
        val result = fOut.toByteArray()
        fOut.close()
        return result
    }

    private fun iCalToModel(calendar: Calendar, name: String) : CalendarEvent? {
        try {
            var uid = ""
            var title = ""
            var location = ""
            var description = ""
            var from = 0L
            var to = 0L
            var confirmation = ""
            var categories = ""
            var color = ""
            var lastModified = 0L

            val components = calendar.components
            components.forEach { component ->
                if(component is VEvent) {
                    uid = readPropertyToString<Uid>(component)
                    title = readPropertyToString<Summary>(component)
                    description = readPropertyToString<Description>(component)
                    location = readPropertyToString<Location>(component)
                    categories = readPropertyToString<Categories>(component)
                    confirmation = readPropertyToString<Status>(component)
                    color = readPropertyToString<Color>(component)
                    lastModified = component.getProperty<LastModified>("LAST-MODIFIED").dateTime.time

                    try {
                        from = net.fortuna.ical4j.model.Date(readPropertyToString<DtStart>(component)).time
                        to = net.fortuna.ical4j.model.Date(readPropertyToString<DtEnd>(component)).time
                    } catch (_: Exception) {
                        from = net.fortuna.ical4j.model.DateTime(readPropertyToString<DtStart>(component)).time
                        to = net.fortuna.ical4j.model.DateTime(readPropertyToString<DtEnd>(component)).time
                    }
                }
            }

            return CalendarEvent(0L, uid, from, to, title, location, description, confirmation, categories, color, name, "", -1L, lastModified, authentication?.id!!)
        } catch (ex: Exception) {
            Log.e("error importing", ex.message, ex)
        }
        return null
    }

    private fun modelToICal(event: CalendarEvent): Calendar? {
        try {
            val vEvent = VEvent(net.fortuna.ical4j.model.Date(event.from), net.fortuna.ical4j.model.Date(event.from), event.title)
            vEvent.description.value = event.description
            vEvent.location.value = event.location //vEvent.cevent.categories))
            vEvent.status.value = event.confirmation
            //vEvent.add<PropertyContainer>(Color(ParameterList(), event.color))
            vEvent.uid.value = event.uid
            val cal = Calendar()
            cal.components.add(vEvent)
            return cal
        } catch (_: Exception) {}
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