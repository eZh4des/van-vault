# T12 – Skeletons y LinearProgressIndicator para carga

> **Bloque auditado**: II – Ítem 11 (Latencia < 100ms + indicadores)
> **Técnica *Pragmatic Programmer***: *Tracer Bullets* (percepción de velocidad)
> **Patrón Google**: **M3 Skeleton** + **LinearProgressIndicator** + **Pull-to-Refresh**

## 🎯 Objetivo

Hoy `InventoryScreen` muestra un `CircularProgressIndicator` centrado al inicio, lo que:
1. Bloquea la interacción durante la primera carga.
2. No muestra progreso continuo durante búsquedas.

Vamos a:
1. Usar **`LinearProgressIndicator` indeterminado** en el top bar durante búsquedas.
2. Mostrar **skeletons** (placeholders animados) en la primera carga.
3. Conservar el `PullToRefreshBox` existente pero añadir feedback.

## ✅ Implementación

### 1. Crear `ui/components/SkeletonCard.kt`

```kotlin
package com.vanvault.android.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Skeleton placeholder con shimmer (Material You style).
 * Cumple con "feedback de carga" mientras los datos reales llegan.
 */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHigh

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, shimmerColor, baseColor),
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
            .padding(16.dp)
    ) {
        // placeholders
        Box(Modifier.height(20.dp).fillMaxWidth(0.6f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
        Spacer(Modifier.height(8.dp))
        Box(Modifier.height(14.dp).fillMaxWidth(0.4f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
        Spacer(Modifier.height(16.dp))
        Row {
            Box(Modifier.height(24.dp).fillMaxWidth(0.3f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
            Spacer(Modifier.width(100.dp))
            Box(Modifier.height(24.dp).fillMaxWidth(0.2f).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
        }
    }
}

@Composable
fun SkeletonList(itemCount: Int = 5, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) { SkeletonCard() }
    }
}
```

### 2. Mostrar Skeleton en primera carga de InventoryScreen

```kotlin
// Reemplazar el CircularProgressIndicator centrado
if (isLoading && allProducts.isEmpty()) {
    SkeletonList(itemCount = 5)
} else if (isSearching) {
    // LinearProgressIndicator en top bar (ver paso 3)
    SkeletonList(itemCount = 3)
}
```

### 3. LinearProgressIndicator durante búsquedas

En `MainActivity.kt` o a nivel de `Scaffold`, añadir un slot para top progress:

```kotlin
val searchInProgress = viewModel.searchInProgress.collectAsState().value

if (searchInProgress) {
    LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
}
```

### 4. Mantener `PullToRefreshBox` con feedback visual

```kotlin
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { viewModel.refresh() },  // delegar al VM
    modifier = Modifier.weight(1f).fillMaxWidth()
) {
    LazyColumn { /* items */ }
}
```

## 🧪 Criterio de aceptación

- ✅ La primera carga muestra skeletons con shimmer, no un círculo que bloquea.
- ✅ Las búsquedas muestran un LinearProgressIndicator delgado en la parte superior.
- ✅ Pull-to-refresh sigue funcionando.
- ✅ Toda operación > 1 segundo tiene feedback visual (cumple ítem 11).

## 📎 Evidencia

- Captura de skeletons durante la primera carga.
- Captura de LinearProgressIndicator durante búsqueda de productos.
- Captura de Layout Inspector mostrando el progress con `height = 4.dp`.

## ⏭️ Siguiente

**T13 – Navegación Adaptativa (NavigationBar / NavigationRail / NavigationDrawer).**