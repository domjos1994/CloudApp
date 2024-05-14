package de.domjos.cloudapp2.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before

open class BaseTest {
    protected lateinit var context: Context
    protected lateinit var db: DB

    @Before
    fun init() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, DB::class.java).build()
    }

    @After
    fun after() {
        this.db.close()
    }
}