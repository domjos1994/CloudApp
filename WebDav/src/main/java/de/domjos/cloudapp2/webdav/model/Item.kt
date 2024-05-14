package de.domjos.cloudapp2.webdav.model

data class Item(var name: String, var directory: Boolean, val type: String, val path: String) {
    var exists: Boolean = false

    fun getUrl(url: String): String {
        return if(url.endsWith("/")) {
            "$url$path-".replace("/-", "")
        } else {
            "$url$path"
        }
    }
}