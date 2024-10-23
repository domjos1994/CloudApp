/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.webdav.WebDav
import de.domjos.cloudapp2.webdav.model.Item
import java.io.ByteArrayInputStream

class FileProvider : ContentProvider() {
    private var webdav: WebDav? = null

    companion object {
        const val AUTHORITY = "de.dojodev.file_provider"

        val FILES_URI: Uri = Uri.parse("content://de.dojodev.file_provider/files")
        val DIRECTORY_URI: Uri = Uri.parse("content://de.dojodev.file_provider/directories")
    }

    override fun onCreate(): Boolean {
        try {
            val db = if(this.context == null) null else DB.newInstance(this.context!!)
            if(db == null) {
                return false
            } else {
                val auth = db.authenticationDao().getSelectedItem()
                if(auth == null) {
                    return false
                } else {
                    this.webdav = WebDav(auth)
                    return true
                }
            }
        }catch(_: Exception) {}
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        try {
            if(this.webdav == null) {
                this.onCreate()
                return null
            }

            if(selectionArgs?.isEmpty() != false) {
                this.webdav?.reload()
            }
            val name = selectionArgs?.getOrElse(0) {""} ?: ""
            val directory = (selectionArgs?.getOrElse(1) {""} ?: "").toBoolean()
            val type = selectionArgs?.getOrElse(2) {""} ?: ""
            val path = selectionArgs?.getOrElse(3) {""} ?: ""

            val item = Item(name, directory, type, path)
            this.webdav?.openFolder(item)
            return DavCursor(webdav!!, this.webdav?.getList() ?: listOf())
        } catch (_: Exception) {}
        return DavCursor(webdav!!, listOf())
    }

    override fun getType(uri: Uri): String {
        return AUTHORITY
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        try {
            if(this.webdav == null) {
                this.onCreate()
                return null
            }

            val name = values?.getAsString(DavCursor.FILE_NAME) ?: ""
            if(name.isNotEmpty()) {
                when(uri) {
                    FILES_URI -> {
                        val data = values?.getAsByteArray(DavCursor.FILE_CONTENT)
                        if(data != null) {
                            val stream = ByteArrayInputStream(data)
                            this.webdav?.uploadFile(name, stream)
                            stream.close()
                        }
                        return FILES_URI
                    }
                    DIRECTORY_URI -> {
                        this.webdav?.createFolder(name)
                        return DIRECTORY_URI
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        try {
            if(this.webdav == null) {
                this.onCreate()
                return 0
            }

            val name = selectionArgs?.getOrElse(0) {""} ?: ""
            val directory = (selectionArgs?.getOrElse(1) {""} ?: "").toBoolean()
            val type = selectionArgs?.getOrElse(2) {""} ?: ""
            val path = selectionArgs?.getOrElse(3) {""} ?: ""
            val item = Item(name, directory, type, path)
            this.webdav?.delete(item)
            return 1
        } catch (_: Exception) {}
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        try {
            if(this.webdav == null) {
                this.onCreate()
                return -1
            }

            val name = values?.getAsString(DavCursor.FILE_NAME) ?: ""
            if(name.isNotEmpty()) {
                when(uri) {
                    FILES_URI -> {
                        val data = values?.getAsByteArray(DavCursor.FILE_CONTENT)
                        if(data != null) {
                            val stream = ByteArrayInputStream(data)
                            this.webdav?.uploadFile(name, stream)
                            stream.close()
                        }
                        return 1
                    }
                    DIRECTORY_URI -> {
                        this.webdav?.createFolder(name)
                        return 1
                    }
                }
            }
        } catch (_: Exception) {}
        return -1
    }
}