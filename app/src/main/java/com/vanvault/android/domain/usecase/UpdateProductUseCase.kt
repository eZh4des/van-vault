package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.repository.ProductRepository
import javax.inject.Inject

/**
 * UpdateProductUseCase — Pragma: encapsula reglas de modificación.
 * Design by Contract: valida antes de invocar al repositorio.
 */
class UpdateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Unit> {
        require(product.id.isNotBlank()) { "Product id is required" }
        require(product.name.isNotBlank()) { "Name cannot be blank" }
        require(product.quantity >= 0) { "Quantity must be ≥ 0" }
        return repository.update(product)
    }
}