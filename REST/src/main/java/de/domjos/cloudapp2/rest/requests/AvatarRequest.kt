package de.domjos.cloudapp2.rest.requests

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import de.domjos.cloudapp2.database.model.Authentication
import java.io.BufferedInputStream

/**
 * Get Avatars from Rooms by Token
 * @author Dominic Joas
 */
class AvatarRequest(authentication: Authentication?) : BasicRequest(authentication, "/ocs/v2.php/apps/spreed/api/v1/") {

    /**
     * Get The avatar by a room-token
     * @param token the token of the room
     * @return the Bitmap of Avatar
     */
    @Throws(Exception::class)
    fun getAvatar(token: String): Bitmap? {

        // send request
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