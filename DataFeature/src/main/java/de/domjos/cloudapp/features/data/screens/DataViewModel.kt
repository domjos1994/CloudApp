package de.domjos.cloudapp.features.data.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.DataRepository
import de.domjos.cloudapp.webdav.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class DataViewModel @Inject constructor(
    private val dataRepository: DataRepository
) : ViewModel() {
    var uiState: StateFlow<DataUiState> = dataRepository.list()
        .map<List<Item>, DataUiState> { DataUiState.Success(data = it)}
        .distinctUntilChanged()
        .catch { emit(DataUiState.Error(it)) }
        .stateIn(viewModelScope.plus(Dispatchers.IO), SharingStarted.WhileSubscribed(5000), DataUiState.Loading)

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.init()
            dataRepository.list()
        }
    }

    fun openFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.openFolder(item)
            dataRepository.list()
        }
    }

    fun back() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.back()
            dataRepository.list()
        }
    }

    fun loadFile(item: Item, path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.openResource(item, path)
        }
    }
}

sealed interface DataUiState {
    data object Loading : DataUiState
    data class Error(val throwable: Throwable) : DataUiState
    data class Success(val data: List<Item>) : DataUiState
}