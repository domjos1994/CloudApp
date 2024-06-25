package de.domjos.cloudapp2.rest.requests

import androidx.test.ext.junit.runners.AndroidJUnit4
import de.domjos.cloudapp2.rest.BasicTest
import de.domjos.cloudapp2.rest.model.notes.Note
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Test getting, creating, updating, deleting notes
 * @see de.domjos.cloudapp2.rest.requests.NoteRequest
 */
@RunWith(AndroidJUnit4::class)
class NoteRequestTest : BasicTest() {
    private var noteRequest: NoteRequest? = null
    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var note = ""

    /**
     * initialize Request
     */
    @Before
    fun before() {
        noteRequest = NoteRequest(authentication)
        note = "# Test-Note\n${sdf.format(Date())}"
    }

    /**
     * Test getting, creating and deleting notes
     */
    @Test
    fun testCreatingAndDeletingNotes() {

        runBlocking {
            // add note
            noteRequest?.addNote(Note(0, note, "Test", "test", false, 0))

            // get notes
            val n = getNote()

            // delete note
            noteRequest?.deleteNote(n?.id!!)

            // check if note not exists
            noNote()
        }

    }

    /**
     * Test updating notes
     */
    @Test
    fun testUpdatingNotes() {

        runBlocking {
            // add note
            noteRequest?.addNote(Note(0, note, "Test", "test", false, 0))

            val current: Note? = getNote()

            // update
            current?.title = "Test 1"
            noteRequest?.updateNote(current!!)

            // checking if note exists
            val asyncNotes = noteRequest?.getNotes()
            assertNotNull(asyncNotes)
            asyncNotes?.collect { notes ->

                var hasNote = false
                notes.forEach {n ->
                    if(n.title == "Test 1") {
                        hasNote = true
                    }
                }
                assertTrue(hasNote)
            }

            // delete note
            noteRequest?.deleteNote(current?.id!!)

            // get notes
            noNote()
        }

    }

    private suspend fun getNote(): Note? {
        val asyncNotes = noteRequest?.getNotes()
        assertNotNull(asyncNotes)

        // check if note exists
        var current: Note? = null
        asyncNotes?.collect { notes ->

            var hasNote = false
            notes.forEach {n ->
                if(n.content == note) {
                    hasNote = true
                    current = n
                }
            }
            assertTrue(hasNote)
            assertNotNull(current)
        }
        return current
    }

    private suspend fun noNote() {
        val asyncNotes = noteRequest?.getNotes()
        assertNotNull(asyncNotes)

        // check if note exists
        asyncNotes?.collect { notes ->

            var hasNote = false
            notes.forEach {n ->
                if(n.content == note) {
                    hasNote = true
                }
            }
            assertFalse(hasNote)
        }
    }
}