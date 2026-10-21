package com.vanvault.android.data.repository

import com.vanvault.android.domain.model.Money
import com.vanvault.android.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests para la logica de busqueda lineal (fallback cuando el searchIndex
 * esta vacio).
 *
 * Refactoriza la logica de matching a una funcion pura testeable.
 */
class LinearSearchTest {

    private fun match(product: Product, words: Set<String>): Boolean {
        // En el repo real, `words` viene de `tokenize(query)` que lowercase
        // todo. Aqui replicamos: lowercase + ignora tokens de <2 chars.
        val productTokens = (tokenize(product.name) + tokenize(product.modelOrBarcode))
            .map { it.lowercase() }.toSet()
        val normalizedWords = words.map { it.lowercase() }.filter { it.length >= 2 }.toSet()
        return normalizedWords.all { w -> productTokens.any { it.contains(w) } }
    }

    private fun sample(name: String, model: String, qty: Int = 1) = Product(
        id = name,
        name = name,
        modelOrBarcode = model,
        quantity = qty,
        unitPrice = Money.ofCop(100_000)
    )

    @Test
    fun `single word matches product with that token in name`() {
        val product = sample("DVR 4CH 1080P", "DS-7104HGHI")
        assertTrue(match(product, setOf("dvr")))
    }

    @Test
    fun `single word matches product with that token in model`() {
        val product = sample("Camara IP", "DS-2CD1023")
        assertTrue(match(product, setOf("ds")))
    }

    @Test
    fun `two words require BOTH to be present (AND)`() {
        val product = sample("DVR 4CH 1080P", "DS-7104HGHI")
        // Contiene "dvr" pero no "camara" -> no matchea
        assertEquals(false, match(product, setOf("dvr", "camara")))
    }

    @Test
    fun `two words both present match (AND)`() {
        val product = sample("DVR 4CH 1080P", "DS-7104HGHI")
        // Contiene ambos "dvr" y "ds" -> matchea
        assertTrue(match(product, setOf("dvr", "ds")))
    }

    @Test
    fun `partial substring match works (contains)`() {
        // '1080' es substring de '1080p' -> matchea
        val product = sample("Camara 1080P", "MODEL-X")
        assertTrue(match(product, setOf("1080")))
    }

    @Test
    fun `accents are normalized before matching`() {
        // 'Camara' (sin tilde) matchea con "camara" (sin tilde)
        // 'Domo' (sin tilde) matchea con "domo" (sin tilde)
        val product = sample("Cámara Domo", "DS-2CE")
        // tokenize normaliza acentos, asi que en productTokens ya esta sin tilde
        assertTrue(match(product, setOf("camara")))
        assertTrue(match(product, setOf("domo")))
    }

    @Test
    fun `case-insensitive matching`() {
        val product = sample("DVR 4CH", "ds-7104")
        // Inputs ya en lowercase (como en el repo real via tokenize)
        assertTrue(match(product, setOf("dvr")))
        assertTrue(match(product, setOf("ds")))
    }

    @Test
    fun `words of length 1 are dropped before reaching match (handled upstream)`() {
        // En el repo real, `ProductRepositoryImpl.search()` primero hace
        // `tokenize(query)` y retorna `Empty` si el set queda vacio
        // (incluyendo el caso de length<2). Por lo tanto, `linearSearch()`
        // NUNCA recibe words de length 1. Este test valida ese invariant
        // del upstream.
        val tokenized = tokenize("4 a X")
        assertTrue("tokenize filtra words de length<2", tokenized.isEmpty())

        // Si por algun motivo llega un set vacio, match() retorna true
        // (semantica de 'all' en Kotlin: vacio => true). Esto es seguro
        // porque el search() upstream filtra los vacios.
        val product = sample("Test", "Model")
        assertTrue(match(product, emptySet()))
    }

    @Test
    fun `empty words set matches everything`() {
        val product = sample("DVR", "DS")
        // words vacio -> vacio.all { ... } == true -> matchea
        assertTrue(match(product, emptySet()))
    }

    @Test
    fun `multi word search uses intersection semantics`() {
        val products = listOf(
            sample("CABLE UTP CAT 6", "EXT"),
            sample("CABLE USB", "C"),
            sample("ADAPTADOR", "X")
        )
        // words = {cable, utp} -> matchea solo el primero
        val matched = products.filter { match(it, setOf("cable", "utp")) }
        assertEquals(1, matched.size)
        assertEquals("CABLE UTP CAT 6", matched[0].name)
    }
}