# Plan de Auditoría UI/UX & Accesibilidad Android – VanVault

> Documento maestro de cumplimiento de los **20 puntos** del checklist oficial (UPC – Seccional Aguachica, Moviles-II).
> Cada ítem está vinculado a una o más tareas pequeñas (`T##`) que se ejecutan en orden sugerido.

---

## 0. Resumen ejecutivo

| Bloque | Ítems | Puntos | Tareas asociadas |
|--------|-------|--------|------------------|
| **I. Componentes UI (Material Design 3)** | 01 – 07 | 35 | T01, T03, T04, T05, T13, T17, T18, T20 |
| **II. Métricas UX & Rendimiento** | 08 – 14 | 35 | T14, T15, T12, T07, T08, T16 |
| **III. Accesibilidad Universal (WCAG 2.2 AA)** | 15 – 20 | 30 | T10, T04, T11, T09, T19 |

**Meta**: puntaje **≥ 90 / 100 → Nivel Sobresaliente** (lo que requiere *Cumple = 5 pts* en todos los ítems).

---

## 1. Stack de técnicas (por qué cada una cuenta)

### 1.1 Patrones de Google (Architecture)
- **Capas UI / Domain / Data** (no solo UI/Data como está hoy)
- **Repository Pattern** con interfaces en `domain/` e implementación en `data/`
- **MVVM + StateFlow** para UDF (flujo de datos unidireccional)
- **UseCases / Interactors** para encapsular reglas de negocio reutilizables
- **Hilt** para DI (inyección de dependencias) — incluido desde el inicio
- **Single Source of Truth (SSOT)** en repositorios
- **Offline-first** con DataStore para preferencias

