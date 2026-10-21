# T15 – Validación inline con significado (Nielsen #9)

> **Bloque auditado**: II – Ítem 10 (Tasa de error y recuperación)
> **Técnica *Pragmatic Programmer***: *Design by Contract* + *Crash Early*
> **Patrón Google**: **M3 TextField supportingText** + **Form-level error state**

## 🎯 Objetivo

Hoy la validación es:
```kotlin
if (name.isBlank() || modelOrBarcode.isBlank() || quantity.isBlank() || unitPrice.isBlank()) {
    Toast.makeText(context, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
    return@Button
}
```

Esto viola Nielsen #9 ("Help users recognize, diagnose, and recover from errors"):
1. No dice CUÁL campo falta.
2. Pierde los datos del usuario (al volver el form está limpio).
3. Toast se desvanece.

Vamos a:
1. Validar **por campo**, en `supportingText`.
2. Usar `LaunchedEffect` para no perder los datos en caso de error.
3. Ofrecer **sugerencia** ("Ingresa un número entero positivo").

## ✅ Implementación

### 1. Crear validadores reutilizables en `domain/validation/Validators.kt`

```kotlin
package com.vanvault.android.domain.validation

/**
 * Validador = función pura que retorna null si OK o mensaje si falla.
 * Cumple Design by Contract: tipo de retorno (String?) hace explícito el contrato.
 */
typealias Validator<T> = (T) -> String?

object ProductValidators {
    val requiredText: Validator<String> = {
        if (it.isBlank()) "Este campo es obligatorio" else null
    }

    val positiveInt: Validator<String> = {
        val n = it.toIntOrNull()
        when {
            it.isBlank() -> "Este campo es obligatorio"
            n == null -> "Ingresa un número entero válido"
            n < 0 -> "La cantidad no puede ser negativa"
            else -> null
        }
    }

    val positiveDouble: Validator<Double?> = {
        when {
            it == null -> "Este campo es obligatorio"
            it < 0.0 -> "El precio no puede ser negativo"
            it > 100_000_000.0 -> "El precio parece demasiado alto"
            else -> null
        }
    }

    val productName: Validator<String> = { name ->
        when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < 3 -> "Mínimo 3 caracteres"
            name.length > 100 -> "Máximo 100 caracteres"
            else -> null
        }
    }
}
```

### 2. Usar en `AddProductScreen`

```kotlin
@Composable
fun AddProductScreen(...) {
    var name by rememberSaveable { mutableStateOf("") }
    var quantity by rememberSaveable { mutableStateOf("") }
    var unitPrice by rememberSaveable { mutableStateOf("") }

    val nameError = remember(name) { ProductValidators.productName(name) }
    val qtyError = remember(quantity) { ProductValidators.positiveInt(quantity) }
    val priceError = remember(unitPrice) { ProductValidators.positiveDouble(unitPrice.toDoubleOrNull()) }

    M3TextField(
        value = name,
        onValueChange = { name = it },
        label = "Nombre del producto",
        isError = nameError != null,
        errorMessage = nameError
    )
    // ...
    Button(
        onClick = {
            if (nameError == null && qtyError == null && priceError == null) {
                viewModel.save(...)
            }
        },
        enabled = nameError == null && qtyError == null && priceError == null
    ) { Text("Guardar") }
}
```

### 3. Usar `rememberSaveable` para preservar datos en rotación o salida

```kotlin
var name by rememberSaveable { mutableStateOf("") }
```

`rememberSaveable` guarda el estado ante rotación, recreación de Activity y vuelta atrás (si el NavHost lo gestiona).

## 🧪 Criterio de aceptación

- ✅ Al enviar con campo vacío, ese campo muestra el error y los demás campos NO se borran.
- ✅ Cada error dice exactamente qué está mal y cómo solucionarlo.
- ✅ Rotar el dispositivo con datos en campos no los pierde.
- ✅ Presionar "atrás" y volver reabre el form con los datos.

## 📎 Evidencia

- Captura del form con un campo en rojo mostrando "La cantidad no puede ser negativa" tras intentar enviar.
- Captura del form con valores preservados después de rotar el dispositivo.

## ⏭️ Siguiente

**T16 – Snackbar Undo con SnackbarHost (extender T08 a todas las acciones).**