# T02 – Configurar Hilt (Dependency Injection)

> **Bloque auditado**: I – Todos los ítems UI (permite ViewModels testeables)
> **Técnica *Pragmatic Programmer***: *Decoupling*, *Make It Easy to Reuse*
> **Patrón Google**: **Hilt** (oficial para Android)

## 🎯 Objetivo

Hilt es el contenedor de DI oficial de Google. Lo configuraremos para que:
1. Los `ViewModel` se inyecten en Composables con `hiltViewModel()`.
2. Los repositorios tengan `@Singleton` y sean fáciles de sustituir (mocks en tests).
3. Firebase (`FirebaseAuth`, `FirebaseDatabase`) se inyecte en una sola capa.

## ✅ Pasos concretos

### 1. Actualizar `gradle/libs.versions.toml`

```toml
[versions]
hilt = "2.51.1"
ksp = "2.0.21-1.0.27"

[libraries]
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version = "1.2.0" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

### 2. Aplicar plugins en `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    // ... resto
}
```

### 3. Crear `VanVaultApp.kt`

```kotlin
package com.vanvault.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VanVaultApp : Application()
```

### 4. Declarar en `AndroidManifest.xml`

```xml
<application
    android:name=".VanVaultApp"
    ... >
```

### 5. Crear `di/FirebaseModule.kt`

```kotlin
package com.vanvault.android.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
}
```

### 6. Crear `di/RepositoryModule.kt`

```kotlin
package com.vanvault.android.di

import com.vanvault.android.data.repository.ProductRepositoryImpl
import com.vanvault.android.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository
}
```

### 7. Inyectar dependencias en `ProductRepositoryImpl.kt`

```kotlin
package com.vanvault.android.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.vanvault.android.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : ProductRepository { /* ... */ }
```

### 8. Usar `@HiltViewModel` en futuros ViewModels

```kotlin
@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val observeInventory: ObserveInventoryUseCase,
    private val searchProducts: SearchProductsUseCase
) : ViewModel() { /* ... */ }
```

### 9. Eliminar las instanciaciones manuales en Composables

- Quitar `val repository = remember { ProductRepository() }` de `InventoryScreen.kt`.
- Reemplazar por `val viewModel: InventoryViewModel = hiltViewModel()`.

## 🧪 Criterio de aceptación

- ✅ `./gradlew assembleDebug` exitoso.
- ✅ `MainActivity` está anotada con `@AndroidEntryPoint`.
- ✅ `grep -r "FirebaseAuth.getInstance" app/src/main/java/com/vanvault/android/` solo aparece en `di/FirebaseModule.kt`.

## 📎 Evidencia

- Captura de la pantalla del Inspector de Hilt o del log de build mostrando que la inyección funciona.

## ⏭️ Siguiente

**T03 – Sistema de Color M3 completo (con Dark Theme).**