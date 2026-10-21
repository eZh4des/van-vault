package com.vanvault.android.domain.repository

import com.vanvault.android.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de Usuario (Auth + Profile).
 *
 * Combina dos fuentes:
 *  - [FirebaseAuth] (createUser, updateProfile, sendEmailVerification, signOut)
 *  - `/users/{uid}` en Realtime Database para datos extra (name, phone)
 *
 * **Importante para M9 (fix RegisterScreen)**: la UI NO debe llamar a
 * Firebase directamente. Toda la lógica de registro vive en este repo
 * vía [registerUser].
 */
interface UserRepository {
    /** Devuelve el perfil actual o null si no hay sesión. Cold flow. */
    fun observeCurrentProfile(): Flow<UserProfile?>

    /** Snapshot del perfil actual. */
    suspend fun getCurrentProfile(): UserProfile?

    /**
     * Registra un nuevo usuario.
     *
     * Pasos:
     *  1. FirebaseAuth.createUserWithEmailAndPassword
     *  2. UserProfileChangeRequest con displayName (en Auth)
     *  3. /users/{uid} en RTDB con { name, email, phone: null }
     *  4. sendEmailVerification()
     *
     * @return [Result.success] con el [UserProfile] creado si todo OK
     */
    suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        phone: String?
    ): Result<UserProfile>

    /** Actualiza los datos del perfil del usuario actual. */
    suspend fun updateProfile(displayName: String, phone: String?): Result<Unit>

    /** Cierra la sesión del usuario actual. */
    suspend fun signOut(): Result<Unit>
}