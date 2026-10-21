# T05 – Dark Theme completo y persistente

> **Bloque auditado**: I – Ítem 04 (Modo oscuro completo)
> **Técnica *Pragmatic Programmer***: *Don't Live With Broken Windows* + *Persistence*
> **Patrón Google**: **DataStore** + **M3 Dynamic Color (opcional)**

## 🎯 Objetivo

1. El Dark Mode ya está parcialmente configurado en `Theme.kt` pero:
   - No se aplica de verdad en Composables que pasan `Color.Transparent`.
   - El usuario debe poder cambiarlo manualmente desde `AppearanceScreen`.
2. Hay que **persistir** la preferencia del usuario (DataStore ya existe, mejorarlo).
3. Validar visualmente que NINGÚN texto se vuelve ilegible en Dark.

## ✅ Implementación

### 1. Eliminar todos los `Color.Transparent` usados como fondo

`Color.Transparent` para `containerColor` en `TopAppBar` o `Scaffold` rompe el Dark Theme.
Reemplazar por `MaterialTheme.colorScheme.background` o `surface`.

```bash
grep -rn "Color.Transparent" app/src/main/java/com/vanvault/android/ui/
```

- `TopAppBar(... containerColor = Color.Transparent)` → `MaterialTheme.colorScheme.background`
- `TextFieldDefaults.colors(focusedContainerColor = Color.Transparent)` → `MaterialTheme.colorScheme.surface`
- Mismas correcciones para los `indicatorColor`.

### 2. Validar `AppearanceScreen` y la persistencia

Ya hay `SettingsViewModel` + `SettingsPreferences`. Verificar:
- Que `settings.isDarkMode` se persiste en DataStore (relanzar app y comprobar).
- Que el cambio aplica **sin necesidad de relanzar** la app (recomposition inmediata).

### 3. Crear `ui/theme/DynamicColor.kt` (opcional pero puntúa extra)

```kotlin
package com.vanvault.android.ui.theme

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun supportsDynamicColor(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun dynamicLightColor() = dynamicLightColorScheme(LocalContext.current)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun dynamicDarkColor() = dynamicDarkColorScheme(LocalContext.current)
```

Y en `Theme.kt`, añadir un parámetro `dynamicColor: Boolean = false` que si es `true` y SDK >= S, use los colores dinámicos del sistema (Android 12+).

### 4. Auditar cada pantalla en modo Dark

| Pantalla | Estado actual | Corrección |
|----------|---------------|------------|
| LoginScreen | Texto blanco sobre rojo OK; revisar outlined | Asegurar que usa `MaterialTheme.colorScheme.surface` |
| InventoryScreen | TopAppBar `Color.Transparent` ❌ | Usar `MaterialTheme.colorScheme.background` |
| AddProductScreen | Botón `Color(0xFFDC2626)` ❌ | Usar `MaterialTheme.colorScheme.primary` |
| ProfileScreen | SettingsOption con `BorderStroke(Color(0xFFE4E4E7))` ❌ | Usar `MaterialTheme.colorScheme.outline` |
| FAB Option | `color = MaterialTheme.colorScheme.primary` | OK |

## 🧪 Criterio de aceptación

- ✅ El toggle de Apariencia cambia entre light y dark sin relanzar.
- ✅ Al reiniciar la app, la preferencia persiste.
- ✅ Ningún texto tiene contraste < 4.5:1 en Dark (verificable con Accessibility Scanner).
- ✅ Captura de pantalla del sistema en ambas modalidades sin warnings visuales.

## 📎 Evidencia

- Captura lado a lado de la misma pantalla en Light vs Dark (LoginScreen + InventoryScreen).
- Captura de Accessibility Scanner en Dark mode con 0 errores de contraste.

## ⏭️ Siguiente

**T06 – Formularios M3 (Floating Labels + inputType + imeOptions).**