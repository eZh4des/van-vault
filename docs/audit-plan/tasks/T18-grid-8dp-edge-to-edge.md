# T18 – Retícula de 8dp + Edge-to-Edge + Zonas Seguras

> **Bloque auditado**: I – Ítem 02 (Retícula espacial y zonas seguras)
> **Técnica *Pragmatic Programmer***: *Don't Repeat Yourself* (constantes de espaciado)
> **Patrón Google**: **M3 Layout Grid (8dp base)** + **WindowInsets**

## 🎯 Objetivo

Hoy hay valores arbitrarios (`Spacer(80.dp)`, `padding(24.dp)`, `height(56.dp)`).
M3 define una retícula base de **8dp** (4dp en micro-elementos).

Plan:
1. Definir **constantes** (`Dimens`) reutilizables.
2. Activar **Edge-to-Edge** correctamente (ya hay `enableEdgeToEdge()`).
3. Usar `WindowInsets.systemBars` para respetar zonas seguras.

## ✅ Implementación

### 1. Refinar `ui/theme/Dimens.kt`

```kotlin
package com.vanvault.android.ui.theme

import androidx.compose.ui.unit.dp

object Dimens {
    // Grid base
    val Space1 = 4.dp   // micro
    val Space2 = 8.dp   // base
    val Space3 = 12.dp
    val Space4 = 16.dp  // card padding
    val Space5 = 20.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val Space10 = 40.dp
    val Space12 = 48.dp

    // Componentes
    val ScreenPadding = Space4
    val CardPadding = Space4
    val GridGutter = Space2
    val MinTouchTarget = 48.dp

    // Shapes
    val CardCornerLarge = Space4   // 16dp
    val CardCornerMedium = Space3 // 12dp
    val CardCornerSmall = Space2  // 8dp
    val ButtonCorner = 24.dp
}
```

### 2. Edge-to-Edge correctamente

`MainActivity.kt` ya tiene `enableEdgeToEdge()`. Verificar que:
- Los `Scaffold` no usan `containerColor` que tape la status bar.
- Se respeta `WindowInsets.systemBars` en TopAppBar.

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
Scaffold(
    topBar = {
        TopAppBar(
            windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            ...
        )
    },
    contentWindowInsets = WindowInsets.safeDrawing
) { innerPadding ->
    // Aplicar innerPadding a todo el contenido
    Column(modifier = Modifier.padding(innerPadding)) { ... }
}
```

### 3. Reemplazar todos los valores arbitrarios

```bash
grep -rn "Modifier.height\|Modifier.size\|Spacer(Modifier.height" app/src/main/java/com/vanvault/android/ui/screens/
```

Mapear cada valor a la constante más cercana:
- `4.dp` → `Dimens.Space1`
- `8.dp` → `Dimens.Space2`
- `12.dp` → `Dimens.Space3`
- `16.dp` → `Dimens.Space4`
- `20.dp` → `Dimens.Space5`
- `24.dp` → `Dimens.Space6`
- `32.dp` → `Dimens.Space8`
- `48.dp` → `Dimens.Space12`

### 4. Validar con Layout Inspector

- Activar "Show layout bounds".
- Verificar que todos los `padding` y `margin` están en múltiplos de 4.

## 🧪 Criterio de aceptación

- ✅ `Dimens.kt` centraliza todos los tamaños.
- ✅ Edge-to-Edge habilitado: la UI llega a los bordes laterales y respeta la status bar.
- ✅ Layout Inspector confirma alineación 4dp / 8dp.
- ✅ Capturas en Light y Dark: el contenido nunca queda bajo la status bar o la barra de gestos.

## 📎 Evidencia

- Captura con "Show layout bounds" activado.
- Captura lado a lado Light/Dark en móvil vertical mostrando la retícula consistente.

## ⏭️ Siguiente

**T19 – Orden lógico de foco + foco visible no obstruido (WCAG 2.4.3 / 2.4.11).**