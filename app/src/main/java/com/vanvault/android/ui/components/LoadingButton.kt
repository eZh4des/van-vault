package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/**
 * Boton M3 con estado de carga integrado.
 *
 * **Por que este componente?** (T12 Item 11 + T20 Item 5)
 *  - Retroalimentacion visual inmediata: el texto se reemplaza por un
 *    spinner + "Guardando..." (el usuario sabe que la accion ocurrio).
 *  - El boton se deshabilita durante la carga (evita doble envio).
 *  - Los estados disabled/loading usan los roles semanticos de M3.
 *
 * Cumple:
 *  - Item 11: retroalimentacion de carga en operaciones > 1s
 *  - Item 20: estados visuales diferenciados (enabled/disabled/loading)
 *  - Item 17: el estado se anuncia por TalkBack ("Cargando")
 */
@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    loadingText: String = "Procesando…",
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimary
) {
    val isEnabled = enabled && !isLoading

    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier.semantics {
            stateDescription = if (isLoading) "Cargando" else ""
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
            Text(loadingText)
        } else {
            Text(text)
        }
    }
}
