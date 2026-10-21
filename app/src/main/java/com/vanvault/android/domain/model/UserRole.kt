package com.vanvault.android.domain.model

/**
 * Rol de un usuario en la plataforma VanVault.
 *
 * **Reglas de negocio** (en [com.vanvault.android.domain.usecase.RegisterUseCase]):
 *  - Todo usuario que crea una cuenta es [CLIENT] por defecto.
 *  - El email `alex.daniel50904@gmail.com` es el unico con rol [ADMIN].
 *
 * Cualquier intento de asignar otro rol via API sera ignorado
 * (futura mejora: panel de admin para cambiar roles, validado server-side).
 *
 * Cumple "Orthogonality" del Pragmatic Programmer: el enum esta desacoplado
 * de la implementacion Firebase — un Value Object del dominio.
 */
enum class UserRole(val displayName: String, val shortLabel: String) {
    /** Cliente: usuario normal, puede gestionar su propio inventario. */
    CLIENT(displayName = "Cliente", shortLabel = "Cliente"),

    /** Administrador: puede ver panel de control y lista de usuarios. */
    ADMIN(displayName = "Administrador", shortLabel = "Admin");

    companion object {
        /**
         * Email del admin de plataforma. Single source of truth para
         * la regla de "quien es admin al registrarse".
         *
         * Para escalar a varios admins, mover a RTDB `/config/admins` y leer.
         */
        const val PLATFORM_ADMIN_EMAIL = "alex.daniel50904@gmail.com"

        /**
         * Resuelve el rol desde un email segun las reglas actuales.
         */
        fun fromEmail(email: String): UserRole =
            if (email.equals(PLATFORM_ADMIN_EMAIL, ignoreCase = true)) ADMIN else CLIENT
    }
}