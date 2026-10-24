package com.vanvault.android.domain.model

/**
 * Modo de tema seleccionado por el usuario.
 *
 * Antes solo existia un booleano `isDarkMode`, lo que impedia expressar
 * "seguir al sistema" (el comportamiento por defecto en Android).
 *
 * Cumple T05 de la auditoria: 'adaptacion completa al Tema Oscuro'.
 */
enum class ThemeMode(val displayName: String, val storageKey: String) {
    /** Sigue la configuracion del sistema (recomendado). */
    SYSTEM("Seguir al sistema", "system"),

    /** Fuerza siempre el tema claro. */
    LIGHT("Claro", "light"),

    /** Fuerza siempre el tema oscuro. */
    DARK("Oscuro", "dark");

    companion object {
        /**
         * Resuelve el modo desde la clave persistida.
         * Si es desconocida/null, devuelve [SYSTEM] (comportamiento seguro).
         */
        fun fromStorageKey(key: String?): ThemeMode =
            entries.firstOrNull { it.storageKey == key } ?: SYSTEM
    }
}

/**
 * Preferencias del usuario (persistidas en DataStore).
 */
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isNotificationsEnabled: Boolean = true,
    val language: Language = Language.SPANISH_MX
) {
    /**
     * Indica si el tema oscuro esta activo.
     *
     * @param isSystemDarkTheme `true` si el sistema operativo esta en modo oscuro.
     */
    fun isDarkThemeActive(isSystemDarkTheme: Boolean): Boolean = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
}

enum class Language(val code: String, val displayName: String) {
    SPANISH_MX("es-MX", "Español (México)"),
    ENGLISH_US("en-US", "English (US)")
}
