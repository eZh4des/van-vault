package com.vanvault.android.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provee las dependencias de Firebase a nivel de aplicación.
 *
 * **Por qué**: aunque `FirebaseAuth.getInstance()` ya es singleton internamente,
 * centralizar aquí cumple "Decoupling" (Pragmatic Programmer): si Firebase
 * se reemplaza por otro backend, solo cambiamos este módulo.
 *
 * Pragmatic tip: las rutas de Firebase (`inventory`, `searchIndex`) son
 * detalles de la implementación, no del dominio. Viven en `ProductRepositoryImpl`
 * como `companion object` constants.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
}