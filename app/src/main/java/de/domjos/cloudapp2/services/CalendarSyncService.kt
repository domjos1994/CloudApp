package de.domjos.cloudapp2.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import de.domjos.cloudapp2.adapter.CalendarSyncAdapter

class CalendarSyncService : Service() {
    override fun onCreate() {
        synchronized(sSyncAdapterLock) {
            sSyncAdapter = sSyncAdapter ?: CalendarSyncAdapter(applicationContext, true, true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return sSyncAdapter?.syncAdapterBinder
    }

    companion object {
        // Storage for an instance of the sync adapter
        @SuppressLint("StaticFieldLeak")
        private var sSyncAdapter: CalendarSyncAdapter? = null
        // Object to use as a thread-safe lock
        private val sSyncAdapterLock = Any()
    }
}