# T01 – Arquitectura: Crear capa de Domain (UseCases)

> **Bloque auditado**: I – Ítem 01 (Navegación), II – Ítems 08/09/14 (Métricas UX)
> **Técnica *Pragmatic Programmer***: *Orthogonality*, *Decoupling*, *Design by Contract*
> **Patrón Google**: **Clean Architecture en 3 capas** (UI / Domain / Data)

## 🎯 Objetivo

Hoy `ProductRepository` vive en `data/` y los Composables lo instancian directamente.
Crearemos la **capa de Domain** que define **interfaces de repositorio**, **modelos de dominio enriquecidos**
y **UseCases** para que la UI dependa solo de abstracciones.

## 📁 Estructura nueva

```
app/src/main/java/com/vanvault/android/
├── domain/
│   ├── model/
│   │   ├── Product.kt               ← mover y enriquecer
│   │   ├── StockStatus.kt           ← nuevo
│   │   └── Money.kt                 ← nuevo (Value Object)
│   ├── repository/
│   │   ├── ProductRepository.kt     ← interfaz
│   │   └── SettingsRepository.kt    ← ya existe, validar contrato
│   └── usecase/
│       ├── SearchProductsUseCase.kt
│       ├── ObserveInventoryUseCase.kt
│       ├── AddProductUseCase.kt
│       ├── DeleteProductUseCase.kt
│       └── UpdateProductUseCase.kt
├── data/
│   ├── repository/
│   │   └── ProductRepositoryImpl.kt ← implementación Firebase
│   └── ...
└── ui/
    ├── viewmodel/
    │   ├── InventoryViewModel.kt    ← nuevo
    │   └── ...
    └── ...
```

## ✅ Pasos concretos

### 1. Crear `domain/model/Product.kt` (modelo enriquecido)

```kotlin
package com.vanvault.android.domain.model

import com.vanvault.android.domain.model.Money

/**
 * Domain entity representing a product in the inventory.
 * Encapsulates business rules (DRY: stock status, formatted price).
 *
 * Pragmatic tip: "Code is the design" — put the *behavior* here, not in Composables.
 */
data class Product(
    val id: String,
    val name: String,
    val modelOrBarcode: String,
    val quantity: Int,
    val unitPrice: Money
) {
    /** Single source of truth for stock classification (Design by Contract). */
    val stockStatus: StockStatus
        get() = when {
            quantity <= 0 -> StockStatus.OUT_OF_STOCK
            quantity < LOW_STOCK_THRESHOLD -> StockStatus.LOW_STOCK
            else -> StockStatus.AVAILABLE
        }

    companion object {
        const val LOW_STOCK_THRESHOLD = 5
    }
}
```

### 2. Crear `domain/model/StockStatus.kt`

```kotlin
package com.vanvault.android.domain.model

/**
 * Domain enum used to drive UI in BOTH screen reader announcements
 * (TalkBack: "3 unidades — Insuficiente") and color roles.
 * Avoids hardcoding strings in Composables (DRY).
 */
enum class StockStatus(val labelKey: String) {
    AVAILABLE("stock_available"),
    LOW_STOCK("stock_low"),
    OUT_OF_STOCK("stock_out")
}
```

### 3. Crear `domain/model/Money.kt` (Value Object)

```kotlin
package com.vanvault.android.domain.model

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToLong

/** Money is a Value Object — immutable, currency-aware, equality by value. */
@JvmInline
value class Money private constructor(val cents: Long) {

    fun toDisplayString(locale: Locale = Locale("es", "CO")): String {
        val format = NumberFormat.getCurrencyInstance(locale).apply {
            maximumFractionDigits = 0
        }
        return format.format(cents / 100.0)
    }

    operator fun times(quantity: Int): Money =
        Money(cents * quantity)

    companion object {
        fun ofCop(whole: Long): Money = Money(whole * 100)
        fun fromDouble(value: Double): Money = Money((value * 100).roundToLong())
        val ZERO = Money(0)
    }
}
```

### 4. Crear `domain/repository/ProductRepository.kt` (interfaz)

