package com.vanvault.android.data.repository

import java.text.Normalizer

private val ACCENT_MARKS = Regex("\\p{Mn}+")
private val NON_ALNUM = Regex("[^a-z0-9]+")
private val FORBIDDEN_KEY_CHARS = Regex("""[./$#\[\]]""")
private val SPACES = Regex("""\s+""")

/** Words under 2 characters are not indexed or searched. */
const val MIN_TOKEN_LENGTH = 2

/**
 * Converts text into comparable tokens: lowercase, without accents, split by non-alphanumeric chars.
 * Matches the logic in `tools/build_firebase_import.py`.
 */
fun tokenize(text: String): Set<String> =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(ACCENT_MARKS, "")
        .lowercase()
        .split(NON_ALNUM)
        .filterTo(linkedSetOf()) { it.length >= MIN_TOKEN_LENGTH }

/**
 * Normalizes a product name into a valid Firebase key, replacing forbidden characters.
 */
fun normalizeProductKey(name: String): String? =
    name.replace(FORBIDDEN_KEY_CHARS, "_")
        .replace(SPACES, " ")
        .trim()
        .ifEmpty { null }

/**
 * Normalizes a model string. Kept for exact model search compatibility.
 */
fun normalizeModelKey(input: String): String? =
    input.replace(FORBIDDEN_KEY_CHARS, "_")
        .replace(SPACES, " ")
        .trim()
        .ifEmpty { null }

/**
 * Intersects multiple sets of search results, starting with the smallest set for efficiency.
 */
fun intersectAll(sets: List<Set<String>>): Set<String> {
    if (sets.isEmpty()) return emptySet()
    val sorted = sets.sortedBy { it.size }
    var result: Set<String> = sorted.first()
    for (i in 1 until sorted.size) {
        if (result.isEmpty()) break
        result = result.filterTo(linkedSetOf()) { it in sorted[i] }
    }
    return result
}

/**
 * Organizes product candidates, sorting by length first (prioritizing shorter/exact matches),
 * then alphabetically.
 */
fun organizeCandidates(candidates: Set<String>): List<String> {
    return candidates.sortedWith(compareBy({ it.length }, { it }))
}

/**
 * Generates Firebase search index paths for a product to be used in batched updates.
 */
fun searchIndexPaths(productKey: String, model: String, indexPath: String = "searchIndex"): List<String> =
    (tokenize(productKey) + tokenize(model)).map { "$indexPath/$it/$productKey" }
