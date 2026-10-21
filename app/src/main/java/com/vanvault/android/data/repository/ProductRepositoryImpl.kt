package com.vanvault.android.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.vanvault.android.domain.model.Money
import com.vanvault.android.domain.model.Product
import com.vanvault.android.domain.repository.ProductLookupResult
import com.vanvault.android.domain.repository.ProductRepository
import com.vanvault.android.domain.repository.ProductSearchResult
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Implementación Firebase de [ProductRepository].
 *
 * Arquitectura (Google):
 *  - Capa data (esta clase) encapsula Firebase.
 *  - Expone el modelo de dominio ([Product] con [Money]).
 *  - La UI solo conoce la interfaz del dominio (orthogonality / The Pragmatic Programmer).
 *
 * Esquema Firebase:
 *   inventory/{name}            -> { model, quantity, unitPrice }
 *   searchIndex/{token}/{name} -> true
 *
 * DI: Hilt inyecta [FirebaseDatabase] desde [com.vanvault.android.di.FirebaseModule].
 * Las rutas son constantes (no necesitan ser inyectadas — son detalles de impl).
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : ProductRepository {

    override fun observeInventory(): Flow<List<Product>> = callbackFlow {
        val ref = database.getReference(INVENTORY_PATH)
        val listener = ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = snapshot.children.mapNotNull { it.toDomainOrNull() }
                trySend(products)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        })
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun search(query: String, limit: Int): ProductSearchResult {
        val words = tokenize(query)
        if (words.isEmpty()) return ProductSearchResult.Empty
        return try {
            val keySets = coroutineScope {
                words.map { w -> async { keysForWordPrefix(w) } }.awaitAll()
            }
            val keys = organizeCandidates(intersectAll(keySets))
            if (keys.isEmpty()) return ProductSearchResult.Empty

            val page = keys.take(limit)
            val products = coroutineScope {
                page.map { key -> async { readProduct(key) } }.awaitAll().filterNotNull()
            }
            if (products.isEmpty()) ProductSearchResult.Empty
            else ProductSearchResult.Success(products, hasMore = keys.size > limit)
        } catch (e: Exception) {
            ProductSearchResult.Error(e.message ?: "No se pudo consultar el inventario")
        }
    }

    override suspend fun findByName(name: String): ProductLookupResult {
        val key = normalizeProductKey(name) ?: return ProductLookupResult.Error("Ingresa un nombre")
        return try {
            val product = readProduct(key)
            if (product == null) ProductLookupResult.NotFound else ProductLookupResult.Found(product)
        } catch (e: Exception) {
            ProductLookupResult.Error(e.message ?: "No se pudo consultar el inventario")
        }
    }

    override suspend fun findById(id: String): Product? = readProduct(id)

    override suspend fun save(product: Product): Result<Unit> = runCatching {
        val key = normalizeProductKey(product.name)
            ?: throw IllegalArgumentException("Nombre inválido para guardar")
        val dto = mapOf(
            "model" to product.modelOrBarcode,
            "quantity" to product.quantity,
            "unitPrice" to (product.unitPrice.cents / 100.0)
        )
        database.getReference(INVENTORY_PATH).child(key).setValue(dto).await()
        writeIndex(product, key)
    }

    override suspend fun update(product: Product): Result<Unit> = runCatching {
        val newKey = normalizeProductKey(product.name)
            ?: throw IllegalArgumentException("Nombre inválido para actualizar")

        // Detectar rename: si la key nueva difiere del id actual, hay que
        // reescribir el searchIndex (borrar tokens viejos, crear nuevos).
        val oldKey = product.id.takeIf { it.isNotBlank() }?.let { normalizeProductKey(it) }
        val isRename = oldKey != null && oldKey != newKey

        val dto = mapOf(
            "model" to product.modelOrBarcode,
            "quantity" to product.quantity,
            "unitPrice" to (product.unitPrice.cents / 100.0)
        )

        if (isRename) {
            // oldKey es no-null garantizado por la condicion de isRename.
            val safeOldKey = oldKey!!
            // Rename en Firebase: borrar viejo, escribir nuevo
            database.getReference(INVENTORY_PATH).child(safeOldKey).removeValue().await()
            database.getReference(INVENTORY_PATH).child(newKey).setValue(dto).await()
            removeIndexFor(safeOldKey)
        } else {
            database.getReference(INVENTORY_PATH).child(newKey).setValue(dto).await()
        }
        // Reindexar con el nombre/modelo actuales
        writeIndex(product, newKey)
    }

    /**
     * Elimina las entradas del searchIndex que apuntan a un producto.
     * Útil cuando se renombra o elimina un producto.
     *
     * Implementación: scan completo del searchIndex. Para nuestro tamaño
     * (~400 tokens) es eficiente. Si creciera a millones, usaríamos un
     * Map<token, Set<productKey>> en memoria o Algolia/Elastic.
     */
    private suspend fun removeIndexFor(productKey: String) {
        val ref = database.getReference(INDEX_PATH)
        val snapshot = ref.get().await()
        val updates = mutableMapOf<String, Any?>()
        for (tokenSnap in snapshot.children) {
            if (tokenSnap.hasChild(productKey)) {
                updates["${tokenSnap.key}/$productKey"] = null
            }
        }
        if (updates.isNotEmpty()) {
            database.getReference().updateChildren(updates).await()
        }
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        database.getReference(INVENTORY_PATH).child(id).removeValue().await()
        // Limpiar entradas del searchIndex para que la busqueda no devuelva
        // un producto que ya no existe.
        removeIndexFor(id)
    }

    // ---------- Internals ----------

    private suspend fun keysForWordPrefix(prefix: String): Set<String> {
        val snapshot = database.getReference(INDEX_PATH)
            .orderByKey()
            .startAt(prefix)
            .endAt(prefix + "\uf8ff")
            .fetch()
        val keys = linkedSetOf<String>()
        for (word in snapshot.children) {
            for (productKey in word.children) productKey.key?.let(keys::add)
        }
        return keys
    }

    private suspend fun readProduct(key: String): Product? {
        val snap = database.getReference(INVENTORY_PATH).child(key).fetch()
        return snap.toDomainOrNull()
    }

    /**
     * Escribe (o sobrescribe) las entradas del índice invertido para este producto.
     *
     * Crea `searchIndex/{token}/{productKey}: true` para CADA token del nombre + modelo.
     * Esto permite que `search(query)` haga `orderByKey().startAt(token)` y
     * resuelva en O(log n) en lugar de O(n).
     *
     * Si [ModelOrBarcode] cambia, los tokens viejos del modelo quedan como basura
     * (mitigado en M2 con `update()`). Por ahora aceptable.
     */
    private suspend fun writeIndex(product: Product, key: String) {
        val tokens = tokenize(product.name) + tokenize(product.modelOrBarcode)
        if (tokens.isEmpty()) return
        val updates = mutableMapOf<String, Any>()
        for (token in tokens) {
            updates["$INDEX_PATH/$token/$key"] = true
        }
        database.getReference().updateChildren(updates).await()
    }

    private fun DataSnapshot.toDomainOrNull(): Product? {
        if (!exists()) return null
        val name = key ?: return null
        val model = child("model").getValue(String::class.java).orEmpty()
        val quantity = (child("quantity").value as? Number)?.toInt() ?: 0
        val unitPriceCents = ((child("unitPrice").value as? Number)?.toDouble() ?: 0.0)
        return Product(
            id = name,
            name = name,
            modelOrBarcode = model,
            quantity = quantity,
            unitPrice = Money.fromDouble(unitPriceCents)
        )
    }

    private suspend fun Query.fetch(): DataSnapshot = suspendCancellableCoroutine { cont ->
        get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        // "Put Abstractions in Code, Details in Metadata" (Pragmatic Programmer Tip 38)
        // Las rutas son detalles de Firebase; viven aquí como metadata, no como dependencias.
        const val INVENTORY_PATH = "inventory"
        const val INDEX_PATH = "searchIndex"
    }
}

// Helper para Firebase Task<T> -> suspend
private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addOnSuccessListener { cont.resume(it) }
        addOnFailureListener { cont.resumeWithException(it) }
    }