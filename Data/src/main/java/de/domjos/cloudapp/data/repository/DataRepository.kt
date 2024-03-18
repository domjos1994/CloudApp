package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.webdav.WebDav
import de.domjos.cloudapp.webdav.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import java.util.LinkedList
import javax.inject.Inject

interface DataRepository {
    fun init()
    fun list(): MutableStateFlow<MutableList<Item>>
    fun openFolder(item: Item)
    fun openResource(item: Item, path: String)
    fun back()
}

class DefaultDataRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : DataRepository {
    private var webDav: WebDav? = null
    override fun init() {
        this.webDav = WebDav(authenticationDAO)
    }

    override fun list(): MutableStateFlow<MutableList<Item>> {
        if(this.webDav == null) {
            this.init()
        }
        return MutableStateFlow(mutableListOf())
    }

    override fun openFolder(item: Item) {
        webDav?.openFolder(item)
    }

    override fun openResource(item: Item, path: String) {
        webDav?.openResource(item, path)
    }

    override fun back() {
        webDav?.back()
    }


}