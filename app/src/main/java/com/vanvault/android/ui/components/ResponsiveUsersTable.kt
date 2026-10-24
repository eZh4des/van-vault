package com.vanvault.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vanvault.android.domain.model.AdminUser
import com.vanvault.android.domain.model.UserRole
import com.vanvault.android.ui.theme.successColor

/**
 * Tabla responsive de usuarios — vista adaptativa al zoom.
 *
 * **Adaptabilidad** (cumple Item 02 de la auditoria — multiples factores de forma):
 *  - Compact (ancho < 480dp, tipico telefono vertical):
 *    * Email debajo del nombre (sin columna email)
 *    * Rol mas pequeno
 *  - Medium (480-720dp, telefono landscape o tablet pequena):
 *    * Email a la derecha, rol a la derecha
 *  - Expanded (>= 720dp, tablet horizontal / desktop):
 *    * Email + rol + estado en columnas separadas
 *
 * **Touch targets**: 64dp minimo por fila (mas generoso que el WCAG 2.5.8
 * de 48dp, para tabular data).
 *
 * **Accesibilidad**: cada fila tiene contentDescription con todos los
 * campos para TalkBack.
 */
@Composable
fun ResponsiveUsersTable(
    users: List<AdminUser>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // 480dp = breakpoint entre compact y medium (~)
        // 720dp = breakpoint entre medium y expanded
        val width = maxWidth
        when {
            width < 480.dp -> CompactUsersList(users)
            width < 720.dp -> MediumUsersTable(users)
            else -> ExpandedUsersTable(users)
        }
    }
}

@Composable
private fun CompactUsersList(users: List<AdminUser>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = users, key = { it.id }) { user ->
            CompactUserRow(user)
        }
    }
}

@Composable
private fun CompactUserRow(user: AdminUser) {
    // Layout vertical: nombre / (email + rol en fila)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics { contentDescription = userCardDescription(user) },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(initials = user.initials, role = user.role, size = 36.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                RoleBadge(role = user.role, compact = true)
            }
        }
    }
}

@Composable
private fun MediumUsersTable(users: List<AdminUser>) {
    // Layout de filas con 3 segmentos: avatar+name, email, role
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("Usuario", modifier = Modifier.weight(2f))
                TableHeaderCell("Correo", modifier = Modifier.weight(2f))
                TableHeaderCell("Rol", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = users, key = { it.id }) { user ->
                MediumUserRow(user)
            }
        }
    }
}

@Composable
private fun MediumUserRow(user: AdminUser) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics { contentDescription = userCardDescription(user) },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(2f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserAvatar(initials = user.initials, role = user.role, size = 36.dp)
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(2f)
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End
            ) {
                RoleBadge(role = user.role)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun ExpandedUsersTable(users: List<AdminUser>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("Nombre y apellido", modifier = Modifier.weight(2.5f))
                TableHeaderCell("Correo electronico", modifier = Modifier.weight(2.5f))
                TableHeaderCell("Rol", modifier = Modifier.weight(1f))
                TableHeaderCell("Activo", modifier = Modifier.weight(0.8f), textAlign = TextAlign.End)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = users, key = { it.id }) { user ->
                ExpandedUserRow(user)
            }
        }
    }
}

@Composable
private fun ExpandedUserRow(user: AdminUser) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .semantics { contentDescription = userCardDescription(user) },
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(2.5f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                UserAvatar(initials = user.initials, role = user.role, size = 40.dp)
                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "@" + user.email.substringBefore("@"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(2.5f)
            )
            Box(modifier = Modifier.weight(1f)) {
                RoleBadge(role = user.role)
            }
            Box(
                modifier = Modifier.weight(0.8f),
                contentAlignment = Alignment.CenterEnd
            ) {
                ActiveIndicator(isActive = user.isActive)
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
private fun UserAvatar(initials: String, role: UserRole, size: Dp = 36.dp) {
    Surface(
        shape = CircleShape,
        color = if (role == UserRole.ADMIN)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(size)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (role == UserRole.ADMIN)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RoleBadge(role: UserRole, compact: Boolean = false) {
    val isAdmin = role == UserRole.ADMIN
    val bg = if (isAdmin)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceContainerHigh
    val fg = if (isAdmin)
        MaterialTheme.colorScheme.onPrimaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        contentColor = fg
    ) {
        Text(
            text = role.shortLabel,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            fontWeight = if (isAdmin) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp, vertical = if (compact) 4.dp else 6.dp)
        )
    }
}

@Composable
private fun ActiveIndicator(isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(
                    color = if (isActive) MaterialTheme.successColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape
                )
        )
        Text(
            text = if (isActive) "Activo" else "Inactivo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun userCardDescription(user: AdminUser): String =
    "${user.fullName}, ${user.email}, rol ${user.role.displayName}"