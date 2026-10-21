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