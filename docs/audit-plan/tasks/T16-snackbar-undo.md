# T16 – Snackbar Undo consistente en toda la app

> **Bloque auditado**: II – Ítem 13 (control del usuario)
> **Técnica *Pragmatic Programmer***: *DRY* (un solo componente para todas las acciones reversibles)
> **Patrón Google**: **M3 SnackbarHostState** + **actionLabel**

## 🎯 Objetivo

Centralizar el Snackbar Undo en una clase/side-effect compartido para que:
1. Cualquier acción destructiva (eliminar producto, descartar cambios, cerrar sesión) pueda revertirse.
2. El SnackbarHost sea único por NavHost.
3. Se respete el principio de **Control y libertad** de Nielsen.

## ✅ Implementación

### 1. Crear `ui/util/SnackbarController.kt`

```kotlin
package com.vanvault.android.ui.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

/**
 * Centraliza mensajes + Undo.
 * Usar `rememberSnackbarController()` en Composables.
 */
class SnackbarController(val hostState: SnackbarHostState) {
    suspend fun showUndo(
        message: String,
        actionLabel: String = "Deshacer",
        onUndo: suspend () -> Unit
    ) {
        val result = hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = true,
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            onUndo()
        }
    }

    suspend fun show(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        hostState.showSnackbar(message = message, duration = duration)
    }
}
```

### 2. Proveer a nivel de `MainActivity`

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VanVaultTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val snackbar = remember { SnackbarController(snackbarHostState) }

                // Pasarlo a través de CompositionLocal o directamente a cada Composable
                CompositionLocalProvider(LocalSnackbarController provides snackbar) {
                    AppNavGraph(snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}

val LocalSnackbarController = staticCompositionLocalOf<SnackbarController> {
    error("No SnackbarController provided")
}
```

### 3. Consumir en `InventoryScreen`

```kotlin
val snackbar = LocalSnackbarController.current

fun deleteWithUndo(product: Product) {
    viewModel.deleteProduct(product.id)   // optimismo: borrar inmediatamente
    coroutineScope.launch {
        snackbar.showUndo("Producto eliminado: ${product.name}") {
            viewModel.restoreProduct(product)
        }
    }
}
```

### 4. Usar en cambios no guardados (Add/Edit Product)

```kotlin
val snackbar = LocalSnackbarController.current

BackHandler(enabled = hasUnsavedChanges) {
    snackbar.showUndo(
        message = "Tienes cambios sin guardar",
        actionLabel = "Mantener"
    ) {
        // do nothing → re-enable BackHandler? Better: discard and navigate
        navController.navigateUp()
    }
    // Show another snackbar to confirm discard?
}
```

## 🧪 Criterio de aceptación

- ✅ `SnackbarController` se inyecta vía `CompositionLocal`.
- ✅ Cada acción destructiva (eliminar, descartar, logout) usa el mismo Snackbar con Undo.
- ✅ La acción Undo es funcional y restaura el estado.

## 📎 Evidencia

- Captura del Snackbar "Producto eliminado — Deshacer" en InventoryScreen.
- Captura tras pulsar "Deshacer": el producto vuelve a la lista.

## ⏭️ Siguiente

**T17 – Iconografía vectorial consistente.**