package de.domjos.cloudapp.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp.database.model.Authentication

@Dao
interface AuthenticationDAO {

    @Query("SELECT * FROM authentications")
    fun getAll(): List<Authentication>

    @Query("SELECT * FROM authentications WHERE title=:title")
    fun getItemByTitle(title: String): Authentication

    @Insert
    fun insertAuthentication(authentication: Authentication): Long

    @Update
    fun updateAuthentication(authentication: Authentication)

    @Delete
    fun deleteAuthentication(authentication: Authentication)
}