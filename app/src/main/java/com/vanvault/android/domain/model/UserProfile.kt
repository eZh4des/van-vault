package com.vanvault.android.domain.model

/**
 * Perfil del usuario actual, combinando Firebase Auth + Realtime Database.
 *
 * **Modelo de dominio puro**: las pantallas (Account/MyAccount) reciben esta
 * estructura inmutable. La capa data combina FirebaseAuth.currentUser con
 * `/users/{uid}` desde Realtime Database.
 *
 * **Pragmatic tip**: cumple OOP — `initials` y `displayRole` se calculan
 * desde las propiedades, no se almacenan redundantes.
 */
data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val phone: String? = null,
    val isEmailVerified: Boolean = false,
    val initials: String = computeInitials(displayName, email)
) {
    companion object {
        /**
         * Calcula las iniciales para el avatar circular:
         *  - Prioriza las primeras letras del displayName (max 2)
         *  - Fallback: primeras letras del email
         *  - Edge case: si no hay nada, devuelve "U" (Usuario)
         */
        fun computeInitials(name: String, email: String): String {
            val fromName = name.trim()
                .split(Regex("\\s+"))
                .filter { it.isNotBlank() }
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")
            if (fromName.isNotBlank()) return fromName
            val fromEmail = email.trim()
                .substringBefore("@")
                .take(2)
                .uppercase()
            return fromEmail.ifBlank { "U" }
        }
    }
}