# T06 – Formularios M3: Floating Labels, `inputType` y `imeOptions`

> **Bloque auditado**: I – Ítem 06 (Diseño de Formularios M3)
> **Técnica *Pragmatic Programmer***: *DRY* (componente reutilizable `M3TextField`)
> **Patrón Google**: **M3 OutlinedTextField** + **WCAG 1.3.5 (Identify Input Purpose)**

## 🎯 Objetivo

Estandarizar todos los campos de texto:
1. **Floating Label** (etiqueta persistente que sube al enfocar).
2. `keyboardType` coherente con el dato (email, number, phone, decimal).
3. `imeOptions` para flujo natural (`actionNext`, `actionDone`).
4. Crear un componente reutilizable para evitar duplicación (DRY).

## ✅ Implementación

### 1. Crear componente reutilizable `ui/components/M3TextField.kt`

```kotlin
package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

/**
 * M3 Text Field reutilizable.
 *
 * Pragmatic tip: DRY — evita repetir OutlinedTextField + colors en cada pantalla.
 * Google tip: imeOptions + inputType cumplen WCAG 1.3.5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3TextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    contentDescriptionOverride: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescriptionOverride?.let { contentDescription = it }
                },
            singleLine = singleLine,
            enabled = enabled,
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            ),
            supportingText = {
                if (isError && errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}
```

### 2. Reescribir LoginScreen usando el nuevo componente

```kotlin
M3TextField(
    value = email,
    onValueChange = { email = it },
    label = "Correo electrónico",
    keyboardType = KeyboardType.Email,
    imeAction = ImeAction.Next,
    isError = showEmailError,
    errorMessage = if (showEmailError) "Formato de correo inválido" else null,
    contentDescriptionOverride = "Campo para correo electrónico"
)

M3TextField(
    value = password,
    onValueChange = { password = it },
    label = "Contraseña",
    keyboardType = KeyboardType.Password,
    imeAction = ImeAction.Done,
    // ... otros params
)
```

### 3. Mapear cada campo en las pantallas

| Campo | keyboardType | imeAction | ¿Password? |
|-------|-------------|-----------|------------|
| Email (Login/Register) | `Email` | `Next` | No |
| Password | `Password` | `Done` | Sí (VisualTransformation) |
| Nombre producto | `Text` | `Next` | No |
| Modelo/Barcode | `Text` (o `Number`?) | `Next` | No |
| Cantidad | `Number` | `Next` | No |
| Precio unitario | `Decimal` | `Done` | No |

### 4. Validación inline (DRY + Design by Contract)

Reemplazar el patrón actual:
```kotlin
// ❌ Antes: Toast genérico
if (name.isBlank()) { Toast.makeText(context, "Por favor llena todos los campos", ...).show(); return }
```

Por validación por campo:
```kotlin
// ✅ Ahora: error visible debajo del campo, datos preservados
val nameError = if (name.isBlank()) "El nombre es obligatorio" else null
M3TextField(value = name, ..., isError = nameError != null, errorMessage = nameError)
```

## 🧪 Criterio de aceptación

- ✅ Cada `OutlinedTextField` se reemplaza por `M3TextField`.
- ✅ Las etiquetas flotan al enfocar y persisten con el valor.
- ✅ Los `imeOptions` están definidos en cada campo.
- ✅ El teclado contextual cambia según el `keyboardType` (email muestra @, number muestra números).

## 📎 Evidencia

- Captura de pantalla de `AddProductScreen` con un campo enfocado mostrando el label flotante.
- Captura del teclado de email (con `@`) y teclado numérico (con `Number`).

## ⏭️ Siguiente

**T07 – Empty States + manejo Offline.**