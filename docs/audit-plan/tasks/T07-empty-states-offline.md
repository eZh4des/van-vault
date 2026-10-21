# T07 – Empty States informativos + Manejo Offline

> **Bloque auditado**: II – Ítem 12 (Estados vacíos + modo sin conexión)
> **Técnica *Pragmatic Programmer***: *Don't Live With Broken Windows* (UX Resiliente)
> **Patrón Google**: **Offline-first** (NetworkBoundResource / Flow combinado)

## 🎯 Objetivo

Cuando:
1. Una lista está vacía → mostrar ilustración + mensaje + acción clara (no solo texto gris).
2. No hay red → mostrar banner informativo + permitir seguir trabajando con caché local.

Hoy `InventoryScreen` solo muestra `"No se encontraron resultados"` en gris.

## ✅ Implementación

### 1. Crear componente `ui/components/EmptyState.kt`

```kotlin
package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Estado vacío reutilizable (Decorator pattern — envuelve cualquier lista).
 *
 * Cumple WCAG 1.1.1: imagen tiene contentDescription.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // decorativo; el title lo describe
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
```

### 2. Usar en `InventoryScreen`

```kotlin
if (displayProducts.isEmpty()) {
    EmptyState(
        icon = if (isOfflineMode) Icons.Outlined.CloudOff else Icons.Outlined.Inventory2,
        title = if (isSearchActive && searchQuery.isNotBlank())
            "No encontramos resultados"
        else if (isOfflineMode)
            "Sin conexión"
        else
            "Aún no tienes productos",
        description = if (isOfflineMode)
            "Mostrando datos en caché. Verifica tu conexión para sincronizar."
        else
            "Agrega tu primer producto usando el botón +",
        actionLabel = if (!isSearchActive && !isOfflineMode) "Agregar producto" else null,
        onAction = if (!isSearchActive && !isOfflineMode) ({ onAddProductClick() }) else null
    )
}
```

### 3. Detectar estado Offline con `ConnectivityManager`

```kotlin
// ui/util/NetworkObserver.kt
package com.vanvault.android.ui.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberIsOnline(): State<Boolean> {
    val context = LocalContext.current
    return produceState(initialValue = true) {
        val cm = context.getSystemService(ConnectivityManager::class.java)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { value = true }
            override fun onLost(network: Network) { value = false }
        }
        cm.registerDefaultNetworkCallback(callback)
        awaitDispose { cm.unregisterNetworkCallback(callback) }
    }
}
```

### 4. Crear componente `OfflineBanner`

```kotlin
@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
    val isOnline by rememberIsOnline()
    AnimatedVisibility(visible = !isOnline) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.width(12.dp))
                Text(
                    "Sin conexión a Internet",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
```

## 🧪 Criterio de aceptación

- ✅ Con Firebase vacío, `InventoryScreen` muestra icono + título + descripción + acción.
- ✅ En modo avión, aparece el `OfflineBanner` rojo arriba.
- ✅ `EmptyState` se usa también en `NotificationsScreen`, `HelpScreen`, etc. (cualquier lista vacía).

## 📎 Evidencia

- Captura del Empty State de inventario con icono y acción "Agregar producto".
- Captura del banner "Sin conexión" activado en modo avión.

## ⏭️ Siguiente

**T08 – Confirmaciones con AlertDialog + Snackbar Undo (Heurísticas Nielsen 3 y 5).**