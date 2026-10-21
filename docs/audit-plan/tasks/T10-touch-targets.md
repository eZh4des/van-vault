# T10 – Touch Targets ≥ 48dp (WCAG 2.5.8)

> **Bloque auditado**: III – Ítem 15 (Tamaño mínimo de objetivos táctiles)
> **Técnica *Pragmatic Programmer***: *Orthogonality* (un solo `MinTouchTarget` para toda la app)
> **Patrón Google**: **Material Design Touch Target Guidelines** (48dp)

## 🎯 Objetivo

WCAG 2.2 introduce el criterio **2.5.8 Target Size (Minimum)**: cada objetivo interactivo debe
tener al menos **24×24 CSS px** (Android equivalente: **48dp**) y al menos **8dp de separación**
entre objetivos adyacentes.

Hoy varios `IconButton` (tamaño por defecto 40dp) **no cumplen**.

## ✅ Implementación

### 1. Constantes reutilizables (`ui/theme/Dimens.kt`)

```kotlin
package com.vanvault.android.ui.theme

import androidx.compose.ui.unit.dp

object Dimens {
    val MinTouchTarget = 48.dp
    val TouchTargetSpacing = 8.dp
    val ScreenPadding = 16.dp
    val CardPadding = 16.dp
    val GridGutter = 8.dp
}
```

### 2. Extensión `Modifier.minTouchTarget()` para los IconButton

```kotlin
// ui/components/TouchTarget.kt
package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** Garantiza 48dp mínimo (WCAG 2.5.8). */
fun Modifier.minTouchTarget(): Modifier = composed {
    this.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
}
```

### 3. Aplicar a TODOS los `IconButton`

```bash
grep -rn "IconButton" app/src/main/java/com/vanvault/android/
```

Cada uno debe envolver el Icon:

```kotlin
IconButton(
    onClick = { ... },
    modifier = Modifier.minTouchTarget()   // ← añadir
) {
    Icon(...)
}
```

### 4. Para botones pequeños con texto corto (ej. "?"), usar `IconButtonDefaults`

```kotlin
IconButton(
    onClick = { /* ... */ },
    modifier = Modifier.minTouchTarget()
) {
    Icon(Icons.Default.Add, contentDescription = null)
}
```

### 5. Validar con Layout Inspector

- Activar "Show layout bounds" en Developer Options.
- Recorrer todas las pantallas.
- Cada elemento clickeable debe tener al menos 48dp de alto y ancho.

### 6. Validar con Accessibility Scanner

- Correr Accessibility Scanner.
- Verificar que no haya alertas de "Touch target size too small".

## 🧪 Criterio de aceptación

- ✅ `grep -rn "IconButton" app/` muestra que TODOS tienen `Modifier.minTouchTarget()` o están en componentes M3 que ya cumplen.
- ✅ Layout Inspector muestra bounds ≥ 48dp en cada elemento clickeable.
- ✅ Accessibility Scanner sin alertas de target size.

## 📎 Evidencia

- Captura de Layout Inspector en InventoryScreen con bounds visibles.
- Captura de Accessibility Scanner sin alertas.

## ⏭️ Siguiente

**T11 – Reflow al 200% (WCAG 1.4.4 / 1.4.10).**