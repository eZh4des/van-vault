# T08 – Confirmaciones destructivas + Undo con Snackbar

> **Bloque auditado**: II – Ítem 13 (Prevención acciones destructivas), III – Ítem 19 (independencia del color)
> **Técnica *Pragmatic Programmer***: *Defensive Programming* + *Crash Early*
> **Patrón Google**: **M3 AlertDialog** + **M3 Snackbar with action**

## 🎯 Objetivo

Hoy `Eliminar producto` no hace nada y `Logout` se ejecuta sin pedir confirmación.
Necesitamos:
1. **AlertDialog M3** antes de acciones destructivas.
2. **Snackbar con acción Undo** para permitir reversión (Nielsen #5: "Help users undo errors").
3. Íconos semánticos (independencia del color, ítem 19).

## ✅ Implementación

### 1. Mejorar `VanVaultDialog.kt` para soportar confirmaciones

```kotlin
package com.vanvault.android.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Dialog reutilizable para confirmaciones destructivas.
 * Cumple Heurística Nielsen #3 (Control y libertad del usuario).
 */
@Composable
fun ConfirmDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmLabel: String = "Confirmar",
    dismissLabel: String = "Cancelar",
    isDestructive: Boolean = false,
    icon: ImageVector = Icons.Outlined.Warning,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, contentDescription = null, tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(
                    confirmLabel,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}
```

### 2. Crear `SnackbarController.kt` para mostrar Snackbars con Undo

```kotlin
package com.vanvault.android.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

suspend fun SnackbarHostState.showUndo(
    message: String,
    actionLabel: String = "Deshacer",
    onAction: suspend () -> Unit
) {
    val result = showSnackbar(
        message = message,
        actionLabel = actionLabel,
        withDismissAction = true,
        duration = SnackbarDuration.Short
    )
    if (result == SnackbarResult.ActionPerformed) {
        onAction()
    }
}
```

### 3. Conectar Logout con AlertDialog en `ProfileScreen`

```kotlin
// ProfileScreen.kt
var showLogoutDialog by remember { mutableStateOf(false) }

// Reemplazar el onClick del logout:
onClick = { showLogoutDialog = true }

// Al final del composable:
ConfirmDialog(
    showDialog = showLogoutDialog,
    title = "¿Cerrar sesión?",
    message = "Tendrás que volver a iniciar sesión para acceder a tu inventario.",
    confirmLabel = "Cerrar sesión",
    isDestructive = true,
    onConfirm = {
        FirebaseAuth.getInstance().signOut()
        onLogoutClick()
    },
    onDismiss = { showLogoutDialog = false }
)
```

### 4. Implementar `Eliminar producto` con Snackbar Undo en `InventoryScreen`

```kotlin
// InventoryScreen.kt
val snackbarHostState = remember { SnackbarHostState() }
val coroutineScope = rememberCoroutineScope()
val recentlyDeleted = remember { mutableStateOf<Product?>(null) }

fun deleteWithUndo(product: Product) {
    recentlyDeleted.value = product
    coroutineScope.launch {
        snackbarHostState.showUndo(
            message = "Producto eliminado: ${product.name}",
            onAction = {
                // Restaurar producto
                viewModel.addProduct(product)
            }
        )
        if (recentlyDeleted.value?.id == product.id) {
            viewModel.deleteProduct(product.id)
            recentlyDeleted.value = null
        }
    }
}

// En el Scaffold:
snackbarHost = { SnackbarHost(snackbarHostState) }

// En el FAB:
FabOption(text = "Eliminar producto", onClick = {
    fabExpanded = false
    val selected = allProducts.firstOrNull()
    if (selected != null) deleteWithUndo(selected) else
        showSnackbar(context, "Mantén pulsado un producto para eliminarlo")
})
```

## 🧪 Criterio de aceptación

- ✅ Pulsar "Cerrar sesión" muestra dialog de confirmación.
- ✅ Eliminar un producto dispara Snackbar con "Deshacer" (Nielsen #5).
- ✅ El Snackbar desaparece tras 4 segundos Y solo entonces se persiste la eliminación.
- ✅ El icono de Warning en el dialog NO depende solo del color (ítem 19).

## 📎 Evidencia

- Captura del AlertDialog "Cerrar sesión" con icono + texto.
- Captura del Snackbar "Producto eliminado — Deshacer" después de eliminar.

## ⏭️ Siguiente

**T09 – `contentDescription` para TalkBack (ítem 17).**