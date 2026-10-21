# T17 – Iconografía 100% vectorial + cuadrícula 24dp

> **Bloque auditado**: I – Ítem 07 (Iconografía vectorial)
> **Técnica *Pragmatic Programmer***: *Code is the design* (las imágenes son código)
> **Patrón Google**: **Material Symbols** + **VectorDrawable** + **AndroidX Vector Drawable**

## 🎯 Objetivo

Hoy hay:
- `ic_cube.xml` (vectorial) ✅
- `ic_launcher_*.webp` (raster, OK para launcher icon)
- `ui/img/3d-cube.png` (raster, usado en alguna parte) ❌
- `cube.png` en `res/drawable/` (raster) ❌

Hay que:
1. Reemplazar todos los íconos raster por vectoriales.
2. Usar Material Symbols (extendido) en todos los Composables.
3. Mantener tamaño 24dp×24dp estándar.

## ✅ Implementación

### 1. Buscar y reemplazar PNGs

```bash
find app/src -name "*.png" -o -name "*.webp" -o -name "*.jpg"
```

| Archivo | Acción |
|---------|--------|
| `ui/img/3d-cube.png` | Reemplazar por `Icons.Outlined.Inventory2` de Material Symbols |
| `res/drawable/cube.png` | Eliminar (solo se usa en LoginScreen) |
| `res/mipmap-*/ic_launcher.webp` | Mantener (launcher icon puede ser raster o adaptativo) |

### 3. Crear vector drawables custom (si hace falta un logo propio)

`res/drawable/ic_vanvault_logo.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp"
    android:viewportWidth="24" android:viewportHeight="24"
    android:tint="?attr/colorOnPrimary">
    <path android:fillColor="@android:color/white"
        android:pathData="M12,2 L4,7 L4,17 L12,22 L20,17 L20,7 Z M12,4.3 L18.5,8 L18.5,16 L12,19.7 L5.5,16 L5.5,8 Z"/>
</vector>
```

### 4. Usar siempre Material Symbols (extended)

```kotlin
// ❌ Antes
Icon(painter = painterResource(id = R.drawable.cube), ...)

// ✅ Ahora
Icon(imageVector = Icons.Filled.Inventory2, contentDescription = "Inventario")
```

### 5. Reemplazar en LoginScreen

```kotlin
// ❌ Antes
Icon(
    painter = painterResource(id = R.drawable.ic_cube),
    contentDescription = "VanVault Logo",
    ...
)

// ✅ Ahora
Icon(
    imageVector = Icons.Outlined.Lock,
    contentDescription = null, // texto "VanVault" está abajo
    ...
)
```

## 🧪 Criterio de aceptación

- ✅ `find app/src -name "*.png"` solo devuelve los iconos de launcher.
- ✅ Todos los `Icon(...)` en Composables usan `imageVector` o `painterResource(R.drawable.ic_*_vector)`.
- ✅ Todos los vector drawables tienen `width=24dp height=24dp`.

## 📎 Evidencia

- Captura de pantalla del LoginScreen con el ícono vectorial.
- Captura del proyecto en Android Studio mostrando solo `.xml` en `res/drawable/`.

## ⏭️ Siguiente

**T18 – Retícula de 8dp y Edge-to-Edge completo.**