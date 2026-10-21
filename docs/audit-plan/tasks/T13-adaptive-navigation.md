# T13 – Navegación Adaptativa M3 (NavigationSuiteScaffold)

> **Bloque auditado**: I – Ítem 01 (Navegación nativa)
> **Técnica *Pragmatic Programmer***: *Easier to Change* (un componente, tres factores de forma)
> **Patrón Google**: **M3 NavigationSuiteScaffold** + **WindowSizeClass**

## 🎯 Objetivo

Sustituir el `VanVaultBottomNavBar` actual (solo para móvil vertical) por el
**`NavigationSuiteScaffold`** oficial de Material 3, que elige automáticamente entre:
- `NavigationBar` (Bottom) → móvil vertical (≤ 600dp ancho)
- `NavigationRail` (lateral) → tablet vertical / foldable (600-840dp)
- `NavigationDrawer` (modal) → tablet horizontal / desktop (> 840dp)

## ✅ Implementación

### 1. Crear `ui/components/VanVaultNavigationSuite.kt`

```kotlin
package com.vanvault.android.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Adaptive navigation root.
 * Decide el layout según WindowSizeClass y el número de items.
 * Cumple ítem 01: Bottom Nav para 3-5 destinos, Drawer para >5.
 */
enum class VanVaultDestination(
    val route: String,
    val labelRes: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
) {
    INVENTORY("home", "Inventario", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    SCANNER("scanner", "Escáner", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
    SALES("sales", "Ventas", Icons.Filled.Receipt, Icons.Outlined.Receipt),
    PROFILE("profile", "Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VanVaultNavigationSuite(
    currentDestination: VanVaultDestination,
    onDestinationSelected: (VanVaultDestination) -> Unit,
    content: @Composable () -> Unit
) {
    val navSuiteItems = VanVaultDestination.entries.map { dest ->
        val selected = dest == currentDestination
        NavigationSuiteItem(
            selected = selected,
            onClick = { onDestinationSelected(dest) },
            icon = {
                Icon(
                    imageVector = if (selected) dest.iconSelected else dest.iconUnselected,
                    contentDescription = dest.labelRes
                )
            },
            label = { Text(dest.labelRes) }
        )
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = { navSuiteItems.forEach { it() } },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        content()
    }
}
```

### 2. Eliminar `BottomNavBar.kt` antiguo (o marcar como deprecated)

Mantener `NavItem` enum pero mapear a `VanVaultDestination`.

### 3. Usar en `InventoryScreen.kt` y `ProfileScreen.kt`

```kotlin
// Antes: VanVaultBottomNavBar(currentRoute = NavItem.INVENTORY, onNavigate = ...)
// Ahora: VanVaultNavigationSuite(
//            currentDestination = VanVaultDestination.INVENTORY,
//            onDestinationSelected = { onNavigate(it.route) }
//        ) {
//            // contenido
//        }
```

### 4. Manejar back-stack en `MainActivity.kt`

```kotlin
// Patrón correcto (ya está en parte hecho):
NavHost(...) {
    composable("home") { ... }
    composable("profile") { ... }
    ...
}

// Para tab navigation con save state:
composable("home") {
    InventoryScreen(
        onNavigate = { dest ->
            navController.navigate(dest.route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}
```

## 🧪 Criterio de aceptación

- ✅ En móvil vertical aparece `NavigationBar` abajo.
- ✅ En emulador de tablet (sw600dp) aparece `NavigationRail` a la izquierda.
- ✅ En emulador de desktop (sw840dp) aparece `NavigationDrawer` permanente.
- ✅ El botón "atrás" del sistema vuelve a la pantalla anterior, no cierra la app (BackStack gestionado).

## 📎 Evidencia

- Captura del Bottom Navigation Bar en móvil (Portrait 360×800).
- Captura del Navigation Rail en tablet (Landscape 1280×800).
- Captura del Navigation Drawer en foldable abierto.

## ⏭️ Siguiente

**T14 – Eficiencia de flujo (Time on Task / toques).**