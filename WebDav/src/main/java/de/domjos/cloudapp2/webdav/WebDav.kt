package de.domjos.cloudapp2.webdav

import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.webdav.model.Item
import java.io.File
import java.io.InputStream
import java.util.LinkedList
import java.util.Stack


class WebDav(private val authentication: Authentication) {
    private lateinit var sardine: OkHttpSardine
    private var list: List<DavResource>
    private var currentUrl = ""
    private var lastUrls = Stack<String>()
    private var basePath = ""

    init {
        this.list = LinkedList()
        this.connect()
    }

    fun checkUser() {
        this.connect()
    }

    fun getPath(): String {
        return currentUrl
    }

    fun getSimplePath(): String {
        return currentUrl.replace("${authentication.url}${basePath}", "")
    }

    fun getList(): List<Item> {
        val lst = LinkedList<Item>()
        try {
            if(this.currentUrl != "${authentication.url}${basePath}/") {
                lst.add(Item("..", true, "", ""))
            }
            list.forEach { resource ->
                if(!this.currentUrl.endsWith(resource.path)) {
                    val directory = resource.isDirectory
                    val type = resource.contentType

                    if(directory) {
                        if(!currentUrl.endsWith(resource.path)) {
                            val name = resource.path.split("/")[resource.path.split("/").size - 2]
                            val path = "${currentUrl}${name}/"
                            lst.add(Item(name, true, type, path))
                        }
                    } else {
                        val name = resource.path.split("/")[resource.path.split("/").size - 1]
                        val path = "${currentUrl}${name}"
                        lst.add(Item(name, false, type, path))
                    }
                }
            }
        } catch (_: Exception) {}
        return lst
    }

    fun openFolder(item: Item) {
        if(item.directory) {
            if(this.currentUrl != item.path) {
                this.lastUrls.push(this.currentUrl)
                this.currentUrl = item.path
                this.list = this.sardine.list(this.currentUrl)
            }
        }
    }

    fun reload() {
        this.list = this.sardine.list(this.currentUrl)
    }

    fun back() {
        this.currentUrl = this.lastUrls.pop()
        this.list = this.sardine.list(currentUrl)
    }

    fun openResource(item: Item, path: String) {
        if(!item.directory) {
            this.sardine.get(item.getUrl(this.currentUrl)).use { input ->
                val file = File("$path/${item.name.replace(" ", "_")}")
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }

    fun delete(item: Item) {
        this.sardine.delete(item.getUrl(this.currentUrl))
    }

    fun createFolder(name: String) {
        this.sardine.createDirectory("${this.currentUrl}/$name")
    }

    fun move(source: Item, target: Item) {
        if(target.directory) {
            this.sardine.move(source.getUrl(this.currentUrl), "${target.getUrl(this.currentUrl)}/${source.name}")
        }
    }

    fun uploadFile(name: String, stream: InputStream) {
        this.sardine.put("${this.currentUrl}/$name", stream.readBytes())
        stream.close()
    }

    private fun connect() {
        this.sardine = OkHttpSardine()
        this.sardine.setCredentials(authentication.userName, authentication.password)
        if(this.currentUrl.isEmpty()) {
            this.basePath = "/remote.php/dav/files/${authentication.userName}"
            this.currentUrl = "${authentication.url}$basePath/"
        }
        this.list = this.sardine.list(currentUrl)
    }
}