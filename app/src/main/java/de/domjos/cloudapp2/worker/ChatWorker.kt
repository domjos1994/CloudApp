/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.worker

import android.content.Context
import androidx.room.Room
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.domjos.cloudapp2.R
import de.domjos.cloudapp2.activities.MainActivity
import de.domjos.cloudapp2.appbasics.helper.Notifications
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.rest.requests.RoomRequest
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Suppress("unused")
class ChatWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    @Suppress("UNCHECKED_CAST")
    @OptIn(DelicateCoroutinesApi::class)
    override fun doWork(): Result {
        return try {
            val name = this.context.getString(R.string.channel_chat)
            Notifications.createBubbleIfNotExists(context, Notifications.channel_id_chat, name)

            // load database
            val db =
                Room
                    .databaseBuilder(this.context, DB::class.java, "CloudApp")
                    .allowMainThreadQueries()
                    .build()
            val auth = db.authenticationDao().getSelectedItem()

            GlobalScope.launch(Dispatchers.IO) {
                // load rooms
                val request = RoomRequest(auth)
                val rooms = request.getRooms().first()
                rooms.forEach { room ->
                    Notifications.createBubble(
                        context,
                        room.displayName ?: "",
                        name,
                        MainActivity::class.java as Class<Any>,
                        room.icon
                    )
                }
            }

            Result.success()
        } catch (ex: Exception) {
            val dataBuilder = Data.Builder()
            dataBuilder.putString("Error", ex.message)
            Result.failure(dataBuilder.build())
        }
    }
}