# T21 – Captura de Evidencias + Medición SUS / Task Success Rate

> **Bloque auditado**: II – Ítems 08 (Task Success), 09 (Time on Task), 14 (SUS)
> **Técnica *Pragmatic Programmer***: *Tracer Bullets* (mide lo que importa)
> **Patrón Google**: **Firebase Analytics** + **Baseline Profiles** (UX Research)

## 🎯 Objetivo

La auditoría te pide **evidencias** para cada uno de los 20 puntos.
Hay dos tipos:
1. **Visuales** (capturas de pantalla + vídeo).
2. **Cuantitativas** (métricas SUS, Task Success, Time on Task).

Esta tarea unifica todo en una carpeta `evidencias/` lista para entregar.

## ✅ Estructura de evidencias

```
evidencias/
├── bloque-i-ui/
│   ├── 01-bottom-nav.png
│   ├── 02-grid-8dp-bounds.png
│   ├── 03-type-scale-sp.png
│   ├── 04-light-vs-dark.png
│   ├── 05-loading-button-states.png
│   ├── 06-floating-labels-inputType.png
│   └── 07-vector-icons.png
├── bloque-ii-ux/
│   ├── 08-task-success-rate.xlsx
│   ├── 09-time-on-task.xlsx
│   ├── 10-validacion-inline.png
│   ├── 11-linear-progress.png
│   ├── 12-empty-state.png + offline.png
│   ├── 13-confirmation-dialog.png + undo-snackbar.png
│   └── 14-sus-questionnaire.xlsx
└── bloque-iii-a11y/
    ├── 15-touch-targets-layout-bounds.png
    ├── 16-contrast-accessibility-scanner.png
    ├── 17-talkback-walkthrough.mp4 (o capturas)
    ├── 18-reflow-200.png
    ├── 19-color-independent-icons.png
    └── 20-focus-order.png
```

## ✅ Implementación

### 1. Capturas de pantalla: protocolo

- **Modo**: Portrait 1080×2400 (Pixel 6 o equivalente).
- **Versión de Android**: API 33+ (API 35 recomendado).
- **Tamaño de fuente**: capturar en ambos 100% y 200%.
- **Dark/Light**: cada pantalla en ambos modos.

### 2. Para ítems cuantitativos (08, 09, 14): ejecutar pruebas con usuarios

#### Task Success Rate (ítem 08)

- Reclutar **3 usuarios de prueba** (colegas, familiares).
- Pedirles completar la tarea: "Agrega un producto nuevo llamado 'Laptop Lenovo' con cantidad 5 y precio 250000".
- Medir:
  - ¿Lo logró sin ayuda? (Sí/No)
  - Número de toques.
  - Errores iniciales.
- Calcular: `success_rate = usuarios_exitosos / total * 100`.
- Meta: ≥ 85%.

#### Time on Task (ítem 09)

- Cronómetro desde "abrir AddProduct" hasta "ver producto en lista".
- Meta: < 60 segundos.

#### SUS (ítem 14)

- Aplicar el cuestionario SUS de 10 items a cada uno:
  1. Pienso que me gustaría usar este sistema con frecuencia.
  2. Encontré el sistema innecesariamente complejo.
  3. Pienso que el sistema fue fácil de usar.
  4. Pienso que necesitaría ayuda de una persona técnica.
  5. Encontré las funciones bien integradas.
  6. Pienso que el sistema fue muy inconsistente.
  7. Imagino que la mayoría de personas aprendería a usarlo rápidamente.
  8. Encontré el sistema muy torpe de usar.
  9. Me sentí muy confiado con el sistema.
  10. Necesité aprender muchas cosas antes de usar el sistema.

- Calcular con la fórmula SUS estándar (0-100).
- Meta: ≥ 68.

### 3. Plantillas

#### `evidencias/bloque-ii-ux/task-success-rate.xlsx`

| Usuario | Tarea | Éxito | Toques | Errores | Tiempo (s) |
|---------|-------|-------|--------|---------|-----------|
| U1 | Agregar producto | Sí | 9 | 1 | 42 |
| U2 | Agregar producto | Sí | 8 | 0 | 38 |
| U3 | Agregar producto | No | 5 | 2 | 55 |
| **Total** | | **2/3 = 66.7%** | | | |

#### `evidencias/bloque-ii-ux/sus-questionnaire.xlsx`

| Usuario | Q1 | Q2 | Q3 | Q4 | Q5 | Q6 | Q7 | Q8 | Q9 | Q10 | Score |
|---------|----|----|----|----|----|----|----|----|----|----|----|
| U1 | 4 | 2 | 4 | 1 | 4 | 1 | 5 | 1 | 4 | 1 | 80 |
| U2 | 5 | 1 | 5 | 1 | 5 | 1 | 5 | 1 | 5 | 1 | 95 |
| **Promedio** | | | | | | | | | | | **87.5** |

### 4. Accesibilidad: Accessibility Scanner

- Instalar [Accessibility Scanner](https://play.google.com/store/apps/details?id=com.google.android.apps.accessibility.auditor).
- Recorrer cada pantalla.
- Capturar pantalla sin alertas.
- Si hay alertas, corregir y volver a capturar.

### 5. TalkBack walkthrough

- Settings → Accessibility → TalkBack → On.
- Conectar auriculares para audio.
- Grabar vídeo navegando por LoginScreen → InventoryScreen → AddProductScreen → ProfileScreen.
- O capturar 5-6 pantallas clave + comentar audio.

## 🧪 Criterio de aceptación

- ✅ Las 20 carpetas de evidencias contienen archivos (capturas + cuestionarios).
- ✅ Task Success Rate ≥ 85%.
- ✅ SUS promedio ≥ 68.
- ✅ Accessibility Scanner sin alertas críticas.

## 📎 Entrega final

1. `evidencias/` comprimida en `.zip`.
2. Llenar el HTML de auditoría con cada respuesta (C/CP/NC) y subir las evidencias.
3. Calcular puntaje esperado.

## 🎉 Checklist final pre-entrega

```bash
# 1. Compilar
./gradlew assembleDebug

# 2. Verificar tests
./gradlew test

# 3. Lint
./gradlew lintDebug

# 4. Generar APK firmado (release)
./gradlew bundleRelease
```

## ⏭️ Siguiente

**Has completado todas las tareas.** Ahora es momento de cargar las evidencias en el HTML de auditoría y entregar.