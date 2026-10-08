package com.vanvault.android.data.models

data class Product(
    val id: String = "",
    val name: String = "",
    val modelOrBarcode: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0
)
