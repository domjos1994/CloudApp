/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import de.domjos.cloudapp2.R
import de.domjos.cloudapp2.database.DB
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.database.model.Log
import java.util.Date

private const val id = "de.domjos.cloudapp2.account"
private const val key = "id"

fun getOrCreateSyncAccount(context: Context, authentication: Authentication): Account {
    removeOldData(context)
    return getAccount(context, authentication) ?: createSyncAccount(context, authentication)
}

fun deleteSyncAccount(context: Context, authentication: Authentication) {
    val account = getAccount(context, authentication)
    if(account != null) {
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        accountManager.removeAccountExplicitly(account)
    }
}

fun insertLogException(db: DB, ex: Exception, type: String, msg: String = "", phone: Any? = null, app: Any? = null) {
    try {
        var message = ""
        if(msg.isNotEmpty()) {
            message = "${msg}:\n"
        }
        message += "${ex.message}:\n${ex.stackTraceToString()}"

        val log = Log(
            date = Date(),
            itemType = type,
            messageType = "error",
            message = message,
            object1 = phone?.toString() ?: "",
            object2 = app?.toString() ?: ""
        )
        db.logDao().insertItem(log)
    } catch (_: Exception) {}
}

fun insertLogMessage(db: DB, msg: String, type: String, phone: Any? = null, app: Any? = null) {
    try {
        val log = Log(
            date = Date(),
            itemType = type,
            messageType = "info",
            message = msg,
            object1 = phone?.toString() ?: "",
            object2 = app?.toString() ?: ""
        )
        db.logDao().insertItem(log)
    } catch (_: Exception) {}
}

@Suppress("UNCHECKED_CAST")
fun <T> getValue(cursor: Cursor, column: String, default: T): T? {
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
fun asSyncAdapter(uri: Uri, account: Account?): Uri {
    return if(account != null) {
        uri.buildUpon()
            .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(ContactsContract.PRIMARY_ACCOUNT_NAME, account.name)
            .appendQueryParameter(ContactsContract.PRIMARY_ACCOUNT_TYPE, account.type).build()
    } else {
        uri
    }
}

private fun createSyncAccount(context: Context, authentication: Authentication): Account {
    val account = Account(authentication.title, id)
    val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

    val bundle = Bundle()
    bundle.putString("displayName", authentication.title)
    bundle.putString(key, authentication.id.toString())
    accountManager.addAccountExplicitly(account, null, bundle)

    return account
}

private fun removeOldData(context: Context) {
    val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    val accounts = accountManager.getAccountsByType(id)
    accounts.filter {
        it.name == context.getString(R.string.app_name) ||
            accountManager.getUserData(it, key) == null }.forEach { account ->
        accountManager.removeAccountExplicitly(account)
    }
}

private fun getAccount(context: Context, authentication: Authentication): Account? {
    return try {
        val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val accounts = accountManager.getAccountsByType(id)
        return accounts.find { accountManager.getUserData(it, key) == authentication.id.toString() }
    } catch (_: Exception) { null }
}