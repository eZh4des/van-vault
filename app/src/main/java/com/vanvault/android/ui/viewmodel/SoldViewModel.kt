package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.SoldItem
import com.vanvault.android.domain.usecase.GetSoldItemsOnceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SoldViewModel @Inject constructor(
    private val getSoldItems: GetSoldItemsOnceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SoldUiState())
    val uiState: StateFlow<SoldUiState> = _uiState.asStateFlow()

    init { loadSold() }

    fun loadSold() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val items = getSoldItems()
            _uiState.update { it.copy(isLoading = false, items = items) }
        }
    }
}

data class SoldUiState(
    val isLoading: Boolean = true,
    val items: List<SoldItem> = emptyList()
) {
    val totalSold: Int get() = items.sumOf { it.quantitySold }
    val totalRevenue: Long get() = items.sumOf { it.total.cents }
}