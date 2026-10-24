package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Campo de texto M3 reutilizable para todos los formularios de la app.
 *
 * **Por que un componente centralizado?** (Pragmatic Tip: DRY)
 *  - 12+ campos de texto repetian el mismo codigo de colores y formas
 *  - Centralizar garantiza que TODOS los campos tengan:
 *    * etiqueta flotante persistente (M3 OutlinedTextField)
 *    * `keyboardType` correcto segun el dato (WCAG 1.3.5 Identify Input Purpose)
 *    * `imeAction` para navegar el formulario sin tocar la pantalla
 *    * `supportingText` con el mensaje de error
 *    * `isError` + color de error
 *    * `contentDescription` para TalkBack
 *
 * Cumple T06 de la auditoria: 'Diseno de Formularios y Teclado Contextual'.
 */
@Composable
fun M3TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    // Etiqueta explicita para TalkBack: el label flotante
                    // puede no anunciarse cuando el campo tiene valor.
                    contentDescription = label
                },
            label = { Text(label) },
            enabled = enabled,
            isError = isError,
            singleLine = singleLine,
            minLines = minLines,
            visualTransformation = when {
                isPassword && !passwordVisible -> PasswordVisualTransformation()
                else -> VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions,
            shape = MaterialTheme.shapes.small,
            trailingIcon = when {
                isPassword && onTogglePasswordVisibility != null -> {
                    {
                        IconButton(
                            onClick = onTogglePasswordVisibility,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.VisibilityOff
                                else
                                    Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible)
                                    "Ocultar contraseña"
                                else
                                    "Mostrar contraseña",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> trailingIcon
            },
            supportingText = when {
                isError && errorMessage != null -> {
                    { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                }
                supportingText != null -> {
                    { Text(supportingText) }
                }
                else -> null
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                errorLabelColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
