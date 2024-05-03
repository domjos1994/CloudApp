package de.domjos.cloudapp.appbasics.helper

import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import de.domjos.cloudapp.appbasics.R
import kotlin.random.Random

class Notifications {

    companion object {
        private var lastId: Int = 0

        fun showBasicNotification(context: Context, header: String, content: String): NotificationCompat.Builder {
            val notificationManager=context.getSystemService(NotificationManager::class.java)

            val notification= NotificationCompat.Builder(context,"cloud_app_notifications")
                .setContentTitle(header)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notifications)
                .setLargeIcon(context.bitmapFromResource(R.drawable.icon))
                .setBadgeIconType(R.drawable.icon)
                .setPriority(NotificationManager.IMPORTANCE_DEFAULT)
                .setAutoCancel(true)

            lastId = Random.nextInt()
            notificationManager.notify(
                lastId,
                notification.build()
            )
            return notification
        }

        fun updateNotification(context: Context, progress: Float, text: String, notification: NotificationCompat.Builder) {
            val notificationManager=context.getSystemService(NotificationManager::class.java)

            notification
                .setContentText(text)
                .setProgress(100, (progress * 100).toInt(), false)

            notificationManager.notify(lastId, notification.build())
        }

        fun deleteNotification(context: Context) {
            val notificationManager=context.getSystemService(NotificationManager::class.java)
            notificationManager.cancel(lastId)
        }

        private fun Context.bitmapFromResource(
            @DrawableRes resId:Int
        ) = BitmapFactory.decodeResource(
            resources,
            resId
        )
    }
}