package com.vanvault.android.domain.repository

import com.vanvault.android.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de Usuario (Auth + Profile).
 *
 * Combina dos fuentes:
 *  - [FirebaseAuth.currentUser] para identidad (uid, email, displayName)
 *  - `/users/{uid}` en Realtime Database para datos extra (phone, prefs)
 */
interface UserRepository {
    /** Devuelve el perfil actual o null si no hay sesión. Cold flow. */
    fun observeCurrentProfile(): Flow<UserProfile?>

    /** Snapshot del perfil actual. */
    suspend fun getCurrentProfile(): UserProfile?

    /** Actualiza los datos del perfil del usuario actual. */
    suspend fun updateProfile(displayName: String, phone: String?): Result<Unit>

    /** Cierra la sesión del usuario actual. */
    suspend fun signOut(): Result<Unit>
}