```kotlin
package com.vanvault.android.domain.repository

import com.vanvault.android.domain.model.Product
import kotlinx.coroutines.flow.Flow

/**
 * Sealed result types — Design by Contract: makes invalid states unrepresentable.
 */
sealed interface ProductSearchResult {
    data class Success(val products: List<Product>, val hasMore: Boolean) : ProductSearchResult
    data object Empty : ProductSearchResult
    data class Error(val message: String) : ProductSearchResult
}

sealed interface ProductLookupResult {
    data class Found(val product: Product) : ProductLookupResult
    data object NotFound : ProductLookupResult
    data class Error(val message: String) : ProductLookupResult
}

interface ProductRepository {
    fun observeInventory(): Flow<List<Product>>
    suspend fun search(query: String, limit: Int = 30): ProductSearchResult
    suspend fun findByName(name: String): ProductLookupResult
    suspend fun save(product: Product): Result<Unit>
    suspend fun update(product: Product): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}
```

### 5. Crear `domain/usecase/SearchProductsUseCase.kt`

```kotlin
package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.repository.ProductRepository
import com.vanvault.android.domain.repository.ProductSearchResult
import javax.inject.Inject

/**
 * UseCase = single piece of business logic.
 * Encapsulates input validation so Composables don't have to (Orthogonality).
 */
class SearchProductsUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(query: String): ProductSearchResult {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return ProductSearchResult.Empty
        }
        return repository.search(trimmed)
    }
}
```

### 6. Crear los demás UseCases (Add, Update, Delete, ObserveInventory)

```kotlin
// domain/usecase/ObserveInventoryUseCase.kt
package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveInventoryUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = repository.observeInventory()
}
```

```kotlin
// domain/usecase/AddProductUseCase.kt
package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Unit> {
        require(product.name.isNotBlank()) { "Name cannot be blank" }
        require(product.quantity >= 0) { "Quantity must be ≥ 0" }
        return repository.save(product)
    }
}
```

```kotlin
// domain/usecase/DeleteProductUseCase.kt
package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    /** Returns a [Restorable] so the UI can offer Undo via Snackbar. */
    suspend operator fun invoke(id: String): Result<Restorable> {
        val product = repository.findById(id)
            ?: return Result.failure(IllegalStateException("Product not found"))
        val saveResult = repository.save(product)
        if (saveResult.isFailure) return Result.failure(saveResult.exceptionOrNull()!!)
        val deleteResult = repository.delete(id)
        if (deleteResult.isFailure) return Result.failure(deleteResult.exceptionOrNull()!!)
        return Result.success(Restorable(product, deleteResult.getOrThrow()))
    }

    data class Restorable(val backup: com.vanvault.android.domain.model.Product, val ignored: Unit)
}
```

### 7. Mover y adaptar `ProductRepositoryImpl.kt` a `data/repository/`

- Reemplaza `Product` por la versión de `domain`.
- Implementa los métodos de la interfaz `ProductRepository` (incluido `observeInventory()` con `callbackFlow`).
- Convierte los `Long` monetarios a `Money`.

### 8. Actualizar todas las pantallas (temporalmente) para usar el nuevo `Product`

- `InventoryScreen.kt`: cambiar `Product.unitPrice` (Double) → `Product.unitPrice` (Money).
- `AddProductScreen.kt`: enviar `Money.fromDouble(unitPrice.toDoubleOrNull() ?: 0.0)`.

## 🧪 Criterio de aceptación

- ✅ Existen archivos en `domain/model/`, `domain/repository/`, `domain/usecase/`.
- ✅ Compila `./gradlew assembleDebug` sin errores.
- ✅ Los Composables **no importan** `com.google.firebase.*` directamente (verificable con `grep`).
- ✅ `Product.stockStatus` se usa desde la UI en lugar de `when` hardcodeado.

## 📎 Evidencia a adjuntar

- Captura de **Android Studio – Structure panel** mostrando los 3 paquetes.
- Captura del resultado de `grep -r "firebase" app/src/main/java/com/vanvault/android/ui/` → debe estar vacío.

## ⏭️ Siguiente

**T02 – Configurar Hilt para inyección de dependencias.**