package com.vanvault.android.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vanvault.android.domain.model.UserProfile
import com.vanvault.android.domain.model.UserRole
import com.vanvault.android.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación Firebase de [UserRepository].
 *
 * Combina dos fuentes:
 *  - [FirebaseAuth.currentUser] (uid, email, displayName)
 *  - /users/{uid} en RTDB (phone, role)
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
        val activeProfileListeners = mutableMapOf<String, ValueEventListener>()

        val authListener = AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(null)
            } else {
                val profileRef = database.getReference(USER_PATH).child(user.uid)
                if (activeProfileListeners.containsKey(user.uid)) return@AuthStateListener

                val valueListener = profileRef.addValueEventListener(
                    object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val phone = snapshot.child("phone").getValue(String::class.java)
                            val role = readRole(snapshot, user.email)
                            trySend(snapshotToProfile(user, phone, role))
                        }
                        override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                            trySend(snapshotToProfile(user, null, UserRole.fromEmail(user.email.orEmpty())))
                        }
                    }
                )
                activeProfileListeners[user.uid] = valueListener
                // Emisión inicial del perfil basico (Auth + role inferido)
                trySend(snapshotToProfile(user, null, UserRole.fromEmail(user.email.orEmpty())))
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
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
            val role = readRole(snap, user.email)
            snapshotToProfile(user, phone, role)
        } catch (e: Exception) {
            snapshotToProfile(user, null, UserRole.fromEmail(user.email.orEmpty()))
        }
    }

    /**
     * Registro end-to-end:
     *  1. createUser en Auth
     *  2. updateProfile(displayName) en Auth
     *  3. /users/{uid} en RTDB con { name, email, phone, role }
     *  4. sendEmailVerification
     */
    override suspend fun registerUser(
        email: String,
        password: String,
        displayName: String,
        phone: String?,
        role: UserRole
    ): Result<UserProfile> = runCatching {
        // 1. Crear usuario en Auth
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val user = authResult.user ?: throw IllegalStateException("Auth devolvio user null")

        // 2. Actualizar displayName en Auth
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()

        // 3. Guardar en RTDB /users/{uid} (incluyendo el rol asignado)
        val updates = mapOf(
            "name" to displayName,
            "email" to email,
            "phone" to (phone ?: ""),
            "role" to role.name
        )
        database.getReference(USER_PATH).child(user.uid).updateChildren(updates).await()

        // 4. Enviar email de verificacion
        user.sendEmailVerification().await()

        snapshotToProfile(user, phone, role)
    }

    /**
     * Login: lee /users/{uid} para phone + role.
     */
    override suspend fun loginUser(email: String, password: String): Result<UserProfile> = runCatching {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw IllegalStateException("Auth devolvio user null")

        val phone = try {
            val snap = database.getReference(USER_PATH).child(firebaseUser.uid).get().await()
            snap.child("phone").getValue(String::class.java)
        } catch (e: Exception) {
            null
        }
        val role = UserRole.fromEmail(firebaseUser.email.orEmpty())

        snapshotToProfile(firebaseUser, phone?.takeIf { it.isNotBlank() }, role)
    }

    override suspend fun updateProfile(displayName: String, phone: String?): Result<Unit> = runCatching {
        val user = auth.currentUser ?: throw IllegalStateException("No hay sesion activa")
        val updates = mapOf(
            "name" to displayName,
            "phone" to (phone ?: "")
        )
        database.getReference(USER_PATH).child(user.uid).updateChildren(updates).await()
        // Sincronizar displayName tambien en FirebaseAuth
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
        user.updateProfile(profileUpdates).await()
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        auth.signOut()
    }

    /**
     * Lee el rol persistido en RTDB. Si no existe (usuario legacy),
     * infiere por email.
     */
    private fun readRole(snapshot: DataSnapshot, email: String?): UserRole {
        val persisted = snapshot.child("role").getValue(String::class.java)
        return when (persisted) {
            "ADMIN" -> UserRole.ADMIN
            "CLIENT" -> UserRole.CLIENT
            else -> UserRole.fromEmail(email.orEmpty())
        }
    }

    private fun snapshotToProfile(
        user: com.google.firebase.auth.FirebaseUser,
        phone: String?,
        role: UserRole
    ): UserProfile {
        val name = user.displayName ?: user.email?.substringBefore("@") ?: "Usuario"
        return UserProfile(
            uid = user.uid,
            displayName = name,
            email = user.email ?: "",
            phone = phone?.takeIf { it.isNotBlank() },
            isEmailVerified = user.isEmailVerified,
            role = role
        )
    }

    companion object {
        const val USER_PATH = "users"
    }
}