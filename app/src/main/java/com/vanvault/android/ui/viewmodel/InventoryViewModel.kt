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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    /**
     * Eventos one-shot (Snackbar, navegación, dialogs). No persisten en rotación.
     * El UI los consume con `LaunchedEffect { effects.collect { ... } }`.
     */
    private val _effects = MutableSharedFlow<InventoryEffect>(extraBufferCapacity = 4)
    val effects: SharedFlow<InventoryEffect> = _effects.asSharedFlow()

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

    /**
     * Elimina un producto con posibilidad de Undo vía Snackbar.
     *
     * - Emite `Deleted(product)` para que la UI muestre un Snackbar.
     * - El timeout del Snackbar (default 4s) decide si el delete persiste.
     *
     * Nota: PRAGMA "Tracer Bullets" — emitimos primero el evento y luego
     * esperamos el resultado. Si el snackbar se cierra sin undo, el delete
     * ya está hecho. Si undo se pulsa, restauramos vía `onRestoreProduct`.
     */
    fun onDeleteWithUndo(product: Product) {
        viewModelScope.launch {
            deleteProduct(product.id)
            _effects.tryEmit(InventoryEffect.Deleted(product))
        }
    }

    /** Restaura un producto eliminado (Undo del Snackbar). */
    fun onRestoreProduct(product: Product) {
        viewModelScope.launch { addProduct(product) }
    }

    /** Elimina sin posibilidad de undo (uso legacy). */
    @Deprecated("Use onDeleteWithUndo for Nielsen #5 compliance")
    fun onDeleteProduct(id: String) {
        viewModelScope.launch {
            deleteProduct(id)
        }
    }

    fun onFilterChange(filter: StockFilter) {
        _uiState.update { it.copy(filter = filter) }
    }
}

/**
 * Eventos one-shot del VM. El UI los consume sin persistencia.
 *
 * `Deleted(product)` incluye el producto eliminado para que el UI
 * pueda mostrar el Snackbar Undo y restaurar vía [onRestoreProduct].
 */
sealed interface InventoryEffect {
    data class Deleted(val product: Product) : InventoryEffect
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
    val isRefreshing: Boolean = false,
    val filter: StockFilter = StockFilter.ALL
) {
    /**
     * Productos visibles, respetando:
     *  1. Búsqueda (si hay query activa, usa searchResults)
     *  2. Filtro de stock (si no hay búsqueda, aplica el filtro seleccionado)
     *  3. Lista completa (si no hay búsqueda NI filtro)
     */
    val visibleProducts: List<Product>
        get() = when {
            isSearchActive && searchQuery.isNotBlank() -> searchResults
            filter == StockFilter.ALL -> allProducts
            else -> allProducts.filter { it.stockStatus == filter.toStockStatus() }
        }

    val hasProducts: Boolean
        get() = allProducts.isNotEmpty()

    /** Cuenta de productos por estado (para badges en los chips). */
    fun countFor(s: StockStatus): Int = allProducts.count { it.stockStatus == s }

    fun stockOf(product: Product): StockStatus = product.stockStatus
}

/**
 * Filtros del InventoryScreen. Cada uno mapea a un [StockStatus] del dominio.
 *
 * `ALL` es neutro (no aplica filtro de stock).
 */
enum class StockFilter(val label: String) {
    AVAILABLE("Disponible"),
    LOW_STOCK("Insuficiente"),
    OUT_OF_STOCK("Agotado"),
    ALL("Todos");

    internal fun toStockStatus(): StockStatus = when (this) {
        AVAILABLE -> StockStatus.AVAILABLE
        LOW_STOCK -> StockStatus.LOW_STOCK
        OUT_OF_STOCK -> StockStatus.OUT_OF_STOCK
        ALL -> StockStatus.AVAILABLE  // no se usa cuando es ALL
    }
}