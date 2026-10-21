package com.vanvault.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vanvault.android.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para [com.vanvault.android.ui.screens.ProfileScreen].
 *
 * Solo expone el estado de logout — el resto del ProfileScreen son
 * opciones de navegación que el caller (MainActivity) maneja.
 *
 * DI: Hilt provee [SignOutUseCase].
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val signOut: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun onLogout() {
        _uiState.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            signOut()
            _uiState.update { it.copy(isLoggingOut = false, loggedOut = true) }
        }
    }

    /** Llamado después de navegar a login para limpiar el estado. */
    fun onLogoutHandled() {
        _uiState.update { it.copy(loggedOut = false) }
    }
}

data class ProfileUiState(
    val isLoggingOut: Boolean = false,
    val loggedOut: Boolean = false
)