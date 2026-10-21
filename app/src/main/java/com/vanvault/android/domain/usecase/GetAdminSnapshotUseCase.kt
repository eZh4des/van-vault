package com.vanvault.android.domain.usecase

import com.vanvault.android.data.repository.MockAnalyticsRepository
import com.vanvault.android.domain.model.AdminSnapshot
import javax.inject.Inject

class GetAdminSnapshotUseCase @Inject constructor(
    private val repository: MockAnalyticsRepository
) {
    suspend operator fun invoke(): AdminSnapshot = repository.getAdminSnapshot()
}