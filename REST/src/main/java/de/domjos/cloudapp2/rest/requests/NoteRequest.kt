package de.domjos.cloudapp2.rest.requests

import de.domjos.cloudapp2.database.model.Authentication
import de.domjos.cloudapp2.rest.model.notes.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlin.jvm.Throws

class NoteRequest(authentication: Authentication?) : BasicRequest(authentication, "/index.php/apps/notes/api/v1/")  {

    @Throws(Exception::class)
    fun getNotes(): Flow<List<Note>> {
        val request = super.buildRequest("notes", "GET", null)

        return flow {
            if(request!=null) {
                client.newCall(request).execute().use { response ->
                    val content = response.body!!.string()
                    val notes =  super.json.decodeFromString<List<Note>>(content)
                    emit(notes)
                }
            } else {
                emit(listOf())
            }
        }
    }

    @Throws(Exception::class)
    fun addNote(note: Note) {
        val content = super.json.encodeToString(note)
        val request = super.buildRequest("notes", "POST", content)
        this.client.newCall(request!!).execute()
    }

    @Throws(Exception::class)
    fun updateNote(note: Note) {
        val content = super.json.encodeToString(note)
        val request = super.buildRequest("notes/${note.id}", "PUT", content)
        this.client.newCall(request!!).execute()
    }

    @Throws(Exception::class)
    fun deleteNote(id: Int) {
        val request = super.buildRequest("notes/${id}", "DELETE", null)
        this.client.newCall(request!!).execute()
    }
}