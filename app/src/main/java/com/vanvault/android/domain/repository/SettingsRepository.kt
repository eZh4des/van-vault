package com.vanvault.android.domain.repository

import com.vanvault.android.domain.model.Language
import com.vanvault.android.domain.model.ThemeMode
import com.vanvault.android.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de preferencias del usuario.
 */
interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setLanguage(language: Language)
}
