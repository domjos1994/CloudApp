/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.caldav

import android.util.Log
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp2.caldav.model.ToDoList
import de.domjos.cloudapp2.caldav.model.Todo
import de.domjos.cloudapp2.caldav.utils.Helper
import de.domjos.cloudapp2.caldav.utils.Helper.Companion.getDate
import de.domjos.cloudapp2.caldav.utils.Helper.Companion.readPropertyToString
import de.domjos.cloudapp2.database.converters.ToDoStatusConverter
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.todo.ToDoItem
import net.fortuna.ical4j.data.CalendarBuilder
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.component.VToDo
import net.fortuna.ical4j.model.property.Categories
import net.fortuna.ical4j.model.property.Created
import net.fortuna.ical4j.model.property.DtStamp
import net.fortuna.ical4j.model.property.DtStart
import net.fortuna.ical4j.model.property.Due
import net.fortuna.ical4j.model.property.LastModified
import net.fortuna.ical4j.model.property.Location
import net.fortuna.ical4j.model.property.PercentComplete
import net.fortuna.ical4j.model.property.Priority
import net.fortuna.ical4j.model.property.Status
import net.fortuna.ical4j.model.property.Summary
import net.fortuna.ical4j.model.property.Uid
import net.fortuna.ical4j.model.property.Url
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.StringWriter
import java.net.URI
import java.time.Duration
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ToDoCalDav(private val authentication: Authentication?) {
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

    fun getToDoLists(): List<ToDoList> {
        val lst = mutableListOf<ToDoList>()
        this.sardine?.list(this.basePath)?.drop(1)?.forEach { item ->
            val name = item.displayName

            if(name != null) {
                val path = "${authentication?.url}${item.path}"
                val props = item.customProps
                val color = if (props.isEmpty())
                    ""
                else
                    props.map { it }.find { it.key.endsWith("calendar-color") }?.value ?: ""

                if(!Helper.isDeleted(item)) {
                    lst.add(ToDoList(name, color, path))
                }
            }
        }
        return lst
    }

    fun insertToDoList(toDoList: ToDoList): String {
        val url = "${this.basePath}/${toDoList.name}"
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
        toDoList.path = url
        updateToDoList(toDoList)
        return url
    }

    fun updateToDoList(toDoList: ToDoList) {
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
        displayNameElement.textContent = toDoList.name
        propElement.appendChild(displayNameElement)

        // Todo
        val colorElement = document.createElement("d:x-apple-calendar-color")
        colorElement.textContent = toDoList.color
        propElement.appendChild(colorElement)

        val tf = TransformerFactory.newInstance()
        val transformer = tf.newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, "yes")
        val writer = StringWriter()
        transformer.transform(DOMSource(document), StreamResult(writer))
        val output = writer.buffer.toString()

        val okHttp = OkHttpClient()
        val request = Request.Builder()
            .url(toDoList.path)
            .addHeader("Authorization", Credentials.basic(authentication!!.userName, authentication.password))
            .method("PROPPATCH", output.toRequestBody())
            .build()
        val response = okHttp.newCall(request).execute()
        if(response.code >= 400) {
            throw Exception(response.message)
        }
    }

    fun deleteToDoList(toDoList: ToDoList) {
        this.sardine!!.delete(toDoList.path)
    }

    fun getToDos(toDoList: ToDoList, updateProgress: (Float, String) -> Unit, progressLabel: String): List<Todo> {
        val items = mutableListOf<Todo>()
        if(this.sardine != null) {
            val lst = this.sardine?.list(toDoList.path)

            val percentage = 100.0f / lst?.size!!
            var item = 0
            lst.drop(1).forEach { element ->
                try {
                    val path = "${authentication?.url}${element.path}"
                    val inputStream = this.sardine?.get(path, Helper.buildHeaders(this.authentication!!))

                    val builder = CalendarBuilder()
                    val calendar = builder.build(inputStream)
                    items.addAll(this.iCalToModel(calendar, path))
                } catch (ex: Throwable) {
                    Log.e("Error", ex.message, ex)
                } finally {
                    item += 1
                    updateProgress((percentage*item)/100.0f, String.format(progressLabel, toDoList.path))
                }
            }
        }
        return items
    }

    fun getToDos(updateProgress: (Float, String) -> Unit, progressLabel: String): Map<ToDoList, List<Todo>> {
        val mp = mutableMapOf<ToDoList, List<Todo>>()
        this.getToDoLists().forEach { calendar ->
            mp[calendar] = this.getToDos(calendar, updateProgress, progressLabel)
        }
        return mp
    }

    fun insertToDo(toDoList: ToDoList, todo: Todo): Todo {
        if(this.sardine != null) {
            val uid = UUID.randomUUID().toString()
            todo.uid = uid
            todo.path = "${toDoList.path}/$uid.ics"
            val toDo = this.modelToICal(todo)
            if(toDo != null) {
                this.sardine?.put(todo.path, Helper.getData(toDo))
            }
        }
        return todo
    }

    fun toDoToDatabase(toDoList: ToDoList, todo: Todo): ToDoItem {
        val converter = ToDoStatusConverter()
        return ToDoItem(
            uid = todo.uid,
            listUid = toDoList.path,
            listName = toDoList.name,
            listColor = toDoList.color,
            summary = todo.summary,
            start = todo.start,
            end = todo.end,
            status = converter.fromString(todo.status),
            completed = todo.completed,
            priority = todo.priority,
            location = todo.location,
            url = todo.url,
            categories = todo.categories,
            path = todo.path,
            authId = authentication?.id ?: 0L
        )
    }

    fun databaseToList(toDoItem: ToDoItem): ToDoList {
        return ToDoList(toDoItem.listName, toDoItem.listColor, toDoItem.listUid)
    }

    fun databaseToToDo(toDoItem: ToDoItem): Todo {
        val converter = ToDoStatusConverter()
        return Todo(
            uid = toDoItem.uid,
            created = null,
            lastModified = null,
            timestamp = null,
            summary = toDoItem.summary,
            start = toDoItem.start,
            end = toDoItem.end,
            status = converter.fromStatus(toDoItem.status),
            completed = toDoItem.completed,
            priority = toDoItem.priority,
            location = toDoItem.location ?: "",
            url = toDoItem.url ?: "",
            categories = toDoItem.categories ?: "",
            path = toDoItem.path ?: ""
        )
    }

    fun updateToDo(todo: Todo) {
        if(this.sardine != null) {
            val toDo = this.modelToICal(todo)
            if(toDo != null) {
                this.sardine?.put(todo.path, Helper.getData(toDo))
            }
        }
    }

    fun deleteToDo(todo: Todo) {
        if(this.sardine != null) {
            this.sardine?.delete(todo.path)
        }
    }

    private fun iCalToModel(calendar: Calendar, path: String) : List<Todo> {
        val items = mutableListOf<Todo>()
        try {
            val components = calendar.components
            components.forEach { component ->
                try {
                    if(component is VToDo) {
                        val uid = readPropertyToString<Uid>(component)
                        val created = getDate(readPropertyToString<Created>(component))
                        val lastModified = getDate(readPropertyToString<LastModified>(component))
                        val timestamp = getDate(readPropertyToString<DtStamp>(component))
                        val summary = readPropertyToString<Summary>(component)
                        val start = getDate(readPropertyToString<DtStart>(component))
                        val end = getDate(readPropertyToString<Due>(component))
                        val status = readPropertyToString<Status>(component)
                        val completed = try {
                            val item: PercentComplete = component.properties.find { it is PercentComplete } as PercentComplete
                            item.percentage
                        } catch (_: Exception) {0}
                        val priority = try {
                            val item: Priority = component.properties.find { it is Priority } as Priority
                            item.level
                        } catch (_: Exception) {0}
                        val location = readPropertyToString<Location>(component)
                        val url = readPropertyToString<Url>(component)
                        val categories = readPropertyToString<Categories>(component)

                        items.add(Todo(
                            uid, created, lastModified, timestamp, summary, start, end,
                            status, completed, priority, location, url, categories, path
                        ))
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

    private fun modelToICal(todo: Todo): Calendar? {
        try {
            val vToDo = VToDo()

            // insert summary
            vToDo.properties.add(Summary(todo.summary))

            // insert dates
            if(todo.start != null) {
                vToDo.properties.add(DtStart(net.fortuna.ical4j.model.DateTime(todo.start)))
            }
            if(todo.end != null) {
                vToDo.properties.add(Due(net.fortuna.ical4j.model.DateTime(todo.end)))
            }

            // insert priority
            vToDo.properties.add(Priority(todo.priority))

            // insert status
            try {
                vToDo.properties.add(Status(todo.status))
            } catch (_: Exception) {
                vToDo.properties.add(Status("DRAFT"))
            }

            vToDo.properties.add(PercentComplete(todo.completed))
            vToDo.properties.add(Location(todo.location))
            if(todo.url.isNotEmpty()) {
                try {vToDo.properties.add(Url(URI.create(todo.url)))} catch (_:Exception) {}
            }
            vToDo.properties.add(Categories(todo.categories))
            val cal = Calendar()
            cal.components.add(vToDo)
            return cal
        } catch (ex: Exception) {
            Log.e("Error", ex.message, ex)
        }
        return null
    }
}