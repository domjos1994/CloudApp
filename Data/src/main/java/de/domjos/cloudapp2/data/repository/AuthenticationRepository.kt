package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.capabilities.Data
import de.domjos.cloudapp2.rest.model.user.User
import de.domjos.cloudapp2.rest.requests.UserRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuthenticationRepository {
    val authentications: Flow<List<Authentication>>

    suspend fun check(authentication: Authentication)
    suspend fun insert(authentication: Authentication, msg: String): String
    suspend fun update(authentication: Authentication, msg: String): String
    suspend fun delete(authentication: Authentication): String

    suspend fun getLoggedInUser(): Authentication?
    suspend fun checkConnection(authentication: Authentication): User?
    suspend fun getCapabilities(authentication: Authentication?): Data?
    fun hasAuthentications(): Boolean
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

    override fun hasAuthentications(): Boolean {
        return authenticationDAO.selected()!=0L
    }

    override suspend fun insert(authentication: Authentication, msg: String): String {
        val auth = this.authenticationDAO.getItemByTitle(authentication.title)
        if(auth == null) {
            this.authenticationDAO.insertAuthentication(authentication)
        } else {
            return msg
        }
        return ""
    }

    override suspend fun update(authentication: Authentication, msg: String): String {
        val auth = this.authenticationDAO.getItemByTitle(authentication.title)

        return if(auth == null) {
            this.authenticationDAO.updateAuthentication(authentication)
            ""
        } else {
            if(auth.id != authentication.id) {
                msg
            } else {
                this.authenticationDAO.updateAuthentication(authentication)
                ""
            }
        }
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

    override suspend fun getCapabilities(authentication: Authentication?): Data? {
        val tmp: Authentication? = authentication ?: this.authenticationDAO.getSelectedItem()
        return if(tmp != null) {
            val ur = UserRequest(tmp)
            val cap = ur.getCapabilities()
            cap?.capabilities?.theming?.url = tmp.title
            cap
        } else {
            null
        }
    }
}