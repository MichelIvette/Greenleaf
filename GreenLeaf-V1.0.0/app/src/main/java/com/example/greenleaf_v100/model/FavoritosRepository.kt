package com.example.greenleaf_v100.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FavoritosRepository {

    private val db = FirebaseFirestore.getInstance()

    fun agregarAFavoritos(userId: String, planta: ModelPlanta) {
        db.collection("Favoritos")
            .document(userId)
            .collection("plantasFavoritas")
            .document(planta.id)
            .set(hashMapOf("fecha" to FieldValue.serverTimestamp()))
    }

    fun eliminarDeFavoritos(userId: String, plantaId: String) {
        db.collection("Favoritos")
            .document(userId)
            .collection("plantasFavoritas")
            .document(plantaId)
            .delete()
    }

    fun verificarSiEsFavorito(userId: String, plantaId: String, callback: (Boolean) -> Unit) {
        db.collection("Favoritos")
            .document(userId)
            .collection("plantasFavoritas")
            .document(plantaId)
            .get()
            .addOnSuccessListener { callback(it.exists()) }
            .addOnFailureListener { callback(false) }
    }
}