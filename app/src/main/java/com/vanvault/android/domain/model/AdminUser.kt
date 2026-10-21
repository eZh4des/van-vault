package com.vanvault.android.domain.model

/**
 * Usuario administrador que aparece en el panel de admin.
 *
 * Modelo plano (no un User genérico) — Pragmatic tip: "Code close to the
 * problem domain". El admin solo necesita estos campos para su pantalla.
 */
data class AdminUser(
    val id: String,
    val displayName: String,
    val email: String,
    val role: Role,
    val initials: String,        // para el avatar circular
    val isActive: Boolean,
    val lastSeenLabel: String    // "Hace 5 min", "Ayer", "Desconectado"
) {
    enum class Role(val label: String) {
        OWNER("Propietario"),
        ADMIN("Administrador"),
        EMPLOYEE("Empleado"),
        VIEWER("Visualizador")
    }
}

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