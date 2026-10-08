package com.vanvault.android.data.repository

import com.vanvault.android.data.models.Product
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Result for finding a SINGLE product by its exact name. */
sealed interface ProductLookup {
    data class Found(val product: Product) : ProductLookup
    data object NotFound : ProductLookup
    data class Error(val message: String) : ProductLookup
}

/** Result for searching products by keyword tokens. */
sealed interface ProductSearch {
    data class Results(val products: List<Product>, val hasMore: Boolean) : ProductSearch
    data object Empty : ProductSearch
    data class Error(val message: String) : ProductSearch
}

/**
 * Firebase Structure:
 *  inventory/{name}            -> { model, quantity, unitPrice }
 *  searchIndex/{token}/{name} -> true
 *
 * Search uses intersection of prefix matches in `searchIndex` without fetching the full inventory.
 */
class ProductRepository(
    private val inventoryPath: String = "inventory",
    private val indexPath: String = "searchIndex"
) {
    private val db get() = FirebaseDatabase.getInstance()

    /**
     * Searches for products containing ALL tokenized words as prefixes.
     */
    suspend fun search(text: String, limit: Int = 30): ProductSearch {
        val words = tokenize(text)
        if (words.isEmpty()) {
            return ProductSearch.Error("Enter at least one word with $MIN_TOKEN_LENGTH characters")
        }
        return try {
            // Query each word token in parallel.
            val keySets = coroutineScope {
                words.map { word -> async { keysForWordPrefix(word) } }.awaitAll()
            }
            val keys = organizeCandidates(intersectAll(keySets))
            if (keys.isEmpty()) return ProductSearch.Empty

            val page = keys.take(limit)
            // Direct O(1) read of each matched product.
            val products = coroutineScope {
                page.map { key -> async { readProduct(key) } }.awaitAll().filterNotNull()
            }
            if (products.isEmpty()) ProductSearch.Empty
            else ProductSearch.Results(products, hasMore = keys.size > limit)
        } catch (e: Exception) {
            ProductSearch.Error(e.message ?: "Could not query inventory")
        }
    }

    /** Finds a product by its EXACT name (node key). */
    suspend fun findByName(name: String): ProductLookup {
        val key = normalizeProductKey(name) ?: return ProductLookup.Error("Enter product name")
        return try {
            val product = readProduct(key)
            if (product == null) ProductLookup.NotFound else ProductLookup.Found(product)
        } catch (e: Exception) {
            ProductLookup.Error(e.message ?: "Could not query inventory")
        }
    }

    /** True if the product exists. Used to block duplicates. */
    suspend fun exists(name: String): Boolean = findByName(name) is ProductLookup.Found

    /**
     * Searches by exact model. Kept for historical use; active search uses [search].
     */
    suspend fun findByModel(model: String): ProductLookup {
        val wanted = normalizeModelKey(model) ?: return ProductLookup.Error("Enter a model to search")
        val words = tokenize(wanted)
        if (words.isEmpty()) return ProductLookup.Error("Enter a model to search")
        return try {
            val keys = intersectAll(coroutineScope {
                words.map { async { keysForWordPrefix(it) } }.awaitAll()
            })
            val match = coroutineScope { keys.map { async { readProduct(it) } }.awaitAll() }
                .filterNotNull()
                .firstOrNull { normalizeModelKey(it.modelOrBarcode).equals(wanted, ignoreCase = true) }
            if (match == null) ProductLookup.NotFound else ProductLookup.Found(match)
        } catch (e: Exception) {
            ProductLookup.Error(e.message ?: "Could not query inventory")
        }
    }

    /** Fetches product keys from the index matching the prefix. */
    private suspend fun keysForWordPrefix(prefix: String): Set<String> {
        val snapshot = db.getReference(indexPath)
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
        val snap = db.getReference(inventoryPath).child(key).fetch()
        if (!snap.exists()) return null
        return Product(
            id = key,
            name = key,
            modelOrBarcode = snap.child("model").getValue(String::class.java).orEmpty(),
            quantity = (snap.child("quantity").value as? Number)?.toInt() ?: 0,
            unitPrice = (snap.child("unitPrice").value as? Number)?.toDouble() ?: 0.0
        )
    }

    private suspend fun Query.fetch(): DataSnapshot = suspendCancellableCoroutine { cont ->
        get()
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }
}
