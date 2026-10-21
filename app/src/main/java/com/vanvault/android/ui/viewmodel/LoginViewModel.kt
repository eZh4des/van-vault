package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para [com.vanvault.android.ui.screens.LoginScreen].
 *
 * UDF: [uiState] inmutable + [effects] one-shot.
 *
 * DI: Hilt provee [LoginUseCase].
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<LoginEffect>(extraBufferCapacity = 2)
    val effects: SharedFlow<LoginEffect> = _effects.asSharedFlow()

    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onLogin() {
        val s = _uiState.value
        if (s.isSubmitting) return
        if (s.email.isBlank() || s.password.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Por favor, llena todos los campos para poder iniciar sesion.")
            }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = loginUseCase(s.email, s.password)
            _uiState.update { it.copy(isSubmitting = false) }
            result
                .onSuccess { profile ->
                    if (profile.isEmailVerified) {
                        _effects.tryEmit(LoginEffect.Success(profile))
                    } else {
                        _uiState.update {
                            it.copy(errorMessage = "Verifica tu correo electronico primero.")
                        }
                    }
                }
                .onFailure { e ->
                    val msg = mapFirebaseError(e.message)
                    _uiState.update { it.copy(errorMessage = msg) }
                }
        }
    }

    /**
     * Mapea errores de Firebase a mensajes amigables en espanol.
     */
    private fun mapFirebaseError(raw: String?): String {
        val m = raw.orEmpty().lowercase()
        return when {
            "no user record" in m || "user not found" in m ->
                "No existe una cuenta con este correo."
            "password is invalid" in m || "wrong password" in m ->
                "Contrasena incorrecta."
            "network" in m || "timeout" in m ->
                "Sin conexion. Verifica tu red."
            "too many requests" in m ->
                "Demasiados intentos. Intenta mas tarde."
            else -> raw ?: "Error desconocido al iniciar sesion"
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

sealed interface LoginEffect {
    data class Success(val profile: com.vanvault.android.domain.model.UserProfile) : LoginEffect
}