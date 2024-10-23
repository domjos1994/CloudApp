package de.domjos.cloudapp2.worker

import android.content.Context
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.domjos.cloudapp2.data.syncer.CalendarSync
import de.domjos.cloudapp2.database.DB

class CalendarWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        return try {

            val db =
                Room
                    .databaseBuilder(this.context, DB::class.java, "CloudApp")
                    .allowMainThreadQueries()
                    .build()

            val syncer = CalendarSync(db.calendarEventDao(), db.authenticationDao())
            syncer.sync()
            Result.success()
        } catch (_: Exception) {
            Result.failure()
        }
    }
}