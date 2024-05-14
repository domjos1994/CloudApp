package de.domjos.cloudapp2.database.dao

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.database.BaseTest
import de.domjos.cloudapp2.database.model.Authentication
import kotlinx.coroutines.flow.toList

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class AuthenticationDaoTest : BaseTest() {
    private lateinit var authenticationDAO: AuthenticationDAO

    @Before
    fun initClass() {
        authenticationDAO = super.db.authenticationDao()
    }

    @Test
    suspend fun testCreation() {
        val auth = Authentication(0, "test", "https://test.de", "test", "test",false,null, null)

        var items = authenticationDAO.getAll().toList()
        assertEquals(0, items.size)

        val id = authenticationDAO.insertAuthentication(auth)
        assertNotEquals(0, id)

        items = authenticationDAO.getAll().toList()
        assertEquals(1, items.size)

        auth.id = id
        authenticationDAO.deleteAuthentication(auth)
        items = authenticationDAO.getAll().toList()
        assertEquals(0, items.size)
    }

    @Test
    suspend fun testUpdate() {
        val auth = Authentication(0, "test", "https://test.de", "test", "test",false,null, null)

        var items = authenticationDAO.getAll().toList()
        assertEquals(0, items.size)

        val id = authenticationDAO.insertAuthentication(auth)
        assertNotEquals(0, id)

        var item = authenticationDAO.getItemByTitle("test")
        assertNotNull(item)
        item?.title = "test2"
        authenticationDAO.updateAuthentication(item!!)
        item = authenticationDAO.getItemByTitle("test2")
        assertNotNull(item)

        authenticationDAO.deleteAuthentication(item!!)
        items = authenticationDAO.getAll().toList()
        assertEquals(0, items.size)
    }
}