package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.model.UserRole
import com.vanvault.android.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Fake (test double) de [UserRepository] para tests unitarios.
 *
 * **Por que un Fake y no mockk/mockito?**
 *  - No agrega dependencias (mockk = +2MB, mockito +kotlinx coroutines hack)
 *  - Es trivial de leer (no DSL magico)
 *  - Pragmatic Tip: "A little copying is better than a little dependency"
 *
 * **Variables para assertions**:
 *  - [lastRegisteredEmail] / [lastRegisteredRole] capturan los inputs del
 *    test; los assertions pueden verificar que el UseCase llamo al repo
 *    con los valores correctos.
 *  - [registerResult] controla la respuesta del fake (success/error) para
 *    tests de error-handling.
 */
class FakeUserRepository : UserRepository {
    // Estado capturado para assertions
    var lastRegisteredEmail: String? = null
    var lastRegisteredDisplayName: String? = null
    var lastRegisteredRole: UserRole? = null
    var lastRegisteredPhone: String? = null
    var lastLoginEmail: String? = null

    // Control de respuesta
    var registerResult: Result<UserProfile> = Result.success(
        UserProfile(uid = "fake-uid", displayName = "Fake", email = "fake@x.com")
    )
    var loginResult: Result<UserProfile> = Result.success(
        UserProfile(uid = "fake-uid", displayName = "Fake", email = "fake@x.com")
    )

    // Implementacion de la interfaz
    override fun observeCurrentProfile(): Flow<UserProfile?> = flowOf(null)

    override suspend fun getCurrentProfile(): UserProfile? = null

    override suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        phone: String?,
        role: UserRole
    ): Result<UserProfile> {
        lastRegisteredEmail = email
        lastRegisteredDisplayName = displayName
        lastRegisteredRole = role
        lastRegisteredPhone = phone
        return registerResult
    }

    override suspend fun loginUser(email: String, password: String): Result<UserProfile> {
        lastLoginEmail = email
        return loginResult
    }

    override suspend fun updateProfile(displayName: String, phone: String?): Result<Unit> =
        Result.success(Unit)

    override suspend fun signOut(): Result<Unit> = Result.success(Unit)
}