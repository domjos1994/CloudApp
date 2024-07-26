package de.domjos.cloudapp2.webdav

import android.Manifest
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import de.domjos.cloudapp2.database.model.Authentication

import org.junit.runner.RunWith

import org.junit.BeforeClass
import org.junit.Rule
import java.util.Properties
import de.domjos.cloudapp2.webdav.test.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class WebDavTest {

    @Rule
    @JvmField
    var runtimePermission: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.INTERNET)

    companion object {
        protected var context: Context? = null
        @JvmStatic
        protected var authentication: Authentication? = null
        @JvmStatic
        protected var props: Properties? = null

        /**
         * Read connection
         * and initialize authentication
         */
        @JvmStatic
        @BeforeClass
        fun before() {
            this.context = InstrumentationRegistry.getInstrumentation().targetContext
            val stream = this.context!!.resources.openRawResource(R.raw.example)
            props = Properties()
            props?.load(stream)
            stream.close()

            authentication = Authentication(
                1L, "N28", props!!["url"].toString(),
                props!!["user"].toString(), props!!["pwd"].toString(),
                true, "", null
            )
        }
    }

    @Test
    fun testConnect() {

        // check authentication
        assertNotNull(authentication)
        val webdav = WebDav(authentication!!)

        // connect
        webdav.checkUser()
        assertNotEquals(0, webdav.getList().size)
    }

    @Test
    fun testConnectAndGoForwardAndBackward() {
        val webdav = WebDav(authentication!!)

        // get items
        val items = webdav.getList()
        assertNotEquals(0, items.size)
        val basePath = webdav.getPath()

        // go to first directory
        val directory = items.filter { item -> item.directory }[0]
        webdav.openFolder(directory)
        assertNotEquals(items.size, webdav.getList().size)
        assertNotEquals(basePath, webdav.getPath())

        // go back
        webdav.back()
        assertEquals(basePath, webdav.getPath())
        assertEquals(items.size, webdav.getList().size)
    }
}