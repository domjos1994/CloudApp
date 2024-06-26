/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.domjos.cloudapp2.database.model.Authentication
import kotlinx.coroutines.flow.Flow

/**
 * DAO to work with authentication
 * getting:
 * @property getAll
 * @property getAllWithoutFlow
 * @property getItemByTitle
 * @property getSelectedItem
 * @property selected
 * adding:
 * @property insertAuthentication
 * updating:
 * @property unCheck
 * @property updateAuthentication
 * deleting:
 * @property deleteAuthentication
 * @see de.domjos.cloudapp2.database.model.Authentication
 * @author Dominic Joas
 */
@Dao
interface AuthenticationDAO {

    /**
     * Get a flow with a list of Authentications
     * @return Flow with list of Authentications
     */
    @Query("SELECT * FROM authentications")
    fun getAll(): Flow<List<Authentication>>

    /**
     * Get a list of authentications
     * for testing only
     * @return list of authentications
     */
    @Query("SELECT * FROM authentications")
    fun getAllWithoutFlow(): List<Authentication>

    /**
     * Gets an item by the title of the auth
     * @param title Title of element
     * @return the Item of the title
     */
    @Query("SELECT * FROM authentications WHERE title=:title")
    fun getItemByTitle(title: String): Authentication?

    /**
     * Gets the Item which is selected
     * @return the selected item
     */
    @Query("SELECT * FROM authentications WHERE selected=1")
    fun getSelectedItem(): Authentication?

    /**
     * Unselect all items
     */
    @Query("UPDATE authentications SET selected=0")
    fun unCheck()

    /**
     * Count the number of selected items
     * @return the number of selected items
     */
    @Query("SELECT count(*) FROM authentications WHERE selected=1")
    fun selected(): Long

    /**
     * Adds a new Object
     * @param authentication the authentication object
     * @return the id
     */
    @Insert
    fun insertAuthentication(authentication: Authentication): Long

    /**
     * Updates an existing authentication
     * @param authentication the authentication
     */
    @Update
    fun updateAuthentication(authentication: Authentication)

    /**
     * Deletes an existing authentication
     * @param authentication the authentication
     */
    @Delete
    fun deleteAuthentication(authentication: Authentication)
}