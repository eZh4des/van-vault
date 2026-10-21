# T19 – Orden de foco y Foco Visible No Obstruido

> **Bloque auditado**: III – Ítem 20 (Orden lógico de foco y foco visible)
> **Técnica *Pragmatic Programmer***: *Design by Contract* (el foco es un contrato con el usuario)
> **Patrón Google**: **Modifier.focusOrder()** + **Modifier.focusRequester()**

## 🎯 Objetivo

WCAG 2.4.3 (Focus Order) y 2.4.11 (Focus Not Obscured) requieren:
1. El foco sigue el orden visual lógico (arriba → abajo, izq → der).
2. El indicador de foco siempre es visible (no oculto tras barras).

En Compose, el orden de foco es automático si se usa `Column`/`Row` correctamente.
Pero hay que asegurar:
- `Modifier.focusable()` solo donde haga falta.
- `focusRequester` para ordenar elementos custom.
- Indicador de foco visible (M3 lo da por defecto en `OutlinedTextField`, `Button`).

## ✅ Implementación

### 1. Revisar formularios: el orden debe ser Tab-friendly

En `AddProductScreen`, el orden actual es:
1. Nombre → 2. Modelo/Barcode → 3. Cantidad → 4. Precio → 5. Guardar.

Este orden ya es lógico. Verificar con un teclado externo.

### 2. Asegurar que cada elemento focuseable tiene foco visible

```kotlin
// Por defecto, los OutlinedTextField M3 tienen borde de foco (primary).
// Verificar en AddProductScreen que el color del borde enfocado es MaterialTheme.colorScheme.primary.
```

### 3. Para Cards clickeables en InventoryScreen

```kotlin
Card(
    modifier = Modifier
        .clickable(onClick = { /* detalle producto */ })
        .focusable()  // hacer focuseable también
) {
    // contenido
}
```

### 4. Evitar foco oculto tras TopAppBar

El TopAppBar no debe interceptar el foco de los items del BottomNavigationBar.
En M3 ya está resuelto (el `BottomBar` está fuera del contenido del Scaffold).

### 5. Indicador de foco explícito (opcional pero gana puntos)

```kotlin
// Para los NavigationBarItem, el indicador ya es el fondo selected.
// Para los SettingsOption, añadir ripple:

Surface(
    modifier = Modifier
        .clickable(onClick = onClick)
        .focusable()
        .border(
            width = if (isFocused) 2.dp else 0.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(16.dp)
        ),
    ...
)
```

## 🧪 Criterio de aceptación

- ✅ Con teclado externo conectado, Tab recorre los elementos en orden visual.
- ✅ El indicador de foco es siempre visible (no tapado por el BottomBar).
- ✅ TalkBack sigue el mismo orden.

## 📎 Evidencia

- Captura de InventoryScreen con el segundo item de la lista enfocado (indicador visible).
- Captura de AddProductScreen con campo "Cantidad" enfocado tras Tab desde "Modelo".

## ⏭️ Siguiente

**T20 – Estados visuales (Interaction State Layers).**