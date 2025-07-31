package com.alternadv.vedhelper.ui.screen.tnved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.TnvedSource
import com.alternadv.vedhelper.model.TnvedNodeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TnvedPathItem(val id: String, val name: String)

data class TnvedState(
    val nodes: List<TnvedNodeModel> = emptyList(),
    val path: List<TnvedPathItem> = listOf(TnvedPathItem("0", "Разделы")),
    val isLoading: Boolean = false
)

class TnvedViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TnvedState())
    val uiState: StateFlow<TnvedState> = _uiState

    fun loadNodes(parentId: String = "0", displayName: String? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val result = TnvedSource.getNodes(parentId).orEmpty()

                val newPath = when {
                    parentId == "0" -> listOf(TnvedPathItem("0", "Разделы"))
                    displayName != null -> _uiState.value.path + TnvedPathItem(parentId, displayName)
                    else -> _uiState.value.path
                }

                _uiState.value = _uiState.value.copy(
                    nodes = result,
                    path = newPath,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(nodes = emptyList(), isLoading = false)
            }
        }
    }

    fun navigateToPath(index: Int) {
        val item = _uiState.value.path[index]
        val newPath = _uiState.value.path.take(index + 1)
        _uiState.value = _uiState.value.copy(path = newPath)
        loadNodes(item.id)
    }

    fun initIfNeeded() {
        if (_uiState.value.nodes.isEmpty()) {
            loadNodes()
        }
    }
}