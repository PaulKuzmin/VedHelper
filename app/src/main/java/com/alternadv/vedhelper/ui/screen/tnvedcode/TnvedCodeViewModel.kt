package com.alternadv.vedhelper.ui.screen.tnvedcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.TnvedSource
import com.alternadv.vedhelper.model.TnvedCodeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TnvedCodeState(
    val isLoading: Boolean = false,
    val codeData: TnvedCodeModel? = null,
    val errorMessage: String? = null
)

class TnvedCodeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TnvedCodeState())
    val uiState: StateFlow<TnvedCodeState> = _uiState

    fun loadCode(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = TnvedSource.getCode(code)
                _uiState.value = TnvedCodeState(isLoading = false, codeData = result)
            } catch (e: Exception) {
                _uiState.value = TnvedCodeState(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Ошибка загрузки"
                )
            }
        }
    }
}
