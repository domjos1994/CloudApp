package de.domjos.cloudapp.services

import android.accounts.Account
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import de.domjos.cloudapp.R

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
        fun getAccount(context: Context, type: String?): Account {
            return Account(context.getString(R.string.app_name), type)
        }
    }
}