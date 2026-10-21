package com.vanvault.android.domain.model

/**
 * Usuario (cualquier rol) que aparece en el panel de administracion.
 *
 * Modelo plano (no un User generico) — Pragmatic tip: "Code close to the
 * problem domain". El panel de admin solo necesita estos campos.
 *
 * **Reutiliza** [UserRole] del dominio para mantener consistencia con
 * el sistema de roles. El enum interno [Role] antiguo (OWNER/ADMIN/EMPLOYEE/VIEWER)
 * se removio: ahora la fuente unica es [UserRole].
 */
data class AdminUser(
    val id: String,
    val displayName: String,
    val email: String,
    val role: UserRole,
    val initials: String,
    val isActive: Boolean,
    val lastSeenLabel: String
)

/**
 * Snapshot para el panel de Admin.
 */
data class AdminSnapshot(
    val totalUsers: Int,
    val activeUsers: Int,
    val users: List<AdminUser>,
    val pendingTasks: List<PendingTask>
)

data class PendingTask(
    val id: String,
    val title: String,
    val description: String,
    val priority: Priority
) {
    enum class Priority(val label: String) {
        LOW("Baja"), MEDIUM("Media"), HIGH("Alta")
    }
}