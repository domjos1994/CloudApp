package de.domjos.cloudapp.data.repository

import android.content.Context
import de.domjos.cloudapp.data.R
import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.model.Authentication
import de.domjos.cloudapp.webrtc.model.user.User
import de.domjos.cloudapp.webrtc.requests.UserRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuthenticationRepository {
    val authentications: Flow<List<Authentication>>

    suspend fun check(authentication: Authentication)
    suspend fun insert(authentication: Authentication, context: Context): String
    suspend fun update(authentication: Authentication, context: Context): String
    suspend fun delete(authentication: Authentication): String

    suspend fun getLoggedInUser(): Authentication?
    suspend fun checkConnection(authentication: Authentication): User?
}

class DefaultAuthenticationRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : AuthenticationRepository {

    override var authentications: Flow<List<Authentication>> =
        authenticationDAO.getAll()

    override suspend fun check(authentication: Authentication) {
        authenticationDAO.unCheck()
        authentication.selected = true
        authenticationDAO.updateAuthentication(authentication)
        authentications = authenticationDAO.getAll()
    }

    override suspend fun insert(authentication: Authentication, context: Context): String {
        val auth = this.authenticationDAO.getItemByTitle(authentication.title)
        if(auth == null) {
            this.authenticationDAO.insertAuthentication(authentication)
        } else {
            return context.getString(R.string.validate_auth)
        }
        return ""
    }

    override suspend fun update(authentication: Authentication, context: Context): String {
        val auth = this.authenticationDAO.getItemByTitle(authentication.title)
        if(auth == null) {
            this.authenticationDAO.updateAuthentication(authentication)
        } else {
            return context.getString(R.string.validate_auth)
        }


        return ""
    }

    override suspend fun getLoggedInUser(): Authentication? {
        return this.authenticationDAO.getSelectedItem()
    }

    override suspend fun checkConnection(authentication: Authentication): User? {
        val ur = UserRequest(authentication)
        return ur.checkConnection()
    }

    override suspend fun delete(authentication: Authentication): String {
        this.authenticationDAO.deleteAuthentication(authentication)

        return ""
    }

}