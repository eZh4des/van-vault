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
    suspend fun findById(id: String): Product?
    suspend fun save(product: Product): Result<Unit>
    suspend fun update(product: Product): Result<Unit>
    suspend fun delete(id: String): Result<Unit>
}