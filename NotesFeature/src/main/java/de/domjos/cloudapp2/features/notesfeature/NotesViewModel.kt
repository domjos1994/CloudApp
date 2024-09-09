package de.domjos.cloudapp2.features.notesfeature

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp2.appbasics.helper.ConnectivityViewModel
import de.domjos.cloudapp2.data.repository.NoteRepository
import de.domjos.cloudapp2.rest.model.notes.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
): ConnectivityViewModel() {
    private val _notes = MutableStateFlow(listOf<Note>())
    val notes: StateFlow<List<Note>> get() = _notes

    override fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(super.isConnected()) {
                    load()
                }
            } catch (ex: Exception) {
                super.printException(ex, this)
            }
        }
    }

    fun save(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(note.id == 0) {
                    noteRepository.insertNote(note)
                } else {
                    noteRepository.updateNote(note)
                }
                load()
            } catch (ex: Exception) {
                super.printException(ex, this)
            }
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                noteRepository.deleteNote(note.id)
                load()
            } catch (ex: Exception) {
                super.printException(ex, this)
            }
        }
    }

    fun hasAuthentications(): Boolean {
        try {
            return noteRepository.hasAuthentications()
        } catch (ex: Exception) {
            super.printException(ex, this)
        }
        return false
    }

    private suspend fun load() {
        noteRepository.reload().collect {notes ->
            val tmp = mutableListOf<Note>()
            val favorites = mutableListOf<Note>()
            notes.forEach { note ->
                if(note.favorite) {
                    favorites.add(note)
                } else {
                    tmp.add(note)
                }
            }

            favorites.addAll(tmp)
            _notes.value = favorites
        }
    }
}