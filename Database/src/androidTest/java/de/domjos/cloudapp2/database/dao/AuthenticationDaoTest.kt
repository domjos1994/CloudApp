/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.BaseTest
import de.domjos.cloudapp2.database.model.Authentication

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Class to test Getting, Creating, Updating and Deleting authentication
 * @see de.domjos.cloudapp2.database.model.Authentication
 * @author Dominic Joas
 */
@RunWith(AndroidJUnit4::class)
class AuthenticationDaoTest : BaseTest() {
    private lateinit var authenticationDAO: AuthenticationDAO
    private lateinit var authentication: Authentication

    /**
     * Initialize DAO and object
     */
    @Before
    fun before() {
        super.init()
        authenticationDAO = super.db.authenticationDao()
        authentication = Authentication(
            0,
            "test",
            "https://test.de",
            "test",
            "test",
            false,
            null,
            null
        )
    }

    /**
     * Test getting, creating and deleting Authentication
     */
    @Test
    fun testCreateAndDeletingAuthentication() {
        // check items are empty
        var items = authenticationDAO.getAllWithoutFlow()
        assertEquals(0, items.size)

        // insert object
        val id = authenticationDAO.insertAuthentication(authentication)
        assertNotEquals(0, id)

        // items contains object
        items = authenticationDAO.getAllWithoutFlow()
        assertEquals(1, items.size)

        // delete object
        authentication.id = id
        authenticationDAO.deleteAuthentication(authentication)

        // check items are empty
        items = authenticationDAO.getAllWithoutFlow()
        assertEquals(0, items.size)
    }

    /**
     * Test getting, updating and deleting Authentication
     */
    @Test
    fun testCreateAndUpdatingAuthentication() {
        // check items are empty
        var items = authenticationDAO.getAllWithoutFlow()
        assertEquals(0, items.size)

        // insert object
        val id = authenticationDAO.insertAuthentication(authentication)
        assertNotEquals(0, id)

        // get object
        var item = authenticationDAO.getItemByTitle("test")
        assertNotNull(item)

        // update object
        item?.title = "test2"
        authenticationDAO.updateAuthentication(item!!)

        // get updated object
        item = authenticationDAO.getItemByTitle("test2")
        assertNotNull(item)

        // delete object
        authenticationDAO.deleteAuthentication(item!!)

        // check items are empty
        items = authenticationDAO.getAllWithoutFlow()
        assertEquals(0, items.size)
    }

    /**
     * Test selecting and deselecting class
     */
    @Test
    fun testSelectingAndDeselectingAuthentication() {

        // get data
        val items = authenticationDAO.getAllWithoutFlow()
        assertEquals(0, items.count())

        // insert selected object
        authentication.selected = true
        val id = authenticationDAO.insertAuthentication(authentication)
        authentication.id = id
        assertNotEquals(0, id)

        // check selected item
        var auth = authenticationDAO.getSelectedItem()
        assertNotNull(auth)

        // unselect object
        authentication.selected = false
        authenticationDAO.updateAuthentication(authentication)

        // check selected item
        auth = authenticationDAO.getSelectedItem()
        assertNull(auth)
    }
}