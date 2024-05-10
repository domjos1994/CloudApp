package de.domjos.cloudapp.appbasics.helper

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.domjos.cloudapp.appbasics.R
import java.util.Date
import kotlin.random.Random

class Notifications {

    companion object {
        private var lastId: Int = 0
        private var ts: Long = 0

        fun showBasicNotification(context: Context, header: String, content: String): NotificationCompat.Builder? {
            if(hasPermission(android.Manifest.permission.POST_NOTIFICATIONS, context)) {
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
                ts = Date().time
                return notification
            }
            return null
        }

        fun updateNotification(context: Context, progress: Float, notification: NotificationCompat.Builder?) {
            if(notification != null && hasPermission(android.Manifest.permission.POST_NOTIFICATIONS, context)) {
                val notificationManager=context.getSystemService(NotificationManager::class.java)

                notification
                    .setProgress(100, (progress * 100).toInt(), false)

                val current = Date().time
                if(current > ts * 5000) {
                    notificationManager.notify(lastId, notification.build())
                    ts = current
                }
            }
        }

        fun deleteNotification(context: Context) {
            if(hasPermission(android.Manifest.permission.POST_NOTIFICATIONS, context)) {
                val notificationManager=context.getSystemService(NotificationManager::class.java)
                notificationManager.cancel(lastId)
            }
        }

        private fun Context.bitmapFromResource(
            @DrawableRes resId:Int
        ) = BitmapFactory.decodeResource(
            resources,
            resId
        )
    }
}

fun hasPermission(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}