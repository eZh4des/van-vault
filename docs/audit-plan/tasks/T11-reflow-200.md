# T11 – Reflow al 200% de tamaño de fuente (WCAG 1.4.4 / 1.4.10)

> **Bloque auditado**: III – Ítem 18 (Escalado dinámico 200%)
> **Técnica *Pragmatic Programmer***: *Easier to Change* (no hay que recompilar)
> **Patrón Google**: **`sp` units** + **WindowSizeClass** para layouts adaptativos

## 🎯 Objetivo

Cuando el usuario configura el tamaño de fuente del sistema al máximo (200% en Settings → Display),
toda la UI debe escalar sin recortar palabras ni superponer botones.

Esto se logra con:
1. **Tipografía en `sp`** (ya cubierto en T04).
2. **Layouts vertical-scrollables** en lugar de horizontal-fijos.
3. **Evitar tamaños fijos en `dp`** en contenedores de texto.

## ✅ Auditoría: ¿qué está en `dp` y debería ser flexible?

```bash
grep -rn "Modifier.height" app/src/main/java/com/vanvault/android/ui/
grep -rn "Modifier.size" app/src/main/java/com/vanvault/android/ui/
```

| Caso | Problema | Solución |
|------|----------|----------|
| `Modifier.height(56.dp)` en LoginScreen para `OutlinedTextField` | Puede cortar texto a 200% | Usar `OutlinedTextField` sin altura fija, dejar que crezca con el contenido |
| `Spacer(modifier = Modifier.height(80.dp))` en LoginScreen | Moverá el contenido hacia abajo | Convertir a padding relativo |
| `Modifier.size(64.dp)` para logo | OK (no es texto) | — |
| `BottomNav` labels | M3 los adapta automáticamente | OK |

## ✅ Implementación

### 1. Eliminar `height(...)` y `size(...)` fijos en campos de texto

```kotlin
// ❌ Antes
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    modifier = Modifier.fillMaxWidth()
)

// ✅ Ahora (sin height fijo)
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    modifier = Modifier.fillMaxWidth()
)
```

### 2. Reemplazar `Spacer(Modifier.height(N.dp))` por `Spacer(Modifier.padding(...))`

O mejor, usar `Arrangement.spacedBy()` en el Column padre.

### 3. Hacer que el contenido principal sea scrollable

```kotlin
// LoginScreen.kt - envolver Column en verticalScroll
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    // ... todos los elementos
}
```

### 4. Validar visualmente

1. Settings → Display → Font size → Largest.
2. Abrir todas las pantallas.
3. Verificar que:
   - Los textos no se cortan.
   - Los botones no se solapan.
   - El contenido es scrollable.

### 5. Para la Card de inventario, usar `FlowRow` en lugar de `Row` (ítems múltiples)

```kotlin
@OptIn(ExperimentalLayoutApi::class)
FlowRow(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    Column { Text("DISPONIBLE"); Text(quantity) }
    Column(horizontalAlignment = Alignment.End) { Text("PRECIO"); Text(price) }
}
```

`FlowRow` reordena automáticamente cuando no cabe.

## 🧪 Criterio de aceptación

- ✅ Con tamaño de fuente al máximo (200%) y todas las pantallas: ningún texto recortado, ningún botón solapado.
- ✅ Pantallas con scroll vertical permiten alcanzar todo el contenido.
- ✅ `OutlinedTextField` no tienen altura fija en dp.

## 📎 Evidencia

- Captura de LoginScreen a 100% y 200% mostrando que todo es accesible.
- Captura de InventoryScreen a 200% mostrando que las Cards de producto se reorganizan correctamente.

## ⏭️ Siguiente

**T12 – Skeletons y LinearProgressIndicator para carga.**