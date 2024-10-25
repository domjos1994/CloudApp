package de.domjos.cloudapp2.appbasics.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.provider.Settings
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import de.domjos.cloudapp2.appbasics.R
import java.util.Date
import kotlin.random.Random

class Notifications {

    companion object {
        private var lastId: Int = 0
        private var ts: Long = 0

        const val channel_id = "cloud_app_notifications"
        const val channel_id_chat = "cloud_app_notifications_chat"

        fun showBasicNotification(context: Context, header: String, content: String): NotificationCompat.Builder? {
            if(hasPermission(android.Manifest.permission.POST_NOTIFICATIONS, context)) {
                val notificationManager=context.getSystemService(NotificationManager::class.java)

                val notification= NotificationCompat.Builder(context, channel_id)
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

        @SuppressLint("NewApi")
        fun createBubble(context: Context, channelName: String, name: String, activity: Class<Any>, bitmap: Bitmap?) {
            try {
                val notificationManager= context.getSystemService(NotificationManager::class.java)
                var channel = notificationManager.getNotificationChannel(channel_id_chat)
                if(channel == null) {
                    createBubbleIfNotExists(context, channel_id_chat, channelName)
                    channel = notificationManager.getNotificationChannel(channel_id_chat)
                }


                if(channel?.canBubble() == true) {
                    val target = Intent(context, activity)
                    val bubbleIntent = PendingIntent
                        .getActivity(
                            context, 0, target,
                            PendingIntent.FLAG_MUTABLE
                        )
                    val category = "de.dojodev.category.bubble"

                    val chatPartner = Person.Builder()
                        .setName(name)
                        .setImportant(true)
                        .build()




                    val icon = Icon.createWithBitmap(bitmap)

                    val bubbleData = Notification.BubbleMetadata.Builder(bubbleIntent, icon)
                        .setDesiredHeight(600)
                        .build()

                    val builder = Notification.Builder(context, channel_id_chat)
                        .setSmallIcon(icon)
                        .setBubbleMetadata(bubbleData)
                        .setShortcutId(category)
                        .addPerson(chatPartner)
                    val notification = builder.build()
                    notificationManager.notify(23, notification)
                } else {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, "de.domjos.cloudapp2")
                    }
                    intent.flags = FLAG_ACTIVITY_NEW_TASK

                    context.startActivity(intent)
                }
            } catch (ex: Exception) {
                Log.e("Notification", ex.message, ex)
            }
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

        fun createIfNotExists(context: Context, channelId: String, name: String) {
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(channelId)
            if(channel == null) {
                createNotificationChannel(context, channelId, name)
            }
        }

        @SuppressLint("NewApi")
        fun createBubbleIfNotExists(context: Context, channelId: String, name: String) {
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val channel = manager.getNotificationChannel(channelId)
            if(channel == null) {
                createBubbleNotificationChannel(context, channelId, name)
            } else {
                if(!channel.canBubble() && manager.areBubblesEnabled()) {
                    manager.deleteNotificationChannel(channelId)
                    createBubbleNotificationChannel(context, channelId, name)
                }
            }
        }



        private fun Context.bitmapFromResource(
            @DrawableRes resId:Int
        ) = BitmapFactory.decodeResource(
            resources,
            resId
        )

        private fun createNotificationChannel(context: Context, channelId: String, name: String) {
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_NONE).apply {
                    setSound(null, null)
                }
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        @SuppressLint("NewApi")
        private fun createBubbleNotificationChannel(context: Context, channelId: String, name: String) {
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(null, null)
                    setAllowBubbles(true)
                }
            val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

fun hasPermission(permission: String, context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}