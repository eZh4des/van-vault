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
 * Dialog de confirmación para acciones destructivas (Nielsen #9 — "Help users
 * recognize, diagnose, and recover from errors").
 *
 * Características:
 *  - Botones claros: acción destructiva a la derecha (Cancelar / Confirmar)
 *  - Color de error en el botón de confirmación
 *  - Ícono + título + mensaje descriptivo
 *  - Cumple WCAG: text contrast ≥ 4.5:1, touch targets ≥ 48dp
 *
 * Usado en:
 *  - InventoryScreen → Eliminar producto
 *  - ProfileScreen → Cerrar sesión (futuro)
 *  - EditProductScreen → Descartar cambios (futuro)
 */
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
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
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
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
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