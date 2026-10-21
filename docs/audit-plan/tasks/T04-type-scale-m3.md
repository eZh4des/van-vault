# T04 – Escala Tipográfica M3 completa en `sp`

> **Bloque auditado**: I – Ítem 03 (Jerarquía tipográfica y unidades `sp`)
> **Técnica *Pragmatic Programmer***: *Don't Repeat Yourself* (una sola Type Scale)
> **Patrón Google**: **Material 3 Type Scale** (Display/Headline/Title/Body/Label)

## 🎯 Objetivo

Reemplazar todos los `fontSize = NN.sp` y `MaterialTheme.typography.X.copy(...)` por
**la Type Scale oficial de M3**, garantizando:
1. Que **todos** los tamaños estén en `sp` (escalables).
2. Que haya máximo 2 familias tipográficas.
3. Que cada pantalla tenga jerarquía clara (Headline para títulos, Body para contenido, Label para metadatos).

## ✅ Implementación

### 1. Definir familia tipográfica (en `ui/theme/Type.kt`)

```kotlin
package com.vanvault.android.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Una sola familia (Roboto) - cumple "máximo 2 familias"
private val Roboto = FontFamily.Default

// Escala oficial Material 3 (los 15 estilos)
val Typography = Typography(
    // ===== Display =====
    displayLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp),

    // ===== Headline =====
    headlineLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),

    // ===== Title =====
    titleLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),

    // ===== Body =====
    bodyLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),

    // ===== Label =====
    labelLarge = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
```

### 2. Definir `Shapes` module (`ui/theme/Shape.kt`)

```kotlin
package com.vanvault.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
```

### 3. Reemplazar todos los `Text(fontSize = ...)` por `style = MaterialTheme.typography.X`

```bash
grep -rn "fontSize" app/src/main/java/com/vanvault/android/ui/screens/
```

Reglas de mapeo:
| Uso actual | Reemplazar por |
|------------|----------------|
| `Text("VanVault", fontSize = 28.sp)` (logo) | `style = MaterialTheme.typography.headlineMedium` |
| `Text("Iniciar sesión", fontSize = 16.sp, FontWeight.Bold)` | `style = MaterialTheme.typography.labelLarge` |
| `Text("Correo electrónico", fontSize = 12.sp)` | `style = MaterialTheme.typography.labelMedium` |
| `Text("SKU: ...", fontSize = 14.sp)` | `style = MaterialTheme.typography.bodyMedium` |
| `Text(priceLabel, fontSize = 10.sp)` | `style = MaterialTheme.typography.labelSmall` |

### 4. Reemplazar `.copy(fontSize = ...)` también

Cualquier `MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp)` viola el sistema.
Eliminar `.copy(fontSize=)` — usar la variante que ya tiene el tamaño correcto.

## 🧪 Criterio de aceptación

- ✅ `Type.kt` define los **15 estilos** de la escala M3.
- ✅ `grep -rn "fontSize" app/src/main/java/com/vanvault/android/` solo aparece en `theme/Type.kt`.
- ✅ Cambiar el tamaño de fuente del sistema a 200% escala correctamente toda la UI (verificable en T11).

## 📎 Evidencia

- Captura con tamaño de fuente **normal (100%)** y **grande (200%)** mostrando que todo escala.
- Captura de **Layout Inspector** confirmando unidades en `sp`.

## ⏭️ Siguiente

**T05 – Activar y verificar Dark Theme en todas las pantallas.**