package de.domjos.cloudapp.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.webdav.WebDav
import de.domjos.cloudapp.webdav.model.Item
import java.io.File
import java.io.InputStream
import java.util.LinkedList
import javax.inject.Inject


interface DataRepository {
    var path: String
    fun init()
    fun getList(): List<Item>
    fun openFolder(item: Item)
    fun reload()
    fun hasFolderToMove(): Boolean
    fun getFolderToMove(): String
    fun setToMove(item: Item)
    fun move(item: Item)
    fun createFolder(name: String)
    fun delete(item: Item)
    fun createFile(name: String, stream: InputStream)
    fun openResource(item: Item, path: String)
    fun back()
    fun createDirs(): String
    fun exists(item: Item): Boolean

    fun openFile(path: String, item: Item, context: Context)
    fun hasAuthentications(): Boolean
}

class DefaultDataRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : DataRepository {
    private var webDav: WebDav? = null
    private var source: Item? = null
    private val basePath: String
    override var path: String = ""

    init {
        val publicDoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        this.basePath = "$publicDoc/CloudApp/${authenticationDAO.getSelectedItem()?.title}"
    }

    override fun init() {
        if(webDav == null) {
            webDav = WebDav((authenticationDAO))
            path = webDav!!.getPath()
        }
        webDav!!.checkUser()
    }

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
    }

    override fun getList(): List<Item> {
        path = webDav!!.getPath()
        val items = LinkedList<Item>()
        webDav?.getList()!!.forEach {
            it.exists = exists(it)
            items.add(it)
        }
        return items
    }

    override fun openFolder(item: Item) {
        webDav?.openFolder(item)!!
    }

    override fun reload() {
        webDav?.reload()
    }

    override fun hasFolderToMove(): Boolean {
        return source != null
    }

    override fun getFolderToMove(): String {
        return source?.path ?: ""
    }

    override fun setToMove(item: Item) {
        source = item
    }

    override fun move(item: Item) {
        if(this.hasFolderToMove()) {
            webDav?.move(this.source!!, item)
        }
    }

    override fun createFolder(name: String) {
        this.webDav?.createFolder(name)
    }

    override fun createFile(name: String, stream: InputStream) {
        this.webDav?.uploadFile(name, stream)
    }

    override fun delete(item: Item) {
        this.webDav?.delete(item)
    }

    override fun openResource(item: Item, path: String) {
        webDav?.openResource(item, path)!!
    }

    override fun back() {
        webDav?.back()!!
    }


    override fun createDirs(): String {
        val fBasePath = File(basePath)
        fBasePath.mkdirs()
        var dir = fBasePath.absolutePath

        this.path.split("/").forEach { directory ->
            if(directory != "") {
                dir = if(dir.endsWith("/")) {
                    "$dir$directory"
                } else {
                    "$dir/$directory"
                }

                val f = File(dir)
                if(!f.exists()) run {
                    f.mkdirs()
                }
            }
        }
        return dir
    }

    override fun exists(item: Item): Boolean {
        val fBasePath = File(basePath)
        fBasePath.mkdirs()
        var dir = fBasePath.absolutePath

        this.path.split("/").forEach { directory ->
            if(directory != "") {
                dir = if(dir.endsWith("/")) {
                    "$dir$directory"
                } else {
                    "$dir/$directory"
                }
            }
        }

        val file = File("$dir/${item.name.replace(" ", "_")}")
        return file.exists()
    }

    @OptIn(UnstableApi::class)
    override fun openFile(path: String, item: Item, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        val f = File(path)
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            f
        )
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val type: String
        when(f.extension.lowercase()) {
            "pdf" -> type = "application/pdf"
            "txt" -> type = MimeTypes.BASE_TYPE_TEXT
            "csv" -> type = MimeTypes.BASE_TYPE_TEXT
            "rtf" -> type = MimeTypes.BASE_TYPE_TEXT
            "mp3" -> type = MimeTypes.BASE_TYPE_AUDIO
            "wav" -> type = MimeTypes.AUDIO_WAV
            "mp4" -> type = MimeTypes.APPLICATION_MP4
            "avi" -> type = MimeTypes.VIDEO_AVI
            "jpg" -> type = MimeTypes.IMAGE_JPEG
            "jpeg" -> type = MimeTypes.IMAGE_JPEG
            "png" -> type = MimeTypes.IMAGE_PNG
            "BMP" -> type = MimeTypes.IMAGE_BMP
            "gif" -> type = MimeTypes.BASE_TYPE_IMAGE
            else -> type = "*/*"
        }
        intent.setDataAndType(uri, type)
        startActivity(context, intent, Bundle())
    }
}