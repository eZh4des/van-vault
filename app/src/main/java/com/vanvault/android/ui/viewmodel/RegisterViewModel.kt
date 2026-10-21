package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.usecase.RegisterUseCase
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
 * ViewModel para [com.vanvault.android.ui.screens.RegisterScreen].
 *
 * UDF: expone [uiState] (inmutable) + [effects] (one-shot).
 *
 * DI: Hilt provee [RegisterUseCase].
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<RegisterEffect>(extraBufferCapacity = 2)
    val effects: SharedFlow<RegisterEffect> = _effects.asSharedFlow()

    /**
     * Cambia el nombre. Validacion simple (no se valida regex aqui —
     * eso lo hace el UseCase al enviar).
     */
    fun onNameChange(v: String) = _uiState.update { it.copy(name = v, errorMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, errorMessage = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, errorMessage = null) }

    /**
     * Llamado por la UI tras mostrar [RegisterEffect.Error] para limpiar el state.
     */
    fun onErrorShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onRegister() {
        if (_uiState.value.isSubmitting) return
        val s = _uiState.value
        if (s.name.isBlank() || s.email.isBlank() || s.password.isBlank() || s.confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor llena todos los campos para registrarte.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val result = registerUseCase(
                name = s.name,
                email = s.email,
                password = s.password,
                confirmPassword = s.confirmPassword,
                phone = null  // El campo phone se agrega cuando lo pida la UI
            )
            _uiState.update { it.copy(isSubmitting = false) }
            result
                .onSuccess { profile ->
                    _effects.tryEmit(RegisterEffect.Success(profile.email))
                }
                .onFailure { e ->
                    val msg = e.message ?: "Error desconocido al registrar"
                    _uiState.update { it.copy(errorMessage = msg) }
                }
        }
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

sealed interface RegisterEffect {
    data class Success(val email: String) : RegisterEffect
}