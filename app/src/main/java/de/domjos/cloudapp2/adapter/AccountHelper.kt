/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.adapter

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.services.AuthenticatorService

private const val id = "de.domjos.cloudapp2.account"

private fun createSyncAccount(context: Context, authentication: Authentication): Account {
    val account = AuthenticatorService.getAccount(id, authentication)
    val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    accountManager.addAccountExplicitly(account, null, null)
    return account
}

fun getOrCreateSyncAccount(context: Context, authentication: Authentication): Account {
    val accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    var account = accountManager.getAccountsByType("$id.${authentication.id}")
    return if(account.size != 0) {
        account[0]
    } else {
        createSyncAccount(context, authentication)
    }
}

fun updateSyncAccount(activity: Activity, authentication: Authentication) {
    val accountManager = activity.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    var accounts = accountManager.getAccountsByType("$id.${authentication.id}")
    accounts.forEach { account ->
        accountManager.renameAccount(account, authentication.title, {}, null)
    }
}

fun deleteSyncAccount(activity: Activity, authentication: Authentication) {
    val accountManager = activity.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
    var accounts = accountManager.getAccountsByType("$id.${authentication.id}")
    accounts.forEach { account ->
        accountManager.removeAccount(account, activity, {}, null)
    }
}