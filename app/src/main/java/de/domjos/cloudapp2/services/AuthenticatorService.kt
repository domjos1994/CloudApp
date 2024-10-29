package de.domjos.cloudapp2.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

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
}