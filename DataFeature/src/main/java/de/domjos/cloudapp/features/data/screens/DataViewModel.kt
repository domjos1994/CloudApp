package de.domjos.cloudapp.features.data.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.domjos.cloudapp.data.repository.DataRepository
import de.domjos.cloudapp.webdav.model.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val _items = MutableStateFlow(listOf<Item>())
    val items: StateFlow<List<Item>> get() = _items

    fun init() {
        viewModelScope.launch(Dispatchers.IO) {
            _items.value = dataRepository.items
        }
    }

    fun openFolder(item: Item) {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.openFolder(item)
            _items.value = dataRepository.items
        }
    }

    fun back() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.back()
            _items.value = dataRepository.items
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