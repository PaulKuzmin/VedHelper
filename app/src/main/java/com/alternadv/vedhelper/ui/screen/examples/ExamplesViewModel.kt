package com.alternadv.vedhelper.ui.screen.examples

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.ExamplesSource
import com.alternadv.vedhelper.model.ExampleItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ExamplesState(
    val searchTerm: String = "",
    val items: List<ExampleItem> = emptyList(),
    val isShowHint: Boolean = true,
    val isShowNotFound: Boolean = false,
    val isLoading: Boolean = false
)

class ExamplesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ExamplesState())
    val uiState: StateFlow<ExamplesState> = _uiState

    private var debounceJob: Job? = null

    fun onSearchInput(input: String) {
        _uiState.update { it.copy(searchTerm = input) }

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(500)
            fetchExamples(input)
        }
    }

    private fun fetchExamples(text: String) {
        if (text.isBlank() || text.length < 3) {
            _uiState.update {
                it.copy(
                    items = emptyList(),
                    isShowHint = true,
                    isShowNotFound = false,
                    isLoading = false
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, isShowHint = false, isShowNotFound = false) }

        viewModelScope.launch {
            try {
                val response = ExamplesSource.get(text)
                val data = response?.data?.data.orEmpty()
                _uiState.update {
                    it.copy(
                        items = data,
                        isShowNotFound = data.isEmpty(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        items = emptyList(),
                        isShowNotFound = true,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun initIfNeeded() {
        if (_uiState.value.items.isEmpty() && _uiState.value.searchTerm.isNotBlank()) {
            fetchExamples(_uiState.value.searchTerm)
        }
    }
}
