package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Observa el perfil del usuario actual (Cold flow).
 * Re-emite cuando cambian campos de RTDB.
 */
class ObserveUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeCurrentProfile()
}

/**
 * Snapshot único del perfil actual.
 */
class GetUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): UserProfile? = repository.getCurrentProfile()
}

/**
 * Actualiza displayName + phone del usuario actual.
 */
class UpdateUserProfileUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(displayName: String, phone: String?): Result<Unit> =
        repository.updateProfile(displayName, phone)
}

/**
 * Cierra la sesión.
 */
class SignOutUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.signOut()
}