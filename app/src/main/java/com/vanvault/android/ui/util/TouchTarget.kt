package com.vanvault.android.ui.util

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.dp

/**
 * Modificador que garantiza un area tactil minima de 48dp x 48dp.
 *
 * **WCAG 2.2 criterio 2.5.8 (Target Size - Minimum)**:
 * todos los objetivos interactivos deben medir al menos 48x48 CSS px
 * (en Android, 48dp).
 *
 * **Por que un helper?** (Pragmatic Tip 11 — DRY)
 * En vez de repetir `.heightIn(min = 48.dp)` en 20 sitios, se aplica
 * `.minTouchTarget()` y queda garantizado por construccion.
 *
 * Material3 ya aplica `minimumInteractiveComponentSize` a sus botones
 * (IconButton, Button, etc.), asi que este helper es para los
 * componentes propios (Surface/Box/Row con `.clickable`).
 */
fun Modifier.minTouchTarget(minSize: Int = 48): Modifier = composed {
    this.sizeIn(minWidth = minSize.dp, minHeight = minSize.dp)
}

/**
 * Separacion minima entre objetivos tactiles adyacentes (8dp).
 *
 * WCAG 2.2 nota: ademas del tamano minimo, los objetivos no deben
 * superponerse ni estar demasiado cerca.
 */
val TouchTargetSpacing = 8.dp
