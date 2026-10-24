package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog de confirmación para acciones destructivas o irreversibles.
 *
 * **Nielsen #3** — "Help users recognize, diagnose, and recover from errors":
 * el usuario decide antes de que ocurra algo que no puede deshacer.
 *
 * **Nielsen #5** — "User control and freedom":
 * la accion se puede cancelar y la interfaz explica la consecuencia.
 *
 * Usado en:
 *  - InventoryScreen  → eliminar producto (con Snackbar Undo como 2a capa)
 *  - ProfileScreen    → cerrar sesión
 *  - MyAccountScreen  → cerrar sesión
 *  - Add/EditProduct  → descartar cambios sin guardar
 *
 * Cumple:
 *  - Botones >= 48dp (WCAG 2.5.8)
 *  - Texto >= 4.5:1 (WCAG 1.4.3) via colorScheme
 *  - Icono + texto (no depende solo del color, Item 19)
 */
@Composable
fun ConfirmActionDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "Confirmar",
    dismissLabel: String = "Cancelar",
    icon: ImageVector = Icons.Outlined.Warning,
    itemName: String? = null,
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    val accentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val onAccentColor = if (isDestructive) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (itemName != null) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = itemName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            maxLines = 2
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = onAccentColor
                ),
                modifier = Modifier.heightIn(min = 48.dp) // WCAG 2.5.8
            ) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text(dismissLabel)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

/**
 * Alias deprecado — usar [ConfirmActionDialog].
 *
 * Se mantiene por compatibilidad con las llamadas existentes.
 */
@Deprecated(
    message = "Usar ConfirmActionDialog",
    replaceWith = ReplaceWith("ConfirmActionDialog")
)
@Composable
fun ConfirmDeleteDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "Eliminar",
    dismissLabel: String = "Cancelar",
    icon: ImageVector = Icons.Outlined.Warning,
    itemName: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmActionDialog(
        showDialog = showDialog,
        title = title,
        message = message,
        confirmLabel = confirmLabel,
        dismissLabel = dismissLabel,
        icon = icon,
        itemName = itemName,
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
