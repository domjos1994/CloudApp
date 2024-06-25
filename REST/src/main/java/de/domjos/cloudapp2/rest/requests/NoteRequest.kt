package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.notes.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlin.jvm.Throws

/**
 * Getting, Creating, Updating and Deleting Notes
 * @see de.domjos.cloudapp2.rest.model.notes.Note
 * @author Dominic Joas
 * @param authentication The authentication object
 */
class NoteRequest(authentication: Authentication?) : BasicRequest(authentication, "/index.php/apps/notes/api/v1/")  {

    /**
     * Get a list of notes
     * @return flow with a list of notes or empty list
     */
    @Throws(Exception::class)
    fun getNotes(): Flow<List<Note>> {

        // build request
        val request = super.buildRequest("notes", "GET", null)

        return flow {
            if(request!=null) {

                // call
                client.newCall(request).execute().use { response ->

                    // string to object
                    val content = response.body!!.string()
                    val notes =  super.json.decodeFromString<List<Note>>(content)
                    emit(notes)
                }
            } else {
                emit(listOf())
            }
        }
    }

    /**
     * Add a new Note
     * @param note the new Note
     */
    @Throws(Exception::class)
    fun addNote(note: Note) {

        // object to string
        val content = super.json.encodeToString(note)

        // send request
        val request = super.buildRequest("notes", "POST", content)
        this.client.newCall(request!!).execute()
    }

    /**
     * Update an available Note
     * @param note the available note
     */
    @Throws(Exception::class)
    fun updateNote(note: Note) {

        // object to string
        val content = super.json.encodeToString(note)

        // send request
        val request = super.buildRequest("notes/${note.id}", "PUT", content)
        this.client.newCall(request!!).execute()
    }

    /**
     * Delete an available Note
     * @param id id of the available note
     */
    @Throws(Exception::class)
    fun deleteNote(id: Int) {

        // send request
        val request = super.buildRequest("notes/${id}", "DELETE", null)
        this.client.newCall(request!!).execute()
    }
}