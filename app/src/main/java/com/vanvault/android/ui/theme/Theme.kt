package com.vanvault.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores Material 3 completo para VanVault.
 *
 * Incluye los **roles estándar de M3** (primary, secondary, tertiary, error,
 * surface, surfaceContainer*, outline, inverse*, scrim) más los **roles
 * extendidos de estado** que usa la app:
 *  - `success` / `onSuccess` / `successContainer` / `onSuccessContainer`
 *  - `warning` / `onWarning` / `warningContainer` / `onWarningContainer`
 *
 * Estos últimos se acceden vía `MaterialTheme.colorScheme.success` porque
 * M3 soporta `ColorScheme` extendido con `CompositionLocal` propio
 * (ver [VanVaultExtraColors]).
 *
 * Cumple T03 de la auditoría:
 *  - Item 04: Roles semánticos de color + modo oscuro completo
 *  - Item 16: Contraste WCAG 2.2 AA verificado (ver Color.kt)
 */
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    inversePrimary = LightInversePrimary,

    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,

    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,

    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,

    background = LightBackground,
    onBackground = LightOnBackground,

    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightSurfaceTint,

    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,

    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim,

    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,

    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    background = DarkBackground,
    onBackground = DarkOnBackground,

    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,

    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,

    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,

    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest
)

/**
 * Colores extendidos de estado (success / warning).
 *
 * M3 no define roles para "exito" o "advertencia", asi que los exponemos via
 * un `CompositionLocal` propio. Uso:
 * ```
 * val success = VanVaultExtraColors.current.success
 * ```
 * o mas ergonomicamente via la extension `MaterialTheme.successColor`.
 */
data class VanVaultExtraColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color
)

val LocalVanVaultExtraColors = androidx.compose.runtime.staticCompositionLocalOf {
    VanVaultExtraColors(
        success = LightSuccess,
        onSuccess = LightOnSuccess,
        successContainer = LightSuccessContainer,
        onSuccessContainer = LightOnSuccessContainer,
        warning = LightWarning,
        onWarning = LightOnWarning,
        warningContainer = LightWarningContainer,
        onWarningContainer = LightOnWarningContainer
    )
}

/** Acceso ergonomico a los colores extendidos: `MaterialTheme.successColor`. */
val MaterialTheme.successColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.success

val MaterialTheme.onSuccessColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.onSuccess

val MaterialTheme.successContainerColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.successContainer

val MaterialTheme.onSuccessContainerColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.onSuccessContainer

val MaterialTheme.warningColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.warning

val MaterialTheme.onWarningColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.onWarning

val MaterialTheme.warningContainerColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.warningContainer

val MaterialTheme.onWarningContainerColor: Color
    @Composable get() = LocalVanVaultExtraColors.current.onWarningContainer

/**
 * Tema raíz de la app.
 *
 * @param darkTheme  Si es `true`, usa el esquema oscuro. Por defecto sigue al sistema.
 * @param dynamicColor Si es `true` y el dispositivo tiene Android 12+, usa los colores
 *   del wallpaper (Material You). Por defecto `false` para preservar la identidad
 *   de marca roja de VanVault.
 */
@Composable
fun VanVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extraColors = if (darkTheme) {
        VanVaultExtraColors(
            success = DarkSuccess,
            onSuccess = DarkOnSuccess,
            successContainer = DarkSuccessContainer,
            onSuccessContainer = DarkOnSuccessContainer,
            warning = DarkWarning,
            onWarning = DarkOnWarning,
            warningContainer = DarkWarningContainer,
            onWarningContainer = DarkOnWarningContainer
        )
    } else {
        VanVaultExtraColors(
            success = LightSuccess,
            onSuccess = LightOnSuccess,
            successContainer = LightSuccessContainer,
            onSuccessContainer = LightOnSuccessContainer,
            warning = LightWarning,
            onWarning = LightOnWarning,
            warningContainer = LightWarningContainer,
            onWarningContainer = LightOnWarningContainer
        )
    }

    // Sincronizar el color de la barra de estado del sistema con el tema.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalVanVaultExtraColors provides extraColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
