/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.dojodev.cloudapp2.features.exportfeature.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument.Page
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import de.dojodev.cloudapp2.features.exportfeature.base.BaseExportBuilder
import de.domjos.cloudapp2.appbasics.R
import de.domjos.cloudapp2.rest.model.notes.Note
import de.domjos.cloudapp2.rest.model.notifications.Notification
import de.domjos.cloudapp2.webdav.model.Item
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.coroutineContext

@Suppress("SameParameterValue")
class PDFExportBuilder(private val context: Context) : BaseExportBuilder(context) {
    private lateinit var document: PdfDocument
    private var page: Page? = null
    private lateinit var canvas: Canvas

    private val width = 595
    private val height = 842
    private val borderWidth = 40.0f
    private val borderHeight = 40.0f
    private var y = borderHeight

    override suspend fun doExport(id: Long?, type: String, open: Boolean): String {
        update(context.getString(R.string.export_fetch))
        this.document = this.openPdf()
        val result = super.doExport(id, type, open)
        this.closePdf(this.document)
        update(context.getString(R.string.export_success))
        return result
    }

    override suspend fun exportNotifications(): String {
        var notifications = listOf<Notification>()
        super.notificationRequest.getNotifications().collect {items ->
            notifications = items
        }
        notifications = notifications.filter { super.id==null || it.notification_id==super.id }

        update(context.getString(R.string.export_write))

        this.printHeader("Export")
        this.printSubHeader("Notifications")

        notifications.forEach { notification ->
            this.newPage()
            this.printSubSubHeader(notification.subject)
            this.printImage(notification.icon)
            this.printContent("App: ${notification.app}")
            this.printContent("Date: ${notification.datetime}")
            this.printContent("User: ${notification.user}")
            this.printContent("Link: ${notification.link}")
            this.printContent("Actions:\n ${notification.actions.joinToString(",\n") { it.link }}")
        }

        return this.path
    }

    override suspend fun exportData(): String {
        val items = ConcurrentLinkedQueue<Item>()
        items.addAll(super.webDav.getList())
        items.forEach { item -> addItems(item, items)}

        update(context.getString(R.string.export_write))

        this.printHeader("Export")
        this.printSubHeader("Data")

        items.forEach { item ->
            this.newPage()
            this.printSubSubHeader(item.name)
            this.printContent("Path: ${item.path}")
            this.printContent("Directory: ${item.directory}")
            this.printContent("Type: ${item.type}")
            this.printContent("Exists: ${item.exists}")
            this.printContent("Shared With Me: ${item.sharedWithMe?.displayname_owner ?: ""}")
            this.printContent("Shared By Me: ${item.sharedFromMe?.share_with ?: ""}")
        }

        return this.path
    }

    private fun addItems(item: Item, items: ConcurrentLinkedQueue<Item>) {
        if(item.directory && item.name != "..") {
            super.webDav.openFolder(item)
            val subs = super.webDav.getList()
            items.addAll(subs.filter { it.name != ".." })
            subs.forEach { sub ->
                this.addItems(sub, items)
            }
        }
    }

    override suspend fun exportNotes(): String {
        var notes = mutableListOf<Note>()
        super.noteRequest.getNotes().collect { noteList ->
            notes.addAll(noteList)
        }
        notes = notes.filter { super.id==null || super.id==it.id.toLong() }.toMutableList()

        update(context.getString(R.string.export_write))

        this.printHeader("Export")
        this.printSubHeader("Notes")

        notes.forEach { note ->
            this.newPage()
            this.printSubSubHeader(note.title)
            this.printContent("Category: ${note.category}")
            this.printContent("Favorite: ${note.favorite}")
            this.printContent(note.content)
        }

        return this.path
    }

    override suspend fun exportCalendars(): String {
        val calendarEvents = super.calendarEventDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        this.printHeader("Export")
        this.printSubHeader("Calendars")

        calendarEvents.forEach { event ->
            this.newPage()
            this.printSubSubHeader(event.title)
            this.printContent("Start: ${event.string_from}")
            this.printContent("End: ${event.string_to}")
            this.printContent("Location: ${event.location}")
            this.printContent("Confirmation: ${event.confirmation}")
            this.printContent("Calendar: ${event.calendar}")
            this.printContent(event.description)
        }

        return this.path
    }

