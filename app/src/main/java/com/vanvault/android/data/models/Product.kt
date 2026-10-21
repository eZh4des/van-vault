package com.vanvault.android.data.models

/**
 * DTO (Data Transfer Object) — representa el documento en Firebase Realtime Database.
 *
 * Pragmatic tip (Hexagonal / Clean): mantener el DTO separado del modelo de dominio.
 * Si Firebase cambia el esquema (p. ej. "priceCents" en lugar de "unitPrice"),
 * solo cambia este archivo; el dominio no se entera.
 *
 * El mapper a `com.vanvault.android.domain.model.Product` vive en ProductRepositoryImpl.
 */
data class ProductDto(
    val id: String = "",
    val name: String = "",
    val model: String? = null,
    val quantity: Int? = 0,
    val unitPrice: Double? = 0.0
)