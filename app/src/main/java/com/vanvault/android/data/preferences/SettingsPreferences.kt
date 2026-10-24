package com.vanvault.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vanvault.android.domain.model.Language
import com.vanvault.android.domain.model.ThemeMode
import com.vanvault.android.domain.model.UserSettings
import com.vanvault.android.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Implementacion de [SettingsRepository] sobre DataStore Preferences.
 *
 * Persiste:
 *  - `theme_mode` (string: system/light/dark) — T05
 *  - `notifications_enabled` (boolean)
 *  - `language` (string)
 */
@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val LANGUAGE = stringPreferencesKey("language")
    }

    override val settings: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            themeMode = ThemeMode.fromStorageKey(preferences[PreferencesKeys.THEME_MODE]),
            isNotificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            language = preferences[PreferencesKeys.LANGUAGE]?.let { code ->
                Language.entries.find { it.code == code }
            } ?: Language.SPANISH_MX
        )
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.storageKey
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override suspend fun setLanguage(language: Language) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language.code
        }
    }
}

/**
 * Binding module: SettingsPreferences ↔ SettingsRepository.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsPreferences): SettingsRepository
}
