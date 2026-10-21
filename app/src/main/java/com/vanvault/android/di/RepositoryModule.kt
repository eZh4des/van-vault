package com.vanvault.android.di

import com.vanvault.android.data.repository.ProductRepositoryImpl
import com.vanvault.android.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Vincula las implementaciones de repositorios a sus contratos de dominio.
 *
 * `@Binds` es más eficiente que `@Provides` cuando solo se delega al
 * constructor (Hilt genera código sin lógica adicional).
 *
 * Pragmatic tip: "Code to an interface" — el dominio depende solo de
 * [ProductRepository] (interfaz), no de la implementación concreta.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: com.vanvault.android.data.repository.UserRepositoryImpl
    ): com.vanvault.android.domain.repository.UserRepository
}