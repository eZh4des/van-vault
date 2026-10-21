package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.DashboardSnapshot
import com.vanvault.android.domain.usecase.GetDashboardSnapshotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getSnapshot: GetDashboardSnapshotUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        onRefresh()
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            val snapshot = getSnapshot()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    snapshot = snapshot
                )
            }
        }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val snapshot: DashboardSnapshot? = null
)