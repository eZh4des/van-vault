package com.vanvault.android.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de Material 3 para VanVault.
 *
 * **Seed**: `#DC2626` (rojo de marca, tomado de los mockups SVG).
 *
 * Todos los roles semánticos de M3 están definidos para LIGHT y DARK.
 * Las pantallas NUNCA deben usar `Color(0xFF...)` directamente: deben usar
 * `MaterialTheme.colorScheme.<rol>` (ver T03 de la auditoría).
 *
 * Contraste verificado contra WCAG 2.2 (Item 16 de la auditoría):
 *  - onSurface / surface        → 16.8:1  ✅ (requerido 4.5:1)
 *  - onSurfaceVariant / surface →  9.3:1  ✅
 *  - primary / surface          →  4.9:1  ✅
 *  - onPrimaryContainer / primaryContainer → 12.4:1 ✅
 *  - error / surface            →  6.4:1  ✅
 */

// ============================================================================
// LIGHT
// ============================================================================

val LightPrimary = Color(0xFFDC2626)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFEE2E2)
val LightOnPrimaryContainer = Color(0xFF410002)

val LightSecondary = Color(0xFF775652)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFFDAD6)
val LightOnSecondaryContainer = Color(0xFF2C1512)

val LightTertiary = Color(0xFF725B2E)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFFFDEA6)
val LightOnTertiaryContainer = Color(0xFF281800)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

val LightSuccess = Color(0xFF146C2E)
val LightOnSuccess = Color(0xFFFFFFFF)
val LightSuccessContainer = Color(0xFFC4EED0)
val LightOnSuccessContainer = Color(0xFF072711)

val LightWarning = Color(0xFF8F4E00)
val LightOnWarning = Color(0xFFFFFFFF)
val LightWarningContainer = Color(0xFFFFDCC1)
val LightOnWarningContainer = Color(0xFF2E1500)

val LightBackground = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF1C1B1F)
val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1C1B1F)
val LightSurfaceVariant = Color(0xFFF5DDDA)
val LightOnSurfaceVariant = Color(0xFF534341)
val LightSurfaceTint = LightPrimary
val LightInverseSurface = Color(0xFF313030)
val LightInverseOnSurface = Color(0xFFF4F0EF)
val LightInversePrimary = Color(0xFFFFB4AB)
val LightOutline = Color(0xFF857371)
val LightOutlineVariant = Color(0xFFD8C2C0)
val LightScrim = Color(0xFF000000)

val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFFFF1F0)
val LightSurfaceContainer = Color(0xFFFCEAE8)
val LightSurfaceContainerHigh = Color(0xFFF7E4E2)
val LightSurfaceContainerHighest = Color(0xFFF1DFDD)

// ============================================================================
// DARK
// ============================================================================

val DarkPrimary = Color(0xFFFFB4AB)
val DarkOnPrimary = Color(0xFF690005)
val DarkPrimaryContainer = Color(0xFF93000A)
val DarkOnPrimaryContainer = Color(0xFFFFDAD6)

val DarkSecondary = Color(0xFFE7BDB8)
val DarkOnSecondary = Color(0xFF442927)
val DarkSecondaryContainer = Color(0xFF5D3F3C)
val DarkOnSecondaryContainer = Color(0xFFFFDAD6)

val DarkTertiary = Color(0xFFDEC287)
val DarkOnTertiary = Color(0xFF402D04)
val DarkTertiaryContainer = Color(0xFF594419)
val DarkOnTertiaryContainer = Color(0xFFFFDEA6)

val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkSuccess = Color(0xFF6DD58C)
val DarkOnSuccess = Color(0xFF00390F)
val DarkSuccessContainer = Color(0xFF0F5223)
val DarkOnSuccessContainer = Color(0xFFC4EED0)

val DarkWarning = Color(0xFFFFB77B)
val DarkOnWarning = Color(0xFF4A2800)
val DarkWarningContainer = Color(0xFF6D3A00)
val DarkOnWarningContainer = Color(0xFFFFDCC1)

val DarkBackground = Color(0xFF1A1110)
val DarkOnBackground = Color(0xFFF1DFDD)
val DarkSurface = Color(0xFF1A1110)
val DarkOnSurface = Color(0xFFF1DFDD)
val DarkSurfaceVariant = Color(0xFF534341)
val DarkOnSurfaceVariant = Color(0xFFD8C2C0)
val DarkSurfaceTint = DarkPrimary
val DarkInverseSurface = Color(0xFFF1DFDD)
val DarkInverseOnSurface = Color(0xFF313030)
val DarkInversePrimary = LightPrimary
val DarkOutline = Color(0xFFA08C8A)
val DarkOutlineVariant = Color(0xFF534341)
val DarkScrim = Color(0xFF000000)

val DarkSurfaceContainerLowest = Color(0xFF140C0B)
val DarkSurfaceContainerLow = Color(0xFF231918)
val DarkSurfaceContainer = Color(0xFF271D1C)
val DarkSurfaceContainerHigh = Color(0xFF322826)
val DarkSurfaceContainerHighest = Color(0xFF3D3231)

// ============================================================================
// PALETA PARA USOS NO-COMPOSABLE
// ============================================================================

/**
 * Constantes semánticas para contextos donde NO se puede usar
 * `MaterialTheme.colorScheme.*` (ej: valores por defecto de parámetros de
 * funciones no-@Composable, o constantes de companion object).
 *
 * **Preferencia**: dentro de un @Composable, siempre usar
 * `MaterialTheme.colorScheme.<rol>` para respetar el tema activo
 * (light/dark). Usar estas constantes solo como fallback.
 *
 * Ver [com.vanvault.android.ui.theme.MaterialTheme.successColor] y
 * [com.vanvault.android.ui.theme.MaterialTheme.warningColor] para los
 * roles extendidos.
 */
object VanVaultPalette {
    /** Rojo de marca (equivalente a primary en light). */
    val BrandPrimary = LightPrimary

    /** Contenedor rojo suave (equivalente a primaryContainer en light). */
    val BrandPrimaryContainer = LightPrimaryContainer

    /** Texto sobre rojo suave. */
    val OnBrandPrimaryContainer = LightOnPrimaryContainer

    /** Verde de estado positivo. */
    val Success = LightSuccess

    /** Contenedor verde suave. */
    val SuccessContainer = LightSuccessContainer

    /** Naranja de estado de alerta. */
    val Warning = LightWarning

    /** Contenedor naranja suave. */
    val WarningContainer = LightWarningContainer

    /** Gris neutro para texto secundario. */
    val Neutral = Color(0xFF71717A)

    /** Gris neutro en tema oscuro. */
    val NeutralDark = Color(0xFFA1A1AA)

    /** Borde suave (equivalente a outlineVariant). */
    val BorderSoft = LightOutlineVariant
}
