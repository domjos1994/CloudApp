package de.domjos.cloudapp.appbasics.helper

import android.graphics.Bitmap
import android.graphics.BitmapFactory

class ImageHelper {

    companion object {
        fun convertImageByteArrayToBitmap(imageData: ByteArray): Bitmap? {
            return try {
                BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            } catch (_: Exception) {
                null
            }
        }
    }
}