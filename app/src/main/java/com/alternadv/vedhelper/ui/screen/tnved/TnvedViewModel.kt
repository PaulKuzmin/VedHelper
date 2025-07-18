package com.alternadv.vedhelper.ui.screen.tnved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.TnvedSource
import com.alternadv.vedhelper.model.TnvedNodeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TnvedState(
    val nodes: List<TnvedNodeModel> = emptyList(),
    val isLoading: Boolean = false
)

class TnvedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TnvedState())
    val uiState: StateFlow<TnvedState> = _uiState

    fun loadNodes(parentId: String = "0") {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val result = TnvedSource.getNodes(parentId).orEmpty()
                _uiState.value = TnvedState(nodes = result)
            } catch (e: Exception) {
                _uiState.value = TnvedState() // fail silently for now
            }
        }
    }
}
