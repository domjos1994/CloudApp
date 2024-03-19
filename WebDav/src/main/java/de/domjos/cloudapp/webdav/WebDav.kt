package de.domjos.cloudapp.webdav

import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.webdav.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList


class WebDav(private val authenticationDAO: AuthenticationDAO) {
    private var sardine: OkHttpSardine
    private var list: List<DavResource>
    private var url = ""
    private var basePath = ""

    init {
        this.list = LinkedList()
        val authentication = authenticationDAO.getSelectedItem()
        this.sardine = OkHttpSardine()
        if(authentication != null) {
            this.sardine.setCredentials(authentication.userName, authentication.password)
            this.basePath = "/remote.php/dav/files/${authentication.userName}/"
            this.list = this.sardine.list("${authentication.url}${this.basePath}")
            this.url = authentication.url
        }
    }

    fun getList(): MutableList<Item> {
        try {
            val items = LinkedList<Item>()
            if(url != "${authenticationDAO.getSelectedItem()?.url}${basePath}") {
                items.add(Item("..", true, "", ""))
            }
            list.forEach {
                var name = it.displayName
                if(name == null) {
                    name = it.path
                }

                val item = Item(name, it.isDirectory, it.contentType, it.path)
                items.add(item)
            }
            return items
        } catch(ex: Exception) {
            return mutableListOf()
        }
    }

    fun openFolder(item: Item) {
        if(item.directory) {
            this.list = this.sardine.list(item.getUrl(url))
        }
    }

    fun back() {
        this.sardine.list(this.url.replace(this.url.split("/")[this.url.split("/").size - 1], ""))
    }

    fun openResource(item: Item, path: String) {
        if(!item.directory) {
            this.sardine.get(item.getUrl(this.url)).use { input ->
                val file = File(path)
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
}