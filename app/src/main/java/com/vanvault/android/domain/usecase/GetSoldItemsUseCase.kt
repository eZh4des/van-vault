package com.vanvault.android.domain.usecase

import com.vanvault.android.data.repository.MockAnalyticsRepository
import com.vanvault.android.domain.model.SoldItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSoldItemsUseCase @Inject constructor(
    private val repository: MockAnalyticsRepository
) {
    operator fun invoke(): Flow<List<SoldItem>> = repository.observeSoldItems()
}

class GetSoldItemsOnceUseCase @Inject constructor(
    private val repository: MockAnalyticsRepository
) {
    suspend operator fun invoke(): List<SoldItem> = repository.getSoldItems()
}