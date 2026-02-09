package com.example.greenleaf_v100.model



data class CartItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1
) {
    // Constructor sin parámetros requerido para Firestore
    constructor() : this("", "", "", 0.0, "", 1)
}