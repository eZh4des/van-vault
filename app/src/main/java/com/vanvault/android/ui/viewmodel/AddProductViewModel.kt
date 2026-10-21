package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.Money
import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.usecase.AddProductUseCase
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
 * ViewModel para la pantalla AddProduct.
 *
 * Cumple UDF: expone [AddProductUiState] y emite [AddProductEffect] one-shot
 * (Navigation events, Snackbars) sin acoplar la UI al repositorio o Firebase.
 *
 * DI: Hilt provee [AddProductUseCase] automáticamente.
 */
@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val addProduct: AddProductUseCase
) : ViewModel() {

    /**
     * Generador de keys inyectable — extraído para testabilidad futura.
     * Por defecto usa UUID; podría inyectarse un `IdGenerator` si se necesita.
     */
    private val generateProductKey: () -> String = { java.util.UUID.randomUUID().toString() }

    private val _uiState = MutableStateFlow(AddProductUiState())
    val uiState: StateFlow<AddProductUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<AddProductEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AddProductEffect> = _effects.asSharedFlow()

    fun onNameChange(v: String) = _uiState.update { it.copy(name = v) }
    fun onModelChange(v: String) = _uiState.update { it.copy(modelOrBarcode = v) }
    fun onQuantityChange(v: String) = _uiState.update { it.copy(quantity = v) }
    fun onPriceChange(v: String) = _uiState.update { it.copy(unitPrice = v) }

    fun onBarcodeScanned(barcode: String) = _uiState.update { it.copy(modelOrBarcode = barcode) }

    fun onSave() {
        val s = _uiState.value
        val nameError = ProductValidators.productName(s.name)
        val qtyError = ProductValidators.positiveInt(s.quantity)
        val priceError = ProductValidators.positiveDouble(s.unitPrice.toDoubleOrNull())

        _uiState.update {
            it.copy(
                nameError = nameError,
                quantityError = qtyError,
                priceError = priceError
            )
        }

        if (nameError != null || qtyError != null || priceError != null) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val product = Product(
                id = generateProductKey(),
                name = s.name.trim(),
                modelOrBarcode = s.modelOrBarcode.trim(),
                quantity = s.quantity.toInt(),
                unitPrice = Money.fromDouble(s.unitPrice.toDouble())
            )
            val result = addProduct(product)
            _uiState.update { it.copy(isSaving = false) }
            result
                .onSuccess { _effects.tryEmit(AddProductEffect.Saved) }
                .onFailure { _effects.tryEmit(AddProductEffect.Error(it.message ?: "Error al guardar")) }
        }
    }

    sealed interface AddProductEffect {
        data object Saved : AddProductEffect
        data class Error(val message: String) : AddProductEffect
    }
}

data class AddProductUiState(
    val name: String = "",
    val modelOrBarcode: String = "",
    val quantity: String = "",
    val unitPrice: String = "",
    val nameError: String? = null,
    val quantityError: String? = null,
    val priceError: String? = null,
    val isSaving: Boolean = false
) {
    val canSubmit: Boolean
        get() = name.isNotBlank() &&
                quantity.isNotBlank() &&
                unitPrice.isNotBlank() &&
                !isSaving
}

private object ProductValidators {
    val productName: (String) -> String? = { name ->
        when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < 3 -> "Mínimo 3 caracteres"
            name.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
    }
    val positiveInt: (String) -> String? = { qty ->
        val n = qty.toIntOrNull()
        when {
            qty.isBlank() -> "La cantidad es obligatoria"
            n == null -> "Ingresa un número entero válido"
            n < 0 -> "La cantidad no puede ser negativa"
            else -> null
        }
    }
    val positiveDouble: (Double?) -> String? = { price ->
        when {
            price == null -> "El precio es obligatorio"
            price < 0.0 -> "El precio no puede ser negativo"
            else -> null
        }
    }
}