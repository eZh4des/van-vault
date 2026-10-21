package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.model.StockStatus
import com.vanvault.android.domain.repository.ProductSearchResult
import com.vanvault.android.domain.usecase.AddProductUseCase
import com.vanvault.android.domain.usecase.DeleteProductUseCase
import com.vanvault.android.domain.usecase.ObserveInventoryUseCase
import com.vanvault.android.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para [com.vanvault.android.ui.screens.InventoryScreen].
 *
 * Google Architecture (UI layer):
 *  - Expone [InventoryUiState] como única fuente de verdad (UDF, SSOT).
 *  - Toda la lógica de negocio se delega a UseCases.
 *  - La UI NUNCA instancia el repositorio ni Firebase.
 *
 * Pragmatic tip: el ViewModel sobrevive a recomposiciones y configuration changes.
 *
 * DI: Hilt provee los UseCases automáticamente gracias a `@HiltViewModel` +
 * `@Inject`. No necesitamos `ViewModelProvider.Factory` manual.
 */
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val observeInventory: ObserveInventoryUseCase,
    private val searchProducts: SearchProductsUseCase,
    private val addProduct: AddProductUseCase,
    private val deleteProduct: DeleteProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    // Lista maestra observada de Firebase (SSOT)
    val inventory: StateFlow<List<Product>> = observeInventory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var searchJob: Job? = null

    init {
        inventory
            .onEach { products ->
                _uiState.update { state ->
                    if (!state.isSearchActive) state.copy(allProducts = products)
                    else state
                }
            }
            .launchIn(viewModelScope)
    }

    // ---------- Acciones UI ----------

    fun onSearchActiveChange(active: Boolean) {
        _uiState.update { it.copy(isSearchActive = active) }
        if (!active) onSearchQueryChange("")
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            _uiState.update { it.copy(isSearching = true) }
            when (val res = searchProducts(query)) {
                is ProductSearchResult.Success ->
                    _uiState.update { it.copy(searchResults = res.products, isSearching = false) }
                ProductSearchResult.Empty ->
                    _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                is ProductSearchResult.Error ->
                    _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            }
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onDeleteProduct(id: String) {
        viewModelScope.launch {
            deleteProduct(id)
        }
    }

    fun onRestoreProduct(product: Product) {
        viewModelScope.launch { addProduct(product) }
    }
}

/**
 * Estado inmutable de la pantalla (UDF).
 * La UI observa este StateFlow y se recompone cuando cambia.
 */
data class InventoryUiState(
    val allProducts: List<Product> = emptyList(),
    val searchResults: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val isRefreshing: Boolean = false
) {
    val visibleProducts: List<Product>
        get() = if (isSearchActive && searchQuery.isNotBlank()) searchResults else allProducts

    val hasProducts: Boolean
        get() = allProducts.isNotEmpty()

    fun stockOf(product: Product): StockStatus = product.stockStatus
}