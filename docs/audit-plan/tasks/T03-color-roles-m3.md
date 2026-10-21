# T03 – Sistema de Color M3 (Light + Dark completo)

> **Bloque auditado**: I – Ítem 04 (Roles semánticos y Dark Theme)
> **Técnica *Pragmatic Programmer***: *Orthogonality* (un solo origen para los colores)
> **Patrón Google**: **Material 3 Color Roles** (primary, secondary, tertiary, surface, etc.)

## 🎯 Objetivo

Reemplazar los `Color(0xFFDC2626)` hard-coded y el `Color.kt` incompleto por un esquema
de color M3 completo (light + dark) con todos los roles semánticos.
Esto resuelve simultáneamente:
- **Ítem 04** (Dark Theme completo)
- **Ítem 16** (contraste WCAG 4.5:1) — usaremos seed UPC verde + colores validados
- Auditoría visual del **Ítem 02** (consistencia de paleta)

## 📐 Paleta nueva (Material Theme Builder, seed = UPC verde)

### Light
| Rol | Color | Uso |
|-----|-------|-----|
| `primary` | `#006C3A` | Botones primarios, FAB, indicadores activos |
| `onPrimary` | `#FFFFFF` | Texto sobre `primary` |
| `primaryContainer` | `#9CF6B6` | Fondo de chips activos, FAB extendido |
| `onPrimaryContainer` | `#00210E` | Texto sobre `primaryContainer` |
| `secondary` | `#4E6354` | Acciones secundarias |
| `tertiary` | `#825500` | Highlights, badges |
| `error` | `#BA1A1A` | Errores, "Eliminar" |
| `errorContainer` | `#FFDAD6` | Fondo de mensajes de error |
| `background` | `#F6FBF5` | Fondo general |
| `surface` | `#F6FBF5` | Tarjetas |
| `surfaceContainer*` | `#FFFFFF` → `#DFE4DE` | Elevaciones M3 |
| `onSurface` | `#181D19` | Texto principal |
| `onSurfaceVariant` | `#404942` | Texto secundario (≥ 4.5:1 OK) |
| `outline` | `#707971` | Bordes |
| `outlineVariant` | `#C0C9C0` | Bordes sutiles |

### Dark (mismo seed, tonos invertidos)
| Rol | Color |
|-----|-------|
| `primary` | `#80D99C` |
| `onPrimary` | `#00391C` |
| `primaryContainer` | `#00522B` |
| `onPrimaryContainer` | `#9CF6B6` |
| `background` | `#101511` |
| `surface` | `#101511` |
| `surfaceContainer` | `#1C211D` |
| `onSurface` | `#DFE4DE` |
| `onSurfaceVariant` | `#C0C9C0` |
| `error` | `#FFB4AB` |
| `errorContainer` | `#93000A` |

> Validar contraste con [WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/) — **objetivo ≥ 4.5:1** en `onSurface`/`surface` y ≥ 3:1 en `outline`/`surface`.

## ✅ Implementación

### 1. Reescribir `ui/theme/Color.kt`

```kotlin
package com.vanvault.android.ui.theme

import androidx.compose.ui.graphics.Color

// ======== LIGHT ========
val PrimaryLight = Color(0xFF006C3A)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFF9CF6B6)
val OnPrimaryContainerLight = Color(0xFF00210E)

val SecondaryLight = Color(0xFF4E6354)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFD0E8D5)
val OnSecondaryContainerLight = Color(0xFF0B1F14)

val TertiaryLight = Color(0xFF825500)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFFDDB3)
val OnTertiaryContainerLight = Color(0xFF291800)

val ErrorLight = Color(0xFFBA1A1A)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)

val BackgroundLight = Color(0xFFF6FBF5)
val OnBackgroundLight = Color(0xFF181D19)

val SurfaceLight = Color(0xFFF6FBF5)
val OnSurfaceLight = Color(0xFF181D19)
val OnSurfaceVariantLight = Color(0xFF404942)
val SurfaceVariantLight = Color(0xFFDFE4DE)
val OutlineLight = Color(0xFF707971)
val OutlineVariantLight = Color(0xFFC0C9C0)

// ======== DARK ========
val PrimaryDark = Color(0xFF80D99C)
val OnPrimaryDark = Color(0xFF00391C)
val PrimaryContainerDark = Color(0xFF00522B)
val OnPrimaryContainerDark = Color(0xFF9CF6B6)

val SecondaryDark = Color(0xFFB5CCBA)
val OnSecondaryDark = Color(0xFF213528)
val SecondaryContainerDark = Color(0xFF374B3D)
val OnSecondaryContainerDark = Color(0xFFD0E8D5)

val TertiaryDark = Color(0xFFFFB951)
val OnTertiaryDark = Color(0xFF452B00)
val TertiaryContainerDark = Color(0xFF633F00)
val OnTertiaryContainerDark = Color(0xFFFFDDB3)

val ErrorDark = Color(0xFFFFB4AB)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundDark = Color(0xFF101511)
val OnBackgroundDark = Color(0xFFDFE4DE)

val SurfaceDark = Color(0xFF101511)
val OnSurfaceDark = Color(0xFFDFE4DE)
val OnSurfaceVariantDark = Color(0xFFC0C9C0)
val SurfaceVariantDark = Color(0xFF404942)
val OutlineDark = Color(0xFF8A938B)
val OutlineVariantDark = Color(0xFF404942)
```

### 2. Reescribir `ui/theme/Theme.kt`

```kotlin
package com.vanvault.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceVariant = SurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceVariant = SurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
)

@Composable
fun VanVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
```

### 3. Buscar y reemplazar todos los `Color(0xFF...)` hard-coded en pantallas

`grep -rn "Color(0xFF" app/src/main/java/com/vanvault/android/ui/` y reemplazar por:
- Rojo `#DC2626` → `MaterialTheme.colorScheme.error`
- Verde `#059669` → `MaterialTheme.colorScheme.primary`
- Naranja `#F97316` → `MaterialTheme.colorScheme.tertiary`
- Grises → `MaterialTheme.colorScheme.outline` / `onSurfaceVariant`

### 4. Verificar contraste con Accessibility Scanner

## 🧪 Criterio de aceptación

- ✅ Theme.kt declara los 25+ roles semánticos de M3.
- ✅ Dark mode se activa con el toggle del sistema y no se "rompe" ningún texto.
- ✅ `grep -r "Color(0xFF" app/src/main/java/com/vanvault/android/ui/` solo devuelve referencias dentro de `theme/Color.kt`.
- ✅ Accessibility Scanner no reporta contraste < 4.5:1.

## 📎 Evidencia

- Captura del **Theme Builder** de Material mostrando los colores generados.
- Captura de pantalla en **Light** y **Dark** mode (login + inventario).
- Captura de **Accessibility Scanner** sin alertas de contraste.

## ⏭️ Siguiente

**T04 – Escala tipográfica M3 completa (Headline/Title/Label) en `sp`.**