package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.Money
import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.repository.ProductSearchResult
import com.vanvault.android.domain.usecase.SearchProductsUseCase
import com.vanvault.android.domain.usecase.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla EditProduct.
 *
 * DI: Hilt provee [SearchProductsUseCase] y [UpdateProductUseCase].
 */
@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val searchProducts: SearchProductsUseCase,
    private val updateProduct: UpdateProductUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProductUiState())
    val uiState: StateFlow<EditProductUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<EditProductEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<EditProductEffect> = _effects.asSharedFlow()

    fun onQueryChange(q: String) = _uiState.update { it.copy(searchQuery = q) }

    fun onSearch() {
        val q = _uiState.value.searchQuery.trim()
        if (q.isBlank()) return
        _uiState.update { it.copy(isSearching = true) }
        viewModelScope.launch {
            when (val res = searchProducts(q)) {
                is ProductSearchResult.Success -> {
                    val products = res.products
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            searchResults = products
                        )
                    }
                    when {
                        products.isEmpty() -> _effects.tryEmit(EditProductEffect.NotFound)
                        products.size == 1 -> selectProduct(products.first())
                        else -> Unit
                    }
                }
                ProductSearchResult.Empty -> {
                    _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
                    _effects.tryEmit(EditProductEffect.NotFound)
                }
                is ProductSearchResult.Error -> {
                    _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
                    _effects.tryEmit(EditProductEffect.Error(res.message))
                }
            }
        }
    }

    fun onSelectFromList(product: Product) = selectProduct(product)

    private fun selectProduct(product: Product) {
        _uiState.update {
            it.copy(
                currentProductId = product.id,
                name = product.name,
                modelOrBarcode = product.modelOrBarcode,
                quantity = product.quantity.toString(),
                unitPrice = (product.unitPrice.cents / 100).toString(),
                searchResults = emptyList()
            )
        }
    }

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onModelChange(v: String) = _uiState.update { it.copy(modelOrBarcode = v) }
    fun onQuantityChange(v: String) = _uiState.update { it.copy(quantity = v) }
    fun onPriceChange(v: String) = _uiState.update { it.copy(unitPrice = v) }

    fun onSave() {
        val s = _uiState.value
        val productId = s.currentProductId ?: return

        if (s.name.isBlank() || s.modelOrBarcode.isBlank() ||
            s.quantity.isBlank() || s.unitPrice.isBlank()
        ) {
            _effects.tryEmit(EditProductEffect.Error("Por favor llena todos los campos"))
            return
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val product = Product(
                id = productId,
                name = s.name.trim(),
                modelOrBarcode = s.modelOrBarcode.trim(),
                quantity = s.quantity.toIntOrNull() ?: 0,
                unitPrice = Money.fromDouble(s.unitPrice.toDoubleOrNull() ?: 0.0)
            )
            val result = updateProduct(product)
            _uiState.update { it.copy(isSaving = false) }
            result
                .onSuccess { _effects.tryEmit(EditProductEffect.Saved) }
                .onFailure { _effects.tryEmit(EditProductEffect.Error(it.message ?: "Error al actualizar")) }
        }
    }

    sealed interface EditProductEffect {
        data object Saved : EditProductEffect
        data object NotFound : EditProductEffect
        data class Error(val message: String) : EditProductEffect
    }
}

data class EditProductUiState(
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false,
    val currentProductId: String? = null,
    val name: String = "",
    val modelOrBarcode: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val isSaving: Boolean = false
) {
    val isEditing: Boolean get() = currentProductId != null
    val canSubmit: Boolean
        get() = isEditing &&
                name.isNotBlank() &&
                modelOrBarcode.isNotBlank() &&
                quantity.isNotBlank() &&
                unitPrice.isNotBlank() &&
                !isSaving
}