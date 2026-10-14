package com.vanvault.android.domain.model

data class UserSettings(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = true,
    val language: Language = Language.SPANISH_MX
)

enum class Language(val code: String, val displayName: String) {
    SPANISH_MX("es-MX", "Español (México)"),
    ENGLISH_US("en-US", "English (US)")
}
