/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import de.domjos.cloudapp2.providers.DavCursor
import de.domjos.cloudapp2.providers.FileProvider
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FileSyncAdapter(
    private val context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    private val contentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onPerformSync(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        provider: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            val cursor = contentResolver.query(FileProvider.FILES_URI, null, null, null, null)
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val path = c.getString(DavCursor.FILE_PATH_INDEX)
                }
            }
        }
    }

}