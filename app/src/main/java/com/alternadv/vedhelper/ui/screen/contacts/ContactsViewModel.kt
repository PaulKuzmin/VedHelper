package com.alternadv.vedhelper.ui.screen.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alternadv.vedhelper.datasource.ContactsSource
import com.alternadv.vedhelper.model.ContactsModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ContactsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val contacts: List<ContactsModel> = emptyList()
)

class ContactsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ContactsUiState(isLoading = true))
    val uiState: StateFlow<ContactsUiState> = _uiState

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = ContactsSource.get()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contacts = result ?: emptyList()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }
}