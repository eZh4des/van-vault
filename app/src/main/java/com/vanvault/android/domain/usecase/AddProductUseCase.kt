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