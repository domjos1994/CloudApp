/*
 * Copyright (c) 2024 Dominic Joas
 * This file is part of the CloudApp-Project and licensed under the
 * General Public License V3.
 */

package de.domjos.cloudapp2.rest

import android.Manifest
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.test.R
import org.junit.BeforeClass
import org.junit.Rule
import java.util.Properties

/**
 * Basic-Class to test REST-Classes
 * @author Dominic Joas
 */
open class BasicTest {

    // get rule to get internet permission
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
}