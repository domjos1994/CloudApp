/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.providers

import android.content.ContentResolver
import android.database.CharArrayBuffer
import android.database.ContentObserver
import android.database.Cursor
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import de.domjos.cloudapp2.webdav.WebDav
import de.domjos.cloudapp2.webdav.model.Item

class DavCursor(private val webDav: WebDav, private val items: List<Item>) : Cursor {
    private var lst = mutableListOf<Item>()
    private var position = 0

    companion object {
        const val FILE_NAME = "name"
        const val FILE_TYPE = "type"
        const val FILE_PATH = "path"
        const val FILE_DIRECTORY = "directory"
        const val FILE_CONTENT = "content"

        const val FILE_NAME_INDEX = 0
        const val FILE_TYPE_INDEX = 1
        const val FILE_PATH_INDEX = 2
        const val FILE_DIRECTORY_INDEX = 3
        const val FILE_CONTENT_INDEX = 4
    }

    init {
        this.lst = items.toMutableList()
        this.position = 0
    }

    override fun close() {
        lst.clear()
        position = 0
    }

    override fun getCount(): Int {
        return lst.size
    }

    override fun getPosition(): Int {
        return position
    }

    override fun move(offset: Int): Boolean {
        this.position += offset
        return lst.size > this.position
    }

    override fun moveToPosition(position: Int): Boolean {
        this.position = position
        return lst.size > this.position
    }

    override fun moveToFirst(): Boolean {
        if(this.lst.isEmpty()) {
            return false
        } else {
            this.position = 0
            return true
        }
    }

    override fun moveToLast(): Boolean {
        if(this.lst.isEmpty()) {
            return false
        } else {
            this.position = this.lst.size - 1
            return true
        }
    }

    override fun moveToNext(): Boolean {
        if(this.lst.size <= this.position + 1) {
            return false
        } else {
            this.position += 1
            return true
        }
    }

    override fun moveToPrevious(): Boolean {
        if(this.position == 0) {
            return false
        } else {
            this.position -= 1
            return true
        }
    }

    override fun isFirst(): Boolean {
        return this.position == 0
    }

    override fun isLast(): Boolean {
        return this.position == this.lst.size - 1
    }

    override fun isBeforeFirst(): Boolean {
        return false
    }

    override fun isAfterLast(): Boolean {
        return false
    }

    override fun getColumnIndex(columnName: String?): Int {
        return when(columnName?.lowercase() ?: "") {
            FILE_NAME.lowercase() -> FILE_NAME_INDEX
            FILE_TYPE.lowercase() -> FILE_TYPE_INDEX
            FILE_PATH.lowercase() -> FILE_PATH_INDEX
            FILE_DIRECTORY.lowercase() -> FILE_DIRECTORY_INDEX
            FILE_CONTENT.lowercase() -> FILE_CONTENT_INDEX
            else -> FILE_NAME_INDEX
        }
    }

    override fun getColumnIndexOrThrow(columnName: String?): Int {
        return when(columnName?.lowercase() ?: "") {
            FILE_NAME.lowercase() -> FILE_NAME_INDEX
            FILE_TYPE.lowercase() -> FILE_TYPE_INDEX
            FILE_PATH.lowercase() -> FILE_PATH_INDEX
            FILE_DIRECTORY.lowercase() -> FILE_DIRECTORY_INDEX
            FILE_CONTENT.lowercase() -> FILE_CONTENT_INDEX
            else -> throw Exception("Index not found!")
        }
    }

    override fun getColumnName(columnIndex: Int): String {
        return when(columnIndex) {
            FILE_NAME_INDEX -> FILE_NAME
            FILE_TYPE_INDEX -> FILE_TYPE
            FILE_PATH_INDEX -> FILE_PATH
            FILE_DIRECTORY_INDEX -> FILE_DIRECTORY
            FILE_CONTENT_INDEX -> FILE_CONTENT
            else -> FILE_NAME
        }
    }

    override fun getColumnNames(): Array<String> {
        return arrayOf(FILE_NAME, FILE_TYPE, FILE_PATH, FILE_DIRECTORY)
    }

