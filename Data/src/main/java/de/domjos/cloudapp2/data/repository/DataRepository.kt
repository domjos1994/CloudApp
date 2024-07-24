package de.domjos.cloudapp2.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.dao.DataItemDAO
import de.domjos.cloudapp2.database.model.webdav.DataItem
import de.domjos.cloudapp2.rest.model.shares.InsertShare
import de.domjos.cloudapp2.rest.model.shares.Share
import de.domjos.cloudapp2.rest.model.shares.Types
import de.domjos.cloudapp2.rest.model.shares.UpdateShare
import de.domjos.cloudapp2.rest.requests.AutocompleteRequest
import de.domjos.cloudapp2.rest.requests.ShareRequest
import de.domjos.cloudapp2.webdav.WebDav
import de.domjos.cloudapp2.webdav.model.Item
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.InputStream
import java.util.LinkedList
import javax.inject.Inject


interface DataRepository {
    var path: String

    fun init()
    suspend fun getList(): List<Item>
    suspend fun getFromDatabase(): List<Item>
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

    suspend fun getShares(): Flow<List<Share>>
    suspend fun insertShare(share: InsertShare): Flow<Share?>
    suspend fun deleteShare(id: Int): Flow<String>
    suspend fun updateShare(id: Int, share: UpdateShare): Flow<Share?>

    suspend fun getAutocompleteItems(text: String, shareType: Types): Flow<List<String>>
}

class DefaultDataRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO,
    private val dataItemDAO: DataItemDAO
) : DataRepository {
    private var webDav: WebDav? = null
    private var source: Item? = null
    private val basePath: String
    override var path: String = ""
    private val shareRequest: ShareRequest
        get() = ShareRequest(authenticationDAO.getSelectedItem())
    private var shares: Flow<List<Share>>? = null
    private var sharesByMe: Flow<List<Share>>? = null
    private val autoRequest: AutocompleteRequest
        get() = AutocompleteRequest(authenticationDAO.getSelectedItem())

    init {
        val publicDoc =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        this.basePath = "$publicDoc/CloudApp/${authenticationDAO.getSelectedItem()?.title}"
        this.loadShares()
    }

    override fun init() {
        if(webDav == null) {
            webDav = WebDav((authenticationDAO))
            path = webDav!!.getPath()
        }
        webDav!!.checkUser()
    }

    override suspend fun getList(): List<Item> {
        path = webDav!!.getPath()
        val auth = authenticationDAO.getSelectedItem()
        var tmp = "/remote.php/dav/files/${auth!!.userName}$path%"
        if(path == "/")  {
            tmp = "/remote.php/dav/files/${auth.userName}$path"
        }
        val authId = auth.id
        val items = LinkedList<Item>()
        dataItemDAO.getDataItemsByPath(authId, path).forEach {
            val item = Item(it.name, it.directory, it.type, it.path)
            item.exists = it.exists
            items.add(item)
        }
        val lst = dataItemDAO.getDataItemsByPath(authId, "$tmp%")
        webDav?.getList()!!.forEach {
            it.exists = exists(it)
            if(it.name != "" && it.path != "") {
                val filteredItem = lst.find { item -> it.path.split(it.name)[0]==item.path && it.name == item.name }
                if(filteredItem != null) {
                    filteredItem.path = it.path.split(it.name)[0]
                    filteredItem.name = it.name
                    filteredItem.exists = it.exists
                    filteredItem.type = it.type
                    filteredItem.directory = it.directory
                    dataItemDAO.updateDataItem(filteredItem)
                } else {
                    dataItemDAO.insertDataItem(DataItem(0L, it.name, it.path.split(it.name)[0], it.type, it.directory, it.exists, authId))
                }
            }
            items.add(it)
        }
        if(shares != null) {
            items.forEach { item ->
                shares?.collect { shares ->
                    shares.forEach { share ->
                        if(item.path.endsWith(share.file_target)) {
                            item.sharedWithMe = share
                        }
                    }
                }
                sharesByMe?.collect {shares ->
                    shares.forEach { share ->
                        if(item.path.endsWith(share.file_target)) {
                            item.sharedFromMe = share
                        }
                    }

                }
            }
        }
        return items
    }

    override suspend fun getFromDatabase(): List<Item> {
        val tmpPath  = webDav?.getPath()!!
        val auth = authenticationDAO.getSelectedItem()
        var tmp = "/remote.php/dav/files/${auth!!.userName}$tmpPath%"
        val items = LinkedList<Item>()
         if(tmpPath != "/") {
            items.add(Item("..", true, "", ""))
        } else {
            tmp = "/remote.php/dav/files/${auth.userName}$tmpPath"
        }
        dataItemDAO.getDataItemsByPath(auth.id, tmp).forEach {
            val item = Item(it.name, it.directory, it.type, it.path)
            item.exists = it.exists
            if(items.find { inside -> inside.name == item.name && inside.path == item.path } == null) {
                items.add(item)
            }
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

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
    }

    override suspend fun getShares(): Flow<List<Share>> {
        return shareRequest.getShares(false)
    }

    override suspend fun insertShare(share: InsertShare): Flow<Share?> {
        val result = shareRequest.addShare(share)
        this.loadShares()
        return result
    }

    override suspend fun deleteShare(id: Int): Flow<String> {
        val result = shareRequest.deleteShare(id)
        this.loadShares()
        return result
    }

    override suspend fun updateShare(id: Int, share: UpdateShare): Flow<Share?> {
        val result = shareRequest.updateShare(id, share)
        this.loadShares()
        return result
    }

    override suspend fun getAutocompleteItems(text: String, shareType: Types): Flow<List<String>> {
        return autoRequest.getItem(shareType, text)
    }

    private fun loadShares() {
        shares = this.shareRequest.getShares(true)
        sharesByMe = this.shareRequest.getShares(false)
    }
}