### 1.2 Técnicas del *Pragmatic Programmer* aplicadas
| Técnica | Aplicación concreta |
|---------|---------------------|
| **DRY (Don't Repeat Yourself)** | Extraer `OutlinedTextField` configurado, `LoadingButton`, `EmptyState`, `ErrorMessage`, todos a `ui/components/` |
| **Orthogonality** | Pantallas no se acoplan a Firebase directo; van por ViewModel → UseCase → Repository → DataSource |
| **Tracer Bullets** | Cada tarea entrega un flujo vertical funcional (UI → VM → Repository → Firebase) verificable |
| **Design by Contract** | Sealed classes (`ProductSearch`, `ProductLookup`, `UiState`) como contratos entre capas |
| **Reversibility** | Interfaces en `domain/` permiten cambiar Firebase por Room/Retrofit sin tocar UI |
| **Don't Live With Broken Windows** | Eliminar `Color(0xFFDC2626)` hard-coded, refactorizar estados en Composables |
| **Decoupling / Law of Demeter** | ViewModels exponen solo `StateFlow<UiState>`, no Firebase ni Context |
| **Defensive Programming** | Validaciones tempranas en repositorios (`search(text).takeIf { it.isNotBlank() }`) |
| **Coding Close to the Problem Domain** | Lenguaje del dominio: `Product.stockStatus`, `OutOfStock`, `LowStock`, `Available` |

### 1.3 Patrones de diseño clásicos aplicados
- **Repository** → desacopla fuente de datos
- **Strategy** → `ProductSearchStrategy` para tokenización e índices
- **Observer (Flow/StateFlow)** → UDF
- **Factory** → `ViewModelProvider.Factory` (y luego `@HiltViewModel`)
- **Adapter** → ya existe `BottomNavBar`; consolidarlo como `NavigationSuiteScaffold`
- **Decorator** → `EmptyState`, `LoadingState`, `ErrorState` envuelven cualquier lista
- **Composite** → `VanVaultScaffold` que combina TopBar + NavBar + FAB

---

## 2. Estado actual vs requerido (resumen)

| # | Ítem auditoría | Estado actual | Riesgo | Tarea principal |
|---|----------------|---------------|--------|-----------------|
| 01 | Navegación nativa (Bottom Nav / Drawer) | **Parcial** – `BottomNavBar.kt` existe pero solo 4 destinos, sin drawer ni Adaptive | Parcial | T13 |
| 02 | Retícula 8dp & Edge-to-Edge | **No cumple** – paddings arbitrarios (`80.dp`, `24.dp`), sin retícula consistente | No cumple | T18 |
| 03 | Jerarquía tipográfica en `sp` | **No cumple** – `Type.kt` solo define `bodyLarge`, usa `FontSize` fijos en muchos sitios | No cumple | T04 |
| 04 | Roles semánticos color + Dark Theme | **Parcial** – `Theme.kt` define roles, pero dark mode incompleto y se rompen colores hardcoded (`Color(0xFFDC2626)`) | Parcial | T03 + T05 |
| 05 | Estados visuales (Ripple, Focus, Disabled) | **No cumple** – usa `Color.Transparent` en containers; sin estados pressed/focused explícitos | No cumple | T20 |
| 06 | Formularios M3 (Floating Labels, inputType) | **Parcial** – usa `OutlinedTextField`, pero sin `keyboardOptions` ni `imeOptions` coherentes; validación pobre | Parcial | T06 + T15 |
| 07 | Iconografía vectorial | **Parcial** – usa Material Icons (vectoriales), pero hay `ic_cube.png` (raster) y `3d-cube.png` | Parcial | T17 |
| 08 | Task Success Rate ≥ 85% | **Sin medir** – no hay telemetría ni pruebas de usuario | No cumple | T21 |
| 09 | Time on Task / Eficiencia | **Sin medir** – flujo de "Agregar producto" requiere ~6 toques | No cumple | T21 + T14 |
| 10 | Errores y validación | **No cumple** – usa `Toast` en vez de mensajes inline; sin preservación de datos en error | No cumple | T15 |
| 11 | Latencia <100ms + indicadores | **Parcial** – usa `CircularProgressIndicator`, pero sin skeletons ni LinearProgressIndicator | Parcial | T12 |
| 12 | Empty States + Offline | **Parcial** – existe "No hay productos", sin icono ni acción; sin manejo offline | Parcial | T07 |
| 13 | Confirmación acciones destructivas | **No cumple** – `Eliminar producto` no hace nada; logout sin confirmación | No cumple | T08 |
| 14 | SUS ≥ 68 | **Sin medir** | No cumple | T21 |
| 15 | Touch targets ≥ 48dp | **No cumple** – `FilledIconButton` de 56dp ok, pero `IconButton` puede ser < 48dp en algunos lugares | No cumple | T10 |
| 16 | Contraste 4.5:1 | **Parcial** – depende de la combinación; red sobre white ~5.9:1 OK, pero revisar texto secundario | Parcial | T03 + verificación |
| 17 | contentDescription / TalkBack | **Parcial** – muchos `Icon` tienen `contentDescription = null` o genérico | Parcial | T09 |
| 18 | Escalado 200% + Reflow | **Sin verificar** – paddings fijos en dp pueden romper | No cumple | T11 |
| 19 | Color + alternativa a gestos | **Parcial** – labels de stock usan color + texto (bien), pero logout sin alternativa | Parcial | T08 |
| 20 | Foco lógico y foco visible | **Sin medidas** – sin `Modifier.focusRequester` ni `focusOrder` | No cumple | T19 |

---

## 3. Cómo ejecutar las tareas

1. **Lee la `T##-nombre.md`** correspondiente.
2. Cada tarea termina con un **criterio de aceptación** (qué evidencia adjuntar al checklist).
3. Las tareas son **vertical slices** (Tracer Bullets): UI → ViewModel → UseCase → Repository → DataSource.
4. Tras completar una tarea, marca la casilla en este README y captura la pantalla (ver `T21`).
5. Antes de cada commit, ejecuta `./gradlew assembleDebug` para evitar regresiones.

---

## 4. Orden sugerido de ejecución

```
T01 (Arquitectura Domain) → T02 (Hilt) → T03 (Color roles) → T04 (Type scale) → T05 (Dark theme)
   → T06 (Formularios M3) → T07 (Empty states) → T08 (Confirmaciones) → T09 (A11y labels)
   → T10 (Touch targets) → T11 (Reflow 200%) → T12 (Loading) → T13 (Adaptive nav)
   → T14 (Flow efficiency) → T15 (Validación) → T16 (Snackbar Undo) → T17 (Iconografía)
   → T18 (Grid 8dp) → T19 (Focus order) → T20 (Estados visuales) → T21 (Evidencias)
```

---

## 5. Próximo paso

Ejecuta la primera tarea: **[T01 - Arquitectura: Capa de Domain](./tasks/T01-arquitectura-capa-domain.md)**