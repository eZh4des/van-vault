# VanVault

**VanVault** es una aplicación para Android desarrollada en **Kotlin** que utiliza principios modernos de desarrollo.

## 🛠️ Tecnologías y Herramientas

El proyecto está estructurado como una aplicación Android estándar y hace uso de las siguientes tecnologías:

*   **Lenguaje:** Kotlin
*   **Sistema de Construcción:** Gradle (Kotlin DSL - `build.gradle.kts`)
*   **Interfaz de Usuario (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose) (utilizando componentes de Material Design 3)
*   **Base de Datos:** Firebase Realtime Database
*   **SDK Objetivo:** Android API 37 (Min API 29)

## 📂 Estructura del Proyecto

*   `app/`: Módulo principal de la aplicación Android.
    *   `src/main/java/com/example/vanvault/`: Código fuente en Kotlin (ej. `MainActivity.kt`, carpeta `ui/`).
    *   `src/main/res/`: Recursos de la aplicación (imágenes, strings, etc.).
    *   `build.gradle.kts`: Configuración de dependencias específicas del módulo de la app (Compose, Firebase, etc.).
*   `build.gradle.kts` (raíz): Configuración a nivel de proyecto (plugins).
*   `settings.gradle.kts`: Definición del proyecto y repositorios de dependencias.

## 🚀 Requisitos Previos

*   Android Studio (versión reciente compatible con Compose y Gradle en Kotlin DSL).
*   JDK 11 (configurado en `sourceCompatibility` y `targetCompatibility`).
*   (Opcional pero recomendado) Configuración de Firebase, requiriendo el archivo `google-services.json` en el directorio `app/` si se van a utilizar los servicios de Firebase de forma completa.
