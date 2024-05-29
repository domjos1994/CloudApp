package de.domjos.cloudapp2.features.notesfeature

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
): ViewModel() {
    private val _notes = MutableStateFlow(listOf<Note>())
    val notes: StateFlow<List<Note>> get() = _notes
    val message = MutableLiveData<String>()

    fun reload() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                noteRepository.reload().collect {
                    _notes.value = it
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
                message.postValue(ex.message)
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
                noteRepository.reload().collect {
                    _notes.value = it
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
                message.postValue(ex.message)
            }
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                noteRepository.deleteNote(note.id)
                noteRepository.reload().collect {
                    _notes.value = it
                }
            } catch (ex: Exception) {
                Log.e(this.javaClass.name, ex.message, ex)
                message.postValue(ex.message)
            }
        }
    }
}