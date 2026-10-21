package com.vanvault.android.domain.usecase

import com.vanvault.android.data.repository.MockAnalyticsRepository
import com.vanvault.android.domain.model.DashboardSnapshot
import javax.inject.Inject

class GetDashboardSnapshotUseCase @Inject constructor(
    private val repository: MockAnalyticsRepository
) {
    suspend operator fun invoke(): DashboardSnapshot = repository.getDashboardSnapshot()
}