package de.domjos.cloudapp.data.repository

import de.domjos.cloudapp.database.dao.AuthenticationDAO
import de.domjos.cloudapp.database.model.Authentication
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuthenticationRepository {
    val authentications: Flow<List<Authentication>>

    suspend fun check(authentication: Authentication)
    suspend fun insert(authentication: Authentication): String
    suspend fun update(authentication: Authentication): String
    suspend fun delete(authentication: Authentication): String
}

class DefaultAuthenticationRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : AuthenticationRepository {

    override var authentications: Flow<List<Authentication>> =
        authenticationDAO.getAll()

    override suspend fun check(authentication: Authentication) {
        authenticationDAO.getAll().collect {
            it.forEach { item ->
                item.selected = false
                authenticationDAO.updateAuthentication(authentication)
            }
        }
        authentication.selected = true
        authenticationDAO.updateAuthentication(authentication)
    }

    override suspend fun insert(authentication: Authentication): String {
        this.authenticationDAO.insertAuthentication(authentication)

        return ""
    }

    override suspend fun update(authentication: Authentication): String {
        this.authenticationDAO.updateAuthentication(authentication)

        return ""
    }
    override suspend fun delete(authentication: Authentication): String {
        this.authenticationDAO.deleteAuthentication(authentication)

        return ""
    }

}