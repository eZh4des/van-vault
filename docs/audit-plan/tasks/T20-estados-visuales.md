# T20 – Estados Visuales de Componentes Interactivos

> **Bloque auditado**: I – Ítem 05 (Estados visuales diferenciados)
> **Técnica *Pragmatic Programmer***: *Polymorphism* (un componente, varios estados)
> **Patrón Google**: **M3 Interaction State Layers** (Pressed, Focused, Hovered, Dragged, Disabled)

## 🎯 Objetivo

Cada componente interactivo debe tener representación visual diferenciada para:
- **Enabled / Disabled**
- **Pressed** (ripple)
- **Focused** (borde/outline)
- **Loading** (progress dentro del botón)

Hoy la mayoría usa solo colores hardcoded sin diferenciar estados.

## ✅ Implementación

### 1. Componente `LoadingButton` reutilizable (Decorator pattern)

```kotlin
package com.vanvault.android.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * M3 Filled Button con estado de carga integrado.
 * - enabled=true, isLoading=false → muestra texto
 * - enabled=true, isLoading=true  → muestra spinner + texto "Cargando..."
 * - enabled=false                  → gris (estado disabled)
 */
@Composable
fun LoadingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
            Text("Guardando…")
        } else {
            Text(text)
        }
    }
}
```

### 2. Aplicar `LoadingButton` en LoginScreen y AddProductScreen

```kotlin
LoadingButton(
    text = "Iniciar sesión",
    onClick = { /* login */ },
    isLoading = isLoading,
    modifier = Modifier.fillMaxWidth().height(50.dp)
)
```

### 3. Asegurar Ripple en superficies

```kotlin
Surface(
    onClick = onClick,    // ← usa Surface(onClick=...) que ya tiene ripple
    modifier = Modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceContainer,
    shape = MaterialTheme.shapes.medium
) {
    Row { /* contenido */ }
}
```

> **Diferencia clave**: `Surface` con `onClick` tiene ripple; `Surface` con `Modifier.clickable` también,
> pero el primero es preferible (mantiene la posición del ripple controlado).

### 4. Estado Disabled explícito

```kotlin
Button(
    onClick = { /* ... */ },
    enabled = canSubmit,  // ← isLoading, formIsValid, etc.
    colors = ButtonDefaults.buttonColors(
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    )
) { /* ... */ }
```

### 5. Estado Focused

M3 lo aplica automáticamente:
- `OutlinedTextField` → borde primary cuando focused.
- `Button` → elevation increase cuando focused.
- Verificar manualmente con captura.

## 🧪 Criterio de aceptación

- ✅ `LoadingButton` se usa en todos los formularios.
- ✅ Cada botón tiene ripple al presionar.
- ✅ Estado disabled se ve diferente (color gris + contenido apagado).
- ✅ Estado focused es visible (borde/outline primary).

## 📎 Evidencia

- Captura del botón "Iniciar sesión" en estado normal.
- Captura del botón durante carga (spinner + texto "Cargando…").
- Captura del botón deshabilitado (gris) con campos vacíos.

## ⏭️ Siguiente

**T21 – Captura de Evidencias + métricas SUS/Task Success.**