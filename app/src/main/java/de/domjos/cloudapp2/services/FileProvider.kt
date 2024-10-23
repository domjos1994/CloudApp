/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.services

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import java.io.File

class FileProvider : DocumentsProvider() {
    private lateinit var root: File

    override fun onCreate(): Boolean {
        root = File(context!!.getExternalFilesDir(null), "sync")
        if(!root.exists()) {
            return root.mkdirs()
        }
        return true
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        val matrixCursor = MatrixCursor(projection)
        val row = matrixCursor.newRow()
        row.add(DocumentsContract.Root.COLUMN_ROOT_ID, root.path)
        row.add(DocumentsContract.Root.COLUMN_TITLE, "Sync Directory")
        row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, root.path)
        row.add(DocumentsContract.Root.COLUMN_MIME_TYPES, "*/*")
        row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES, availableBytes())
        row.add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_SUPPORTS_CREATE)
        return matrixCursor
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        val matrixCursor = MatrixCursor(projection)
        val file = File(documentId!!)

        if (file.exists()) {
            val row = matrixCursor.newRow()
            row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, file.absolutePath)
            row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.name)
            row.add(DocumentsContract.Document.COLUMN_SIZE, file.length())
            row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, getMimeType(file))
        }

        return matrixCursor
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val matrixCursor = MatrixCursor(projection)
        val files = root.listFiles() ?: arrayOf()
        for (file in files) {
            val row = matrixCursor.newRow()
            row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, file.absolutePath)
            row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.name)
            row.add(DocumentsContract.Document.COLUMN_SIZE, file.length())
            row.add(DocumentsContract.Document.COLUMN_MIME_TYPE, getMimeType(file))
        }
        return matrixCursor
    }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val path = File(documentId!!)
        if(path.parentFile?.exists() == false) {
            path.mkdirs()
        }
        if(!path.exists()) {
            path.createNewFile()
        }
        return ParcelFileDescriptor.open(path, ParcelFileDescriptor.MODE_READ_WRITE)
    }

    override fun createDocument(
        parentDocumentId: String?,
        mimeType: String?,
        displayName: String?
    ): String {
        val directory = File(root, parentDocumentId!!)
        if(!directory.parentFile!!.exists()) {
            directory.parentFile!!.mkdirs()
        }
        val file = File(directory.parentFile, displayName!!)
        if(!file.exists()) {
            file.createNewFile()
        }
        return file.path
    }

    private fun getMimeType(file: File): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(file.toString())
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }

    private fun availableBytes(): Long {
        val path = File(context!!.getExternalFilesDir(null), "sync")
        val stat = StatFs(path.path)
        return stat.availableBytes
    }
}