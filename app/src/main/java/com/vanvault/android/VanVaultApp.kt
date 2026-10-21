package com.vanvault.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — punto de entrada de Hilt.
 *
 * **Por qué existe**: Hilt usa esta anotación para generar el
 * `SingletonComponent` raíz que provee las dependencias a toda la app.
 *
 * Google Architecture: "Single source of truth" para las dependencias —
 * cualquier `Activity`, `ViewModel` o servicio con `@AndroidEntryPoint` /
 * `@HiltViewModel` puede acceder a las instancias inyectadas.
 */
@HiltAndroidApp
class VanVaultApp : Application()