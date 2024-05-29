package de.domjos.cloudapp2.data.repository

import de.domjos.cloudapp2.database.dao.AuthenticationDAO
import de.domjos.cloudapp2.rest.model.notes.Note
import de.domjos.cloudapp2.rest.requests.NoteRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface NoteRepository {
    val rooms: Flow<List<Note>>
    fun reload(): Flow<List<Note>>
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Int)
}

class DefaultNoteRepository @Inject constructor(
    private val authenticationDAO: AuthenticationDAO
) : NoteRepository {
    private val request: NoteRequest
        get() = NoteRequest(authenticationDAO.getSelectedItem())
    override val rooms: Flow<List<Note>>
        get() = reload()

    override fun reload(): Flow<List<Note>> {
        return request.getNotes()
    }

    @Throws(Exception::class)
    override suspend fun insertNote(note: Note) {
        request.addNote(note)
    }

    @Throws(Exception::class)
    override suspend fun updateNote(note: Note) {
        request.updateNote(note)
    }

    @Throws(Exception::class)
    override suspend fun deleteNote(id: Int) {
        request.deleteNote(id)
    }

}