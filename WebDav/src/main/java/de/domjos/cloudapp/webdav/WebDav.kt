package de.domjos.cloudapp.webdav

import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webdav.model.Item
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.LinkedList


class WebDav(private val authenticationDAO: AuthenticationDAO) {
    private var sardine: OkHttpSardine
    private var list: List<DavResource>
    private var url = ""
    private var currentUrl = ""
    private var lastUrl = ""
    private var basePath = ""
    private var authentication: Authentication?

    init {
        this.list = LinkedList()
        this.authentication = authenticationDAO.getSelectedItem()
        this.sardine = OkHttpSardine()
        if(authentication != null) {
            this.sardine.setCredentials(authentication!!.userName, authentication!!.password)
            this.basePath = "/remote.php/dav/files/${authentication!!.userName}"
            this.list = this.sardine.list("${authentication!!.url}${this.basePath}")
            this.url = authentication!!.url
            this.currentUrl = "$url$basePath"
        }
    }

    fun checkUser() {
        if(this.authentication != null) {
            if(this.authentication!!.id != authenticationDAO.getSelectedItem()!!.id) {
                this.authentication = authenticationDAO.getSelectedItem()
                this.sardine = OkHttpSardine()
                this.sardine.setCredentials(authentication!!.userName, authentication!!.password)
                this.basePath = "/remote.php/dav/files/${authentication!!.userName}"
                this.list = this.sardine.list("${authentication!!.url}${this.basePath}")
                this.url = authentication!!.url
                this.currentUrl = "$url$basePath"
            }
        }
    }

    fun getPath(): String {
        var tmp = this.currentUrl
        tmp = tmp.replace("$url$basePath", "")
        if(tmp.endsWith("/")) {
            tmp = "$tmp-".replace("/-", "")
        }
        if(tmp == "") {
            tmp = "/"
        }
        return tmp
    }

    fun getList(): List<Item> {
        try {
            val items = LinkedList<Item>()
            if(currentUrl != "${authentication?.url}${basePath}") {
                items.add(Item("..", true, "", ""))
            }
            list.forEach {
                val path: String
                var name: String?
                val pathPart: String?
                if(it.isDirectory) {
                    path = "${it.path}-".replace("/-", "")
                    name = it.displayName
                    if(name == null) {
                        name = path.split("/")[path.split("/").size-1]
                    }
                    pathPart = path.replace(basePath, "")
                } else {
                    path = it.path
                    name = it.displayName
                    if(name == null) {
                        name = path.split("/")[path.split("/").size-1]
                    }
                    pathPart = path.replace(basePath, "")
                }


                val item = Item(name, it.isDirectory, it.contentType, path)

                var tmp = currentUrl
                if(tmp.endsWith("/")) {
                    tmp = "$tmp-".replace("/-", "")
                }
                if(!(tmp.endsWith(pathPart) && it.isDirectory)) {
                    items.add(item)
                }
            }
            return items
        } catch(ex: Exception) {
            return mutableListOf()
        }
    }

    fun openFolder(item: Item) {
        if(item.directory) {
            if(this.currentUrl != item.getUrl(this.url)) {
                this.lastUrl = this.currentUrl
                this.currentUrl = item.getUrl(url)
                this.list = this.sardine.list(this.currentUrl)
            }
        }
    }

    fun reload() {
        this.list = this.sardine.list(this.currentUrl)
    }

    fun back() {
        try {
            if(this.lastUrl.endsWith("/")) {
                this.lastUrl = "$lastUrl-".replace("/-","")
            }

            this.currentUrl = this.lastUrl
            this.lastUrl = this.lastUrl.replace(this.lastUrl.split("/")[this.lastUrl.split("/").size-1], "")
            this.list = this.sardine.list(this.currentUrl)
        } catch (ex: Exception) {
            this.currentUrl = "$url$basePath"
            this.lastUrl = ""
            this.list = this.sardine.list(this.currentUrl)
        }
    }

    fun openResource(item: Item, path: String) {
        if(!item.directory) {
            this.sardine.get(item.getUrl(this.url)).use { input ->
                val file = File("$path/${item.name.replace(" ", "_")}")
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
        }
    }

    fun delete(item: Item) {
        this.sardine.delete(item.getUrl(this.url))
    }

    fun createFolder(name: String) {
        this.sardine.createDirectory("${this.currentUrl}/$name")
    }

    fun move(source: Item, target: Item) {
        if(target.directory) {
            this.sardine.move(source.getUrl(this.url), "${target.getUrl(this.url)}/${source.name}")
        }
    }

    fun uploadFile(name: String, stream: InputStream) {
        this.sardine.put("${this.currentUrl}/$name", stream.readBytes())
        stream.close()
    }
}