    override fun getColumnCount(): Int {
        return 5
    }

    override fun getBlob(columnIndex: Int): ByteArray {
        try {
            if(columnIndex == FILE_CONTENT_INDEX) {
                val item = this.lst[this.position]
                return this.webDav.openResource(item)
            }
        } catch (_: Exception) {}
        return ByteArray(0)
    }

    override fun getString(columnIndex: Int): String {
        return  try {
            when(columnIndex) {
                FILE_NAME_INDEX -> this.lst[this.position].name
                FILE_TYPE_INDEX -> this.lst[this.position].type
                FILE_PATH_INDEX -> this.lst[this.position].path
                else -> ""
            }
        } catch (_: Exception) {""}
    }

    override fun copyStringToBuffer(columnIndex: Int, buffer: CharArrayBuffer?) {}

    override fun getShort(columnIndex: Int): Short {
        return try {
            if(columnIndex == FILE_DIRECTORY_INDEX) {
                return if(this.lst[this.position].directory) {
                    1
                } else {
                    0
                }
            }
            -1
        } catch (_: Exception) {-1}
    }

    override fun getInt(columnIndex: Int): Int {
        return try {
            if(columnIndex == FILE_DIRECTORY_INDEX) {
                return if(this.lst[this.position].directory) {
                    1
                } else {
                    0
                }
            }
            -1
        } catch (_: Exception) {-1}
    }

    override fun getLong(columnIndex: Int): Long {
        return try {
            if(columnIndex == FILE_DIRECTORY_INDEX) {
                return if(this.lst[this.position].directory) {
                    1
                } else {
                    0
                }
            }
            -1
        } catch (_: Exception) {-1}
    }

    override fun getFloat(columnIndex: Int): Float {
        return try {
            if(columnIndex == FILE_DIRECTORY_INDEX) {
                return if(this.lst[this.position].directory) {
                    1f
                } else {
                    0f
                }
            }
            -1f
        } catch (_: Exception) {-1f}
    }

    override fun getDouble(columnIndex: Int): Double {
        return try {
            if(columnIndex == FILE_DIRECTORY_INDEX) {
                return if(this.lst[this.position].directory) {
                    1.0
                } else {
                    0.0
                }
            }
            -1.0
        } catch (_: Exception) {-1.0}
    }

    override fun getType(columnIndex: Int): Int {
        return when(columnIndex) {
            FILE_NAME_INDEX -> 1
            FILE_TYPE_INDEX -> 1
            FILE_PATH_INDEX -> 1
            FILE_DIRECTORY_INDEX -> 2
            FILE_CONTENT_INDEX -> 3
            else -> -1
        }
    }

    override fun isNull(columnIndex: Int): Boolean {
        return try {
            if(columnIndex == FILE_DIRECTORY_INDEX) {
                return if(this.lst[this.position].directory) {
                    true
                } else {
                    false
                }
            }
            false
        } catch (_: Exception) {false}
    }

    @Deprecated("Deprecated in Java")
    override fun deactivate() {
        this.close()
    }

    @Deprecated("Deprecated in Java")
    override fun requery(): Boolean {
        this.webDav.reload()
        this.lst = this.webDav.getList().toMutableList()
        return true
    }

    override fun isClosed(): Boolean {
        return lst.isEmpty()
    }

    override fun registerContentObserver(observer: ContentObserver?) {}
    override fun unregisterContentObserver(observer: ContentObserver?) {}
    override fun registerDataSetObserver(observer: DataSetObserver?) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}
    override fun setNotificationUri(cr: ContentResolver?, uri: Uri?) {}

    override fun getNotificationUri(): Uri {
        return Uri.parse("content://de.dojodev.file_provider/notifications")
    }

    override fun getWantsAllOnMoveCalls(): Boolean {
        return true
    }

    override fun setExtras(extras: Bundle?) {}

    override fun getExtras(): Bundle {
        return Bundle.EMPTY
    }

    override fun respond(extras: Bundle?): Bundle {
        return Bundle.EMPTY
    }
}