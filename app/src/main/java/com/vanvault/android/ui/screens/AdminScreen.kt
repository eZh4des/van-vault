package com.vanvault.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanvault.android.domain.model.AdminUser
import com.vanvault.android.domain.model.PendingTask
import com.vanvault.android.ui.components.SkeletonUserCard
import com.vanvault.android.ui.components.SkeletonMetricCard
import com.vanvault.android.ui.components.SkeletonUserCard
import com.vanvault.android.ui.components.M3TopBarWithBack
import com.vanvault.android.ui.util.A11y.merged
import com.vanvault.android.ui.components.SkeletonUserCard
import com.vanvault.android.ui.components.SkeletonMetricCard
import com.vanvault.android.ui.components.SkeletonUserCard
import com.vanvault.android.ui.components.RedBanner
import com.vanvault.android.ui.viewmodel.AdminViewModel
import com.vanvault.android.ui.theme.successColor
import com.vanvault.android.ui.theme.warningColor

/**
 * AdminScreen — panel de administración.
 *
 * Diseño basado en `screen-admin.svg`:
 *  - TopBar con back
 *  - Resumen: total usuarios / activos
 *  - Lista de usuarios con avatar + role + status
 *  - Lista de tareas pendientes (alertas)
 */
@Composable
fun AdminScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            M3TopBarWithBack(
                title = "Administración",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        if (state.isLoading || state.snapshot == null) {
            // T12: skeleton de usuarios + metricas (Item 11)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonMetricCard(modifier = Modifier.weight(1f))
                    SkeletonMetricCard(modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                repeat(4) { SkeletonUserCard() }
            }
        } else {
            val snapshot = state.snapshot!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // Resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        label = "Usuarios totales",
                        value = "${snapshot.totalUsers}",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Activos",
                        value = "${snapshot.activeUsers}",
                        accent = MaterialTheme.successColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Tareas pendientes
                if (snapshot.pendingTasks.isNotEmpty()) {
                    Text(
                        text = "Tareas pendientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    snapshot.pendingTasks.forEach { task ->
                        PendingTaskCard(task)
                        Spacer(Modifier.height(8.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Equipo (vista adaptativa al zoom)
                Text(
                    text = "Equipo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                com.vanvault.android.ui.components.ResponsiveUsersTable(
                    users = snapshot.users,
                    modifier = Modifier.heightIn(max = 600.dp)
                )
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    accent: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.heightIn(min = 88.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun PendingTaskCard(task: PendingTask) {
    val priorityColor = when (task.priority) {
        PendingTask.Priority.HIGH -> MaterialTheme.colorScheme.primary
        PendingTask.Priority.MEDIUM -> MaterialTheme.warningColor
        PendingTask.Priority.LOW -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(priorityColor, CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = priorityColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = task.priority.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = priorityColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun UserCard(user: AdminUser) {
    val isActive = user.isActive
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // T09: anunciar el usuario completo como una unidad
            .merged(
                description = "${user.fullName}, ${user.email}, rol ${user.role.displayName}, ${user.lastSeenLabel}",
                state = if (isActive) "Activo" else "Inactivo"
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        // T11: FlowRow — al 200% de fuente el badge de rol baja de linea
        // en vez de encogerse (WCAG 1.4.4 Resize Text)
        FlowRow(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            // Avatar circular con iniciales
            Surface(
                shape = CircleShape,
                color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.initials,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (isActive) MaterialTheme.successColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = user.lastSeenLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Text(
                    text = user.role.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}