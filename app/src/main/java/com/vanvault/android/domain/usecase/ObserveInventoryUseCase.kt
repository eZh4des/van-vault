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