package de.domjos.cloudapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp.database.model.Authentication
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthenticationDAO {

    @Query("SELECT * FROM authentications")
    fun getAll(): Flow<List<Authentication>>

    @Query("SELECT * FROM authentications WHERE title=:title")
    fun getItemByTitle(title: String): Authentication?

    @Query("SELECT * FROM authentications WHERE selected=1")
    fun getSelectedItem(): Authentication?

    @Query("UPDATE authentications SET selected=0")
    fun unCheck()

    @Insert
    fun insertAuthentication(authentication: Authentication): Long

    @Update
    fun updateAuthentication(authentication: Authentication)

    @Delete
    fun deleteAuthentication(authentication: Authentication)
}