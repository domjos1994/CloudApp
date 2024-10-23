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
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.DocumentsContract
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.Log
import de.domjos.cloudapp2.database.model.contacts.Contact
import de.domjos.cloudapp2.webdav.WebDav
import de.domjos.cloudapp2.webdav.model.Item
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@Suppress("SameParameterValue")
class FileSyncAdapter(
    private val context: Context,
    autoInitialize: Boolean,
    allowParallelSyncs: Boolean = false,
    private val contentResolver: ContentResolver = context.contentResolver
) : AbstractThreadedSyncAdapter(context, autoInitialize, allowParallelSyncs) {
    private val db = DB.newInstance(this.context)
    private var account: Account? = null
    private var authentication: Authentication? = null

    companion object {
        const val AUTHORITY = "de.dojodev.file_provider"
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onPerformSync(
        account: Account?,
        extras: Bundle?,
        authority: String?,
        provider: ContentProviderClient?,
        syncResult: SyncResult?
    ) {
        this.account = account

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val root = getPhoneRootDocument()
                val webdav = WebDav(authentication!!)
                val items = webdav.getList()
                if(root != null) {
                    list(webdav, items)
                }
            } catch (ex: Exception) {
                insertLogException(ex, "Problem creating files!")
            }
        }
    }

    private fun list(webDav: WebDav, items: List<Item>) {
        items.forEach { item ->
            if(!item.directory) {
                val url = authentication?.url ?: ""
                var path = if(url.endsWith("/")) {
                    item.path.replace("${authentication?.url}remote.php/dav/files/${authentication?.userName}/", "")
                } else {
                    item.path.replace("${authentication?.url}/remote.php/dav/files/${authentication?.userName}/", "")
                }
                path = "${authentication?.title}/$path"
                val uri = DocumentsContract.buildDocumentUri(AUTHORITY, path)
                val createdPath = DocumentsContract.createDocument(contentResolver, uri, item.type, item.name)
                val id = DocumentsContract.getDocumentId(createdPath)
                val data = webDav.openResource(item)
                val bais = ByteArrayInputStream(data)
                val fos = FileOutputStream(File(id ?: ""))
                bais.use { ba ->
                    fos.use { f ->
                        ba.copyTo(f)
                    }
                }
            } else {
                if(item.name != "..") {
                    webDav.openFolder(item)
                    this.list(webDav, webDav.getList())
                }
            }
        }
    }

    private fun getPhoneRootDocument(): Item? {
        var item: Item? = null
        try {
            this.authentication = this.db.authenticationDao().getSelectedItem()
            val uri = asSyncAdapter(DocumentsContract.buildRootsUri(AUTHORITY))
            val projection = arrayOf(
                DocumentsContract.Root.COLUMN_ROOT_ID,
                DocumentsContract.Root.COLUMN_TITLE,
                DocumentsContract.Root.COLUMN_ICON,
                DocumentsContract.Root.COLUMN_SUMMARY,
            )

            val cursor = this.contentResolver.query(uri, projection, null, null, null)
            cursor?.use { c ->
                while(c.moveToNext()) {
                    item = Item(
                        this.getValue(c, DocumentsContract.Root.COLUMN_TITLE, "") ?: "",
                        true,
                        "Folder",
                        this.getValue(c, DocumentsContract.Root.COLUMN_ROOT_ID, "") ?: ""
                    )
                }
            }
        } catch (ex: Exception) {
            this.insertLogException(ex, "Problem fetching phone-data!")
        }
        return item
    }

    private fun insertLogException(ex: Exception, msg: String = "", phoneContact: Contact? = null, appContact: Contact? = null) {
        try {
            var message = ""
            if(msg.isNotEmpty()) {
                message = "${msg}:\n"
            }
            message += "${ex.message}:\n${ex.stackTraceToString()}"

            val log = Log(
                date = Date(),
                itemType = "files",
                messageType = "error",
                message = message,
                object1 = phoneContact?.toString() ?: "",
                object2 = appContact?.toString() ?: ""
            )
            this.db.logDao().insertItem(log)
        } catch (_: Exception) {}
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getValue(cursor: Cursor, column: String, default: T): T? {
        val index = cursor.getColumnIndex(column)
        return if(index >= 0) {
            when(default) {
                is String -> cursor.getString(index) as T?
                is Int -> cursor.getInt(index) as T?
                is Long -> cursor.getLong(index) as T?
                is Float -> cursor.getFloat(index) as T?
                is Double -> cursor.getDouble(index) as T?
                is ByteArray -> cursor.getBlob(index) as T?
                else -> null
            }
        } else {
            default
        }
    }

    @Throws(java.lang.Exception::class)
    private fun asSyncAdapter(uri: Uri): Uri {
        return if(this.account != null) {
            uri.buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(ContactsContract.PRIMARY_ACCOUNT_NAME, this.account?.name)
                .appendQueryParameter(ContactsContract.PRIMARY_ACCOUNT_TYPE, this.account?.type).build()
        } else {
            uri
        }
    }


}