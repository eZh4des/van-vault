package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.usecase.GetUserProfileUseCase
import com.vanvault.android.domain.usecase.ObserveUserProfileUseCase
import com.vanvault.android.domain.usecase.SignOutUseCase
import com.vanvault.android.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para [com.vanvault.android.ui.screens.MyAccountScreen].
 *
 * Exposición:
 *  - [uiState]: el estado actual de la pantalla (loading, profile, error)
 *  - [effects]: eventos one-shot (Snackbar, navegación)
 *
 * DI: Hilt provee los 4 UseCases automáticamente.
 */
@HiltViewModel
class MyAccountViewModel @Inject constructor(
    private val observeProfile: ObserveUserProfileUseCase,
    private val getProfile: GetUserProfileUseCase,
    private val updateProfile: UpdateUserProfileUseCase,
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyAccountUiState())
    val uiState: StateFlow<MyAccountUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observeProfileChanges()
    }

    /**
     * Carga inicial del perfil y se suscribe a cambios.
     * Cumple UDF: el state siempre refleja el último perfil conocido.
     */
    private fun loadProfile() {
        viewModelScope.launch {
            val profile = getProfile()
            _uiState.update { it.copy(isLoading = false, profile = profile) }
        }
    }

    private fun observeProfileChanges() {
        viewModelScope.launch {
            observeProfile().collect { profile ->
                _uiState.update { it.copy(isLoading = false, profile = profile) }
            }
        }
    }

    fun onUpdateProfile(displayName: String, phone: String?) {
        viewModelScope.launch {
            val result = updateProfile(displayName, phone)
            result.onSuccess {
                _uiState.update { it.copy(message = "Perfil actualizado") }
            }.onFailure {
                _uiState.update { it.copy(message = it.message ?: "Error al actualizar perfil") }
            }
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            signOut()
            // El navController debería reaccionar al cambio en auth (signOut vuelve a login)
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }
}

data class MyAccountUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val message: String? = null
)