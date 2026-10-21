package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Avatar circular con iniciales — reutilizable.
 *
 * Inspirado en el SVG `screen-settings.svg` (avatar 80dp #FEE2E2).
 *
 * Cumple:
 *  - WCAG 2.5.8: si el contenedor es focuseable, target >= 48dp
 *  - A11y: contentDescription = nombre del usuario (TalkBack anuncia "Administrador D V")
 *
 * Pragmatic tip: Color como parametro permite al caller controlar el fondo
 * segun el contexto (rosa para perfil, gris para cuentas deshabilitadas, etc).
 */
@Composable
fun AvatarCircle(
    initials: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    backgroundColor: Color = Color(0xFFFEE2E2),
    contentColor: Color = Color(0xFFDC2626),
    fontSizeSp: Int = 32
) {
    Surface(
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor,
        modifier = modifier
            .size(size)
            .semantics { contentDescription?.let { this.contentDescription = it } }
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontSize = fontSizeSp.sp,
                color = contentColor
            )
        }
    }
}