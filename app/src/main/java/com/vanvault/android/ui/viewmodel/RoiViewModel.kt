package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.RoiMetrics
import com.vanvault.android.domain.usecase.GetRoiMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoiViewModel @Inject constructor(
    private val getRoiMetrics: GetRoiMetricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoiUiState())
    val uiState: StateFlow<RoiUiState> = _uiState.asStateFlow()

    init { loadRoi() }

    fun loadRoi() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val metrics = getRoiMetrics()
            _uiState.update { it.copy(isLoading = false, metrics = metrics) }
        }
    }
}

data class RoiUiState(
    val isLoading: Boolean = true,
    val metrics: RoiMetrics? = null
)