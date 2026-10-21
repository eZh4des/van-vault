# T09 – `contentDescription` semántico para TalkBack

> **Bloque auditado**: III – Ítem 17 (Etiquetado semántico para TalkBack)
> **Técnica *Pragmatic Programmer***: *Code Close to the Problem Domain* (anunciar el dominio, no la UI)
> **Patrón Google**: **WCAG 1.1.1 / 4.1.2** — `contentDescription` + `Modifier.semantics`

## 🎯 Objetivo

Hoy muchos `Icon(...)` tienen `contentDescription = null` o un literal genérico
que TalkBack lee como "icon". Hay que asegurar:
1. Iconos informativos: `contentDescription` claro.
2. Iconos decorativos: `null` o `Modifier.clearAndSetSemantics { }`.
3. Botones con texto: redundar descripción para no perder contexto.

## ✅ Auditoría actual

```bash
grep -rn "contentDescription" app/src/main/java/com/vanvault/android/ui/
```

Reemplazos necesarios:

| Archivo / Línea | Actual | Nuevo |
|----------------|--------|-------|
| `LoginScreen.kt:67` | `"VanVault Logo"` | `null` (decorativo, hay texto "VanVault" abajo) |
| `BottomNavBar.kt:69` | `"Inicio"` | OK |
| `BottomNavBar.kt:82` | `"Inventario"` | OK |
| `BottomNavBar.kt:94` | `"Ventas"` | OK |
| `BottomNavBar.kt:107` | `"Perfil"` | OK |
| `InventoryScreen.kt:131` | `"Cerrar búsqueda"` | OK (acción) |
| `InventoryScreen.kt:146` | `"Buscar"` | OK |
| `InventoryScreen.kt:194` | `"Opciones"` | Mejorar → "Mostrar más opciones" |
| `AddProductScreen.kt:62` | `"Regresar"` | OK pero añadir semantics { contentDescription = "Volver a la pantalla anterior" } |
| `AddProductScreen.kt:116` | `"Escanear"` | Mejorar → "Escanear código de barras del producto" |
| `SettingsOption.kt` Icon | `contentDescription = title` | **Repetido** — quitar el icono (decorativo) ya que el texto lo dice |

## ✅ Implementación

### 1. Para iconos decorativos (sin valor informativo)

```kotlin
Icon(
    imageVector = Icons.Default.Add,
    contentDescription = null,  // ya correcto
    modifier = Modifier.size(28.dp)
)
```

### 2. Para iconos informativos únicos (sin texto cercano)

```kotlin
IconButton(onClick = { /* ... */ }) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Volver"  // corto y claro
    )
}
```

### 3. Para botones con texto + icono (NO describir el icono)

```kotlin
Button(onClick = { /* ... */ }) {
    Icon(Icons.Filled.Add, contentDescription = null)
    Spacer(Modifier.width(8.dp))
    Text("Agregar producto")
}
```

### 4. Para Cards con información compleja → usar `Modifier.semantics(mergeDescendants = true)`

```kotlin
Card(
    modifier = Modifier
        .fillMaxWidth()
        .clickable { /* ... */ }
        .semantics(mergeDescendants = true) {
            contentDescription = buildString {
                append("Producto ${product.name}, ")
                append("SKU ${product.modelOrBarcode}, ")
                append("${product.quantity} unidades, ")
                append("estado ${product.stockStatus.labelKey}, ")
                append("precio ${product.unitPrice.toDisplayString()}")
            }
        }
) { /* ... */ }
```

> ⚠️ **Importante**: NO concatenar información técnica como "Card de Tap / Stroke / Row" — TalkBack ya sabe qué es un Card.

### 5. Para estados dinámicos (sin conexión, cargando)

```kotlin
Modifier.semantics {
    stateDescription = if (isLoading) "Cargando" else if (isError) "Error de carga" else "Carga completa"
}
```

## 🧪 Criterio de aceptación

- ✅ `grep -r "contentDescription = null"` solo aparece en iconos puramente decorativos (verificado manualmente).
- ✅ Activar TalkBack y navegar por la app: todos los botones anuncian su acción.
- ✅ Imágenes sin valor informativo (logo, divisores) NO se anuncian.
- ✅ Las Cards de inventario anuncian "Producto X, SKU Y, Z unidades, precio $W".

## 📎 Evidencia

- Captura de pantalla del dispositivo con TalkBack activado.
- Video corto (o serie de capturas) navegando por LoginScreen, InventoryScreen, AddProductScreen.

## ⏭️ Siguiente

**T10 – Touch targets ≥ 48dp (WCAG 2.5.8).**