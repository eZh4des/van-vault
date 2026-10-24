package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Column vertical con scroll vertical garantizado.
 *
 * **Por que este componente?** (T11 — WCAG 1.4.4 / 1.4.10)
 * Cuando el usuario aumenta el tamano de fuente del sistema al 200%,
 * el contenido de una pantalla sin scroll se **corta**: los botones inferiores
 * dejan de ser accesibles y el texto se trunca.
 *
 * Este componente garantiza:
 *  - Scroll vertical (WCAG 1.4.10 Reflow)
 *  - `imePadding()` para que el teclado virtual no tape los campos
 *  - `contentPadding` consistente (retícula M3 de 8dp)
 *  - `Arrangement.spacedBy` en vez de Spacers multiples
 *
 * **Antes**: 14 pantallas tenian `Column(modifier = Modifier.padding(innerPadding))`
 * sin scroll, lo que cortaba el contenido al 200% de fuente.
 */
@Composable
fun ScrollableColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .imePadding()
            .padding(contentPadding),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        content()
    }
}
