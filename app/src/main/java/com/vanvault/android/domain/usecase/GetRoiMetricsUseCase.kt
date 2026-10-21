package com.vanvault.android.domain.usecase

import com.vanvault.android.data.repository.MockAnalyticsRepository
import com.vanvault.android.domain.model.RoiMetrics
import javax.inject.Inject

class GetRoiMetricsUseCase @Inject constructor(
    private val repository: MockAnalyticsRepository
) {
    suspend operator fun invoke(): RoiMetrics = repository.getRoiMetrics()
}