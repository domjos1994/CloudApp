package de.domjos.cloudapp2.services

import android.accounts.Account
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import de.domjos.cloudapp2.R
import de.domjos.cloudapp2.database.model.Authentication

class AuthenticatorService : Service() {
    private var mAuthenticator: Authenticator? = null
    override fun onCreate() {
        mAuthenticator = Authenticator(this)
    }

    /** When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    override fun onBind(intent: Intent?): IBinder {
        return mAuthenticator?.iBinder!!
    }

    companion object {
        fun getAccount(type: String?, authentication: Authentication): Account {
            return Account(authentication.title, "$type.${authentication.id}")
        }
    }
}