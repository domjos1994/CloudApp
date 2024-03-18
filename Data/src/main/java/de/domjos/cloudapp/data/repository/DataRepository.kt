package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.webdav.WebDav
import de.domjos.cloudapp.webdav.model.Item
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface DataRepository {
    val items: Flow<List<Item>>
    fun openFolder(item: Item)
    fun openResource(item: Item, path: String)
    fun back()
}

class DefaultDataRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : DataRepository {
    private val webDav: WebDav
        get() = WebDav(this.authenticationDAO)
    override val items: Flow<List<Item>>
        get() = webDav.getList()

    override fun openFolder(item: Item) {
        webDav.openFolder(item)
    }

    override fun openResource(item: Item, path: String) {
        webDav.openResource(item, path)
    }

    override fun back() {
        webDav.back()
    }


}