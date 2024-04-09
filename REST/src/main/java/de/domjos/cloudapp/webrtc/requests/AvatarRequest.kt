package de.domjos.cloudapp.webrtc.requests

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import de.domjos.cloudapp.database.model.Authentication
import java.io.BufferedInputStream


class AvatarRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/spreed/api/v1/") {

    @Throws(Exception::class)
    fun getAvatar(token: String): Bitmap? {
        val request = super.buildRequest("room/$token/avatar", "get", null)

        client.newCall(request!!).execute().use { response ->
            if(response.code == 200) {
                val input = response.body
                val inputStream = input!!.byteStream()
                val bufferedInputStream = BufferedInputStream(inputStream)
                return BitmapFactory.decodeStream(bufferedInputStream)
            }
            return null
        }
    }
}