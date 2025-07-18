package com.alternadv.vedhelper.ui.screen.rois

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.OisSource
import com.alternadv.vedhelper.model.OisModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoisState(
    val searchTerm: String = "",
    val isLoading: Boolean = false,
    val isShowHint: Boolean = true,
    val isShowNotFound: Boolean = false,
    val oisText: String? = null,
    val items: List<OisModel> = emptyList()
)

class RoisViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RoisState())
    val uiState: StateFlow<RoisState> = _uiState

    private var debounceJob: Job? = null

    fun onSearchInput(input: String) {
        _uiState.update { it.copy(searchTerm = input) }

        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(500)
            fetchResults(input)
        }
    }

    private fun fetchResults(text: String) {
        if (text.isBlank() || text.length < 3) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isShowHint = true,
                    isShowNotFound = false,
                    oisText = null,
                    items = emptyList()
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                isShowHint = false,
                isShowNotFound = false
            )
        }

        viewModelScope.launch {
            try {
                val response = OisSource.get(text)
                val data = response?.ois_list.orEmpty()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        oisText = response?.ois_text,
                        items = data,
                        isShowNotFound = data.isEmpty()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isShowNotFound = true,
                        items = emptyList()
                    )
                }
            }
        }
    }
}
