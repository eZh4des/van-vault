package com.vanvault.android.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación Firebase de [UserRepository].
 *
 * Combina dos fuentes:
 *  - [FirebaseAuth.currentUser] (uid, email, displayName)
 *  - /users/{uid} en RTDB (phone)
 *
 * Si no hay sesión, `observeCurrentProfile()` emite `null`.
 *
 * DI: Hilt provee [FirebaseAuth] y [FirebaseDatabase] desde FirebaseModule.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : UserRepository {

    override fun observeCurrentProfile(): Flow<UserProfile?> = channelFlow {
        // Track active valueListeners for cleanup (per uid).
        // closure mutable hace que pueda agregar listeners desde distintos callbacks.
        val activeProfileListeners = mutableMapOf<String, ValueEventListener>()

        val authListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(null)
            } else {
                val profileRef = database.getReference(USER_PATH).child(user.uid)
                // Si ya hay listener activo para este uid, no adjuntar otro
                if (activeProfileListeners.containsKey(user.uid)) return@AuthStateListener

                val valueListener = profileRef.addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val phone = snapshot.child("phone").getValue(String::class.java)
                            trySend(snapshotToProfile(user, phone))
                        }
                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                            trySend(snapshotToProfile(user, null))
                        }
                    }
                )
                activeProfileListeners[user.uid] = valueListener
                // Emisión inicial sin esperar RTDB
                trySend(snapshotToProfile(user, null))
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            // Limpiar listeners de RTDB
            activeProfileListeners.forEach { (uid, listener) ->
                database.getReference(USER_PATH).child(uid).removeEventListener(listener)
            }
            activeProfileListeners.clear()
        }
    }

    override suspend fun getCurrentProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        return try {
            val snap = database.getReference(USER_PATH).child(user.uid).get().await()
            val phone = snap.child("phone").getValue(String::class.java)
            snapshotToProfile(user, phone)
        } catch (e: Exception) {
            snapshotToProfile(user, null)
        }
    }

    /**
     * Registro end-to-end:
     *  1. createUser en Auth
     *  2. updateProfile(displayName) en Auth
     *  3. /users/{uid} en RTDB
     *  4. sendEmailVerification
     */
    override suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        phone: String?
    ): Result<UserProfile> = runCatching {
        // 1. Crear usuario en Auth
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw IllegalStateException("Auth devolvio user null")

        // 2. Actualizar displayName en Auth
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()

        // 3. Guardar en RTDB /users/{uid}
        val updates = mapOf(
            "name" to displayName,
            "email" to email,
            "phone" to (phone ?: "")
        )
        database.getReference(USER_PATH).child(user.uid).updateChildren(updates).await()

        // 4. Enviar email de verificacion
        user.sendEmailVerification().await()

        // Retornar el perfil recien creado
        snapshotToProfile(user, phone)
    }

    /**
     * Inicia sesion via FirebaseAuth + carga perfil de RTDB.
     *
     *  1. signInWithEmailAndPassword(Auth)
     *  2. Lee /users/{uid} (RTDB) para datos extra (phone)
     *  3. Verifica emailVerified -> si no, retorna excepcion especial
     *     (cumple requerimiento original de verificar email antes de acceder)
     */
    override suspend fun loginUser(email: String, password: String): Result<UserProfile> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Auth devolvio user null")

        // Leer /users/{uid} en RTDB
        val phone = try {
            val snap = database.getReference(USER_PATH).child(firebaseUser.uid).get().await()
            snap.child("phone").getValue(String::class.java)
        } catch (e: Exception) {
            null  // Si falla RTDB, continuar sin telefono
        }

        snapshotToProfile(firebaseUser, phone?.takeIf { it.isNotBlank() })
    }

    override suspend fun updateProfile(displayName: String, phone: String?): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No hay sesión activa")
        val updates = mapOf(
            "name" to displayName,
            "phone" to (phone ?: "")
        )
        database.getReference(USER_PATH).child(user.uid).updateChildren(updates).await()
        // Sincronizar displayName también en FirebaseAuth
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }

    private fun snapshotToProfile(
        user: com.google.firebase.auth.FirebaseUser,
        phone: String?
    ): UserProfile {
        val name = user.displayName ?: user.email?.substringBefore("@") ?: "Usuario"
        return UserProfile(
            uid = user.uid,
            displayName = name,
            email = user.email ?: "",
            phone = phone?.takeIf { it.isNotBlank() },
            isEmailVerified = user.isEmailVerified
        )
    }

    companion object {
        const val USER_PATH = "users"
    }
}