# T00 – Resumen y Checklist de los 20 Ítems

> Esta tarea NO es de código. Es la **rúbrica final** que debes marcar tras completar las demás.

---

## 📋 Checklist por Ítem

### Bloque I – Componentes UI (Material Design 3) — 35 pts

| Ítem | Título | Tareas | Cumple (C/CP/NC) | Evidencia # |
|------|--------|--------|------------------|-------------|
| 01 | Arquitectura de Navegación Nativa | T13 | ☐ | `01-bottom-nav.png` |
| 02 | Retícula Espacial y Zonas Seguras (8dp) | T18 | ☐ | `02-grid-8dp-bounds.png` |
| 03 | Jerarquía Tipográfica y Unidades Escalables (sp) | T04 | ☐ | `03-type-scale-sp.png` |
| 04 | Roles Semánticos de Color y Dark Theme | T03, T05 | ☐ | `04-light-vs-dark.png` |
| 05 | Estados Visuales de Componentes Interactivos | T20 | ☐ | `05-loading-button-states.png` |
| 06 | Formularios M3 (inputType, imeOptions) | T06 | ☐ | `06-floating-labels.png` |
| 07 | Consistencia Iconográfica Vectorial | T17 | ☐ | `07-vector-icons.png` |

### Bloque II – Métricas UX y Rendimiento — 35 pts

| Ítem | Título | Tareas | Cumple | Evidencia |
|------|--------|--------|--------|-----------|
| 08 | Task Success Rate ≥ 85% | T21 | ☐ | `08-task-success.xlsx` |
| 09 | Time on Task | T14, T21 | ☐ | `09-time-on-task.xlsx` |
| 10 | Tasa de Error y Recuperación (Nielsen #9) | T15 | ☐ | `10-validacion-inline.png` |
| 11 | Latencia < 100ms + Indicadores | T12 | ☐ | `11-linear-progress.png` |
| 12 | Empty States + Modo Sin Conexión | T07 | ☐ | `12-empty-state.png` + `12-offline.png` |
| 13 | Confirmación Acciones Destructivas (Nielsen #3, #5) | T08, T16 | ☐ | `13-confirmation-dialog.png` + `13-undo.png` |
| 14 | SUS ≥ 68 | T21 | ☐ | `14-sus.xlsx` |

### Bloque III – Accesibilidad Universal (WCAG 2.2 AA) — 30 pts

| Ítem | Título | Tareas | Cumple | Evidencia |
|------|--------|--------|--------|-----------|
| 15 | Touch Target ≥ 48dp (WCAG 2.5.8) | T10 | ☐ | `15-touch-targets.png` |
| 16 | Contraste 4.5:1 (WCAG 1.4.3 / 1.4.11) | T03 | ☐ | `16-contrast-scanner.png` |
| 17 | contentDescription + TalkBack (WCAG 1.1.1) | T09 | ☐ | `17-talkback-walkthrough.mp4` |
| 18 | Reflow al 200% (WCAG 1.4.4 / 1.4.10) | T11 | ☐ | `18-reflow-200.png` |
| 19 | Color + Alternativa a Gestos (WCAG 1.4.1, 2.5.1) | T08 | ☐ | `19-icons-semanticos.png` |
| 20 | Orden Lógico de Foco (WCAG 2.4.3, 2.4.11) | T19 | ☐ | `20-focus-order.png` |

---

## 🎯 Meta: 90+ pts = Sobresaliente

Para alcanzar Sobresaliente necesitas **20 ítems en Cumple = 100 pts**.
Si aceptas 1-2 en Parcial (2.5 pts), tu techo será **95-97.5** (Sobresaliente sigue).
Si tienes 3+ en Parcial o 1+ en No Cumple, baja a Aceptable (70-89).

---

## ✅ Verificación final

```bash
# 1. Compilación
./gradlew assembleDebug

# 2. Pruebas unitarias (si tienes)
./gradlew test

# 3. Lint
./gradlew lintDebug

# 4. Build release para entrega
./gradlew bundleRelease
```

Carga cada captura de la carpeta `evidencias/` en el HTML de auditoría,
marca C/CP/NC en cada ítem, y exporta el PDF.