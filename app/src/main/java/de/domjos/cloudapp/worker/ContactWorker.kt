package de.domjos.cloudapp.worker

import android.content.Context
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import de.domjos.cloudapp.data.repository.DefaultContactRepository
import de.domjos.cloudapp.database.DB

class ContactWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {
       var state = Result.success()
       try {

           val db =
               Room
                   .databaseBuilder(this.context, DB::class.java, "CloudApp")
                   .allowMainThreadQueries()
                   .build()

           val repo = DefaultContactRepository(db.authenticationDao(), db.contactDao())
           repo.importContacts({_,_->}, {})
       } catch (_: Exception) {
           state = Result.failure()
       }
        return state
    }


}