package de.domjos.cloudapp2.services

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.content.Context
import android.os.Bundle
import de.domjos.cloudapp2.adapter.getOrCreateSyncAccount
import de.domjos.cloudapp2.database.DB


class Authenticator(private val context: Context?) : AbstractAccountAuthenticator(context) {
    private val accountManager: AccountManager?
    private val db: DB?

    init {
        if(context == null) {
            this.accountManager = null
            this.db = null
        } else {
            this.accountManager = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
            this.db = DB.newInstance(context)
        }

    }

    override fun editProperties(
        response: AccountAuthenticatorResponse,
        accountType: String
    ): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String,
        requiredFeatures: Array<String>,
        options: Bundle
    ): Bundle? {
        if(context != null) {
            val selected = this.db?.authenticationDao()?.getSelectedItem()
            if(selected != null) {
                getOrCreateSyncAccount(this.context, selected)
                return options
            } else {
                return null
            }
        } else {
            return null
        }
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        options: Bundle
    ): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle
    ): Bundle? {
        return null
    }

    override fun getAuthTokenLabel(authTokenType: String): String? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle
    ): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(
        response: AccountAuthenticatorResponse,
        account: Account,
        features: Array<String>
    ): Bundle? {
        return null
    }

    override fun getAccountRemovalAllowed(
        response: AccountAuthenticatorResponse,
        account: Account
    ): Bundle {
        val result = Bundle()
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
        return result
    }
}