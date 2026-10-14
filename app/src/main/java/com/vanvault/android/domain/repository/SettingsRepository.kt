package com.vanvault.android.domain.repository

import com.vanvault.android.domain.model.Language
import com.vanvault.android.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setLanguage(language: Language)
}
