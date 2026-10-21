# T14 – Eficiencia de flujo (Time on Task)

> **Bloque auditado**: II – Ítem 09 (Time on Task)
> **Técnica *Pragmatic Programmer***: *Tracer Bullets* (medir lo que haces)
> **Patrón Google**: **Baseline Profiles** + **Firebase Performance Monitoring**

## 🎯 Objetivo

El flujo principal de VanVault es **"Agregar producto al inventario"**.
Actualmente:
- Inicio → Bottom Nav → FAB (+) → "Agregar producto" → Llenar 4 campos → "Guardar" → Back al inventario.
- Total aprox. **6 interacciones + 4 campos**.

Objetivo: **reducir toques redundantes y mejorar feedback**.

## ✅ Mejoras concretas

### 1. Atajo directo desde InventoryScreen

- Long-press en FAB → "Escanear y agregar" (atajo combinado).

### 2. Auto-focus y `imeOptions=actionNext`

(Ya en T06). Esto reduce toques del teclado virtual.

### 3. Auto-completar campos derivados

- Si el usuario escanea un código de barras, autollenar `modelOrBarcode` (ya existe vía Scanner).
- Si el producto ya existe, mostrar diálogo "¿Sumar a cantidad existente?" en vez de error.

### 4. Persistencia de borrador (DRY + UX)

Si el usuario cierra AddProductScreen a mitad, **no perder lo escrito**:

```kotlin
// ui/screens/AddProductScreen.kt
LaunchedEffect(name, modelOrBarcode, quantity, unitPrice) {
    // Guardar en SavedStateHandle del NavController
    navController.currentBackStackEntry?.savedStateHandle?.apply {
        set("draft_name", name)
        set("draft_model", modelOrBarcode)
        set("draft_quantity", quantity)
        set("draft_price", unitPrice)
    }
}
```

```kotlin
// Restaurar al abrir:
val draftName = navController.currentBackStackEntry?.savedStateHandle?.get<String>("draft_name") ?: ""
// ...
var name by remember { mutableStateOf(draftName) }
```

### 5. Medir tiempo (Tracer Bullet)

Añadir `Firebase Performance Monitoring` o simple `System.currentTimeMillis()`:

```kotlin
val startTime = remember { System.currentTimeMillis() }
Button(onClick = {
    val duration = System.currentTimeMillis() - startTime
    analytics.logEvent("product_add_completed", bundleOf("duration_ms" to duration))
    // ...
})
```

## 🧪 Criterio de aceptación

- ✅ Flujo "Agregar producto" completa en ≤ 5 toques visibles + 4 campos (medible).
- ✅ Si el usuario abandona la pantalla y vuelve, los campos están pre-llenados.
- ✅ Existe un evento/log con el tiempo de la tarea para análisis posterior.

## 📎 Evidencia

- Captura del flujo completo: Home → FAB → Add → Llenar → Guardar → Regresar a Home (con tiempo en segundos).
- Captura de Firebase Analytics (o logcat) mostrando el evento `product_add_completed`.

## ⏭️ Siguiente

**T15 – Validación inline (Errores con significado, Nielsen #9).**