    override suspend fun exportContacts(): String {
        val contacts = super.contactDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        this.printHeader("Export")
        this.printSubHeader("Contacts")

        contacts.forEach { contact ->
            this.newPage()
            this.printSubSubHeader(contact.toString())
            if (contact.photo != null) {
                this.printImage(contact.photo!!)
            }
            this.printContent("Prefix/Suffix: ${contact.prefix}/${contact.suffix}")
            this.printContent("Name: ${contact.givenName} ${contact.familyName ?: ""}")
            this.printContent("Organization: ${contact.organization}")
            this.printContent("Additional: ${contact.additional}")
            this.printContent("Birthday: ${contact.birthDay}")
            this.printContent("Categories: ${contact.categories.joinToString(",")}")
            this.printContent("Phone-Numbers: ${contact.phoneNumbers.joinToString(",")}")
            this.printContent("Address-Book: ${contact.addressBook}")
            this.printContent("Addresses:")
            contact.addresses.forEach {
                this.printContent(
                    "${it.street}, ${it.postalCode} ${it.locality}, ${it.country}, ${it.extendedAddress}" + it.types.joinToString(
                        "-"
                    ) { m -> m.name })
            }
            this.printContent("Email-Addresses:")
            contact.emailAddresses.forEach { this.printContent(it.value) }
        }

        return this.path
    }

    override suspend fun exportToDos(): String {
        val todos = super.toDoItemDAO.getAll(authenticationDAO.getSelectedItem()?.id ?: 0)
            .filter { super.id == null || super.id==it.id }

        update(context.getString(R.string.export_write))

        this.printHeader("Export")
        this.printSubHeader("ToDos")

        todos.forEach { todo ->
            this.newPage()
            this.printSubSubHeader(todo.summary)
            this.printContent("Start: ${todo.start}")
            this.printContent("End: ${todo.end}")
            this.printContent("Categories: ${todo.categories}")
            this.printContent("Status: ${todo.status}")
            this.printContent("Priority: ${todo.priority}")
            this.printContent("Completed: ${todo.completed}")
        }

        return this.path
    }

    override suspend fun exportChats(): String {
        return this.path
    }

    override fun getSupportedTypes(): List<String> {
        return listOf(super.notifications, super.data, super.notes, super.contacts, super.calendars, super.todos)
    }

    override fun getExtension(): List<String> {
        return listOf("pdf")
    }

    private fun openPdf(): PdfDocument {
        this.document = PdfDocument()
        this.newPage()
        return this.document
    }

    private fun closePdf(document: PdfDocument) {
        this.closePage()

        val stream = FileOutputStream(super.path)
        document.writeTo(stream)
        stream.close()
        document.close()
    }

    private fun printHeader(value: String) {
        val paint = Paint()
        paint.textSize = 24f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.LEFT
        this.canvas.drawText(value, borderWidth, this.y, paint)
        this.y += 30
    }

    private fun printSubHeader(value: String) {
        val paint = Paint()
        paint.textSize = 20f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.LEFT
        this.canvas.drawText(value, borderWidth, this.y, paint)
        this.y += 30
    }

    private fun printSubSubHeader(value: String) {
        val paint = Paint()
        paint.textSize = 16f
        paint.typeface = android.graphics.Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.LEFT
        this.canvas.drawText(value, borderWidth, this.y, paint)
        this.y += 20
    }

    private fun printContent(value: String) {
        val paint = Paint()
        paint.textSize = 14f
        paint.typeface = android.graphics.Typeface.DEFAULT
        paint.textAlign = Paint.Align.LEFT
        this.canvas.drawText(value, borderWidth, this.y, paint)
        this.y += 20
    }

    private fun printImage(array: ByteArray) {
        val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
        this.canvas.drawBitmap(bitmap, borderWidth, this.y, null)
        this.y += bitmap.height + 20
    }

    private suspend fun printImage(url: String) {
        urlToBitmap(CoroutineScope(coroutineContext), url, this.context, {
            canvas.drawBitmap(it,borderWidth, this.y, null)
            this.y += it.height + 20
        }, {})
    }

    private fun newPage() {
        this.closePage()

        val info = PdfDocument
            .PageInfo
            .Builder(width, height, document.pages.size + 1)
        this.page = document.startPage(info.create())
        this.canvas = page!!.canvas
    }

    private fun closePage() {
        if(this.page != null) {
            this.canvas.save()
            this.document.finishPage(this.page)
            this.page = null
            this.y = this.borderHeight
        }
    }

    private suspend fun urlToBitmap(
        scope: CoroutineScope,
        imageURL: String,
        context: Context,
        onSuccess: (bitmap: Bitmap) -> Unit,
        onError: (error: Throwable) -> Unit
    ) {
        var bitmap: Bitmap? = null
        val loadBitmap = scope.launch(Dispatchers.IO) {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageURL)
                .size(300, 300)
                .decoderFactory(SvgDecoder.Factory())
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            if (result is SuccessResult) {
                bitmap = (result.drawable as BitmapDrawable).bitmap
            } else if (result is ErrorResult) {
                cancel(result.throwable.localizedMessage ?: "ErrorResult", result.throwable)
            }
        }
        loadBitmap.invokeOnCompletion { throwable ->
            bitmap?.let {
                onSuccess(it)
            } ?: throwable?.let {
                onError(it)
            } ?: onError(Throwable("Undefined Error"))
        }
        loadBitmap.join()
    }

}