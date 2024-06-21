package de.domjos.cloudapp2.webdav.model

import de.domjos.cloudapp2.rest.model.shares.Share

data class Item(var name: String, var directory: Boolean, val type: String, val path: String) {
    var exists: Boolean = false
    var sharedWithMe: Share? = null
    var sharedFromMe: Share? = null

    fun getUrl(url: String): String {
        return if(url.endsWith("/")) {
            "$url$path-".replace("/-", "")
        } else {
            "$url$path"
        }
    }
}