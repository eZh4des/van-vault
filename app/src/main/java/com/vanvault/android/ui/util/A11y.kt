package com.vanvault.android.ui.util

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics

/**
 * Modificadores de accesibilidad reutilizables (T09 — Item 17 de la auditoría).
 *
 * **Por qué un helper?**
 * TalkBack lee cada nodo hijo por separado. En una Card con ícono + título +
 * subtítulo + valor, el usuario tendría que deslizar 4 veces para entender
 * un solo elemento. `mergeDescendants = true` hace que se anuncie como
 * una unidad.
 *
 * Este helper centraliza el patron (DRY) y garantiza que se aplique de
 * forma consistente en todas las pantallas.
 */
object A11y {

    /**
     * Agrupa los descendientes en un solo nodo accesible.
     *
     * @param description Descripcion del elemento completo. Si es null, se
     *   usan las etiquetas de los hijos.
     * @param state Estado actual ("Expandido", "Seleccionado", etc).
     *   Se anuncia como "estado: <valor>".
     */
    fun Modifier.merged(
        description: String? = null,
        state: String? = null,
        role: Role? = Role.Button
    ): Modifier = semantics(mergeDescendants = true) {
        description?.let { contentDescription = it }
        state?.let { stateDescription = it }
        role?.let { this.role = it }
    }

    /**
     * Agrupa y marca un elemento como seleccionable/accionable.
     *
     * @param onClickLabel Etiqueta de la accion ("Seleccionar", "Abrir").
     *   Es lo que TalkBack anuncia al final ("doble toque para <etiqueta>").
     */
    fun Modifier.mergedAction(
        description: String? = null,
        state: String? = null,
        onClickLabel: String? = null,
        role: Role = Role.Button,
        onClick: () -> Unit
    ): Modifier = semantics(mergeDescendants = true) {
        description?.let { contentDescription = it }
        state?.let { stateDescription = it }
        this.role = role
        this.onClick(label = onClickLabel) { onClick(); true }
    }

    /**
     * Marca un elemento como decorativo: TalkBack lo ignora por completo.
     * Equivalente a `importantForAccessibility="no"` en el mundo XML.
     */
    fun Modifier.decorative(): Modifier =
        semantics(mergeDescendants = true) { }
}
