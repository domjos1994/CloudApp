package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import de.domjos.cloudapp2.database.DB


class CalendarSyncAdapter @JvmOverloads constructor(
    private val context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    private val contentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {


    override fun onPerformSync(
        account: Account?,
        bundle: Bundle?,
        authority: String?,
        contentProviderClient: ContentProviderClient?,
        syncResult: SyncResult?
    ) {

        val db = DB.newInstance(this.context)
        val helper = PhoneCalendarHelper(account, this.contentResolver, db)
        helper.sync()
    }
}