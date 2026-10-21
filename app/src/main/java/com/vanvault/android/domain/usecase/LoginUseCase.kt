package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Login de usuario — valida inputs y delega al repositorio.
 *
 * **Validaciones**:
 *  - email no vacio
 *  - password no vacio y >= 6 chars (Firebase requiere min 6)
 *
 * **Importante**: NO validamos formato de email aqui (el server hace esa
 * validacion en login). Solo evitamos inputs vacios.
 *
 * Devuelve `Result.success(UserProfile)` si las credenciales son validas
 * o `Result.failure(Throwable)` si:
 *  - Firebase lanza FirebaseAuthInvalidUserException / FirebaseAuthInvalidCredentialsException
 *  - Validacion falla
 */
class LoginUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserProfile> {
        val vEmail = email.trim()
        if (vEmail.isBlank()) return Result.failure(
            IllegalArgumentException("El correo es obligatorio")
        )

        if (password.isBlank()) return Result.failure(
            IllegalArgumentException("La contrasena es obligatoria")
        )
        if (password.length < MIN_PASSWORD_LENGTH) return Result.failure(
            IllegalArgumentException("La contrasena debe tener al menos $MIN_PASSWORD_LENGTH caracteres")
        )

        return repository.loginUser(vEmail, password)
    }

    companion object {
        const val MIN_PASSWORD_LENGTH = 6  // Firebase requiere >= 6
    }
}