package com.vanvault.android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Escalas de radio de esquinas Material 3.
 *
 * M3 define 5 niveles ExtraSmall → ExtraLarge.
 * Los valores son multiplos de 4dp (retícula base M3).
 *
 * Cumple Item 02 de la auditoría (Retícula 8dp / 4dp en micro-elementos).
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
