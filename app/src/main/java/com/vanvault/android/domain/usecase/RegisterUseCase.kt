package com.vanvault.android.domain.usecase

import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Registra un usuario nuevo.
 *
 * Encapsula:
 *  - Validacion de formato de email (Gmail)
 *  - Validacion de fortaleza de contrasena (>=8, mayuscula, minuscula, numero)
 *  - Validacion de confirmacion de contrasena
 *  - Validacion de nombre no vacio
 *  - Llamada a [UserRepository.registerUser]
 *
 * Devuelve `Result.success(UserProfile)` si todo OK o
 * `Result.failure(IllegalArgumentException)` con mensaje legible si los inputs son invalidos.
 *
 * **Por que encapsular validacion aqui?**
 * - Cumple "Design by Contract" del Pragmatic Programmer.
 * - La UI solo necesita mostrar el error, no conoce las reglas.
 * - Facilita testing (mockeas el repo y verificas las validaciones).
 */
class RegisterUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        phone: String? = null
    ): Result<UserProfile> {
        // Validaciones tempranas (crash early)
        val vName = name.trim()
        if (vName.isBlank()) return Result.failure(IllegalArgumentException("El nombre es obligatorio"))
        if (vName.length < 3) return Result.failure(IllegalArgumentException("El nombre debe tener al menos 3 caracteres"))

        val vEmail = email.trim().lowercase()
        if (vEmail.isBlank()) return Result.failure(IllegalArgumentException("El correo es obligatorio"))
        if (!vEmail.matches(EMAIL_REGEX)) {
            return Result.failure(IllegalArgumentException(
                "El correo debe ser una direccion valida."
            ))
        }
        if (vEmail.length > 254) {
            return Result.failure(IllegalArgumentException("El correo es demasiado largo"))
        }

        if (password != confirmPassword) {
            return Result.failure(IllegalArgumentException("Las contrasenas no coinciden"))
        }
        if (!password.matches(PASSWORD_REGEX)) {
            return Result.failure(IllegalArgumentException(
                "La contrasena debe tener al menos 8 caracteres, una mayuscula, una minuscula y un numero."
            ))
        }

        // Llamada al repo (encapsula Firebase)
        return repository.registerUser(
            email = vEmail,
            password = password,
            displayName = vName,
            phone = phone?.trim()?.takeIf { it.isNotBlank() }
        )
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private val PASSWORD_REGEX = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$")
    }
}