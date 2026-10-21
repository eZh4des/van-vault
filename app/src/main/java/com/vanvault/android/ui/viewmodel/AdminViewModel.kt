package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.AdminSnapshot
import com.vanvault.android.domain.usecase.GetAdminSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getAdminSnapshot: GetAdminSnapshotUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init { loadAdmin() }

    fun loadAdmin() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val snapshot = getAdminSnapshot()
            _uiState.update { it.copy(isLoading = false, snapshot = snapshot) }
        }
    }
}

data class AdminUiState(
    val isLoading: Boolean = true,
    val snapshot: AdminSnapshot? = null
)