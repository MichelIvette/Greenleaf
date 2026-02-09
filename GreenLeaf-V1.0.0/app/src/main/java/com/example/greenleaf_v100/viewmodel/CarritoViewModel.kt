package com.example.greenleaf_v100.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import com.example.greenleaf_v100.model.CartItem

class CarritoViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private var userId: String = ""

    fun setUserId(uid: String) {
        userId = uid
        loadCartItems()
    }

    private fun loadCartItems() {
        FirebaseFirestore.getInstance()
            .collection("carritos")
            .document(userId)
            .collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CarritoViewModel", "Error al cargar carrito", error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _cartItems.value = items
            }
    }

    fun addToCart(item: CartItem) {
        val cartRef = FirebaseFirestore.getInstance()
            .collection("carritos")
            .document(userId)
            .collection("items")

        cartRef.whereEqualTo("name", item.name).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    cartRef.add(item)
                } else {
                    val existingDoc = querySnapshot.documents.first()
                    val existingItem = existingDoc.toObject(CartItem::class.java)
                    existingItem?.let {
                        cartRef.document(existingDoc.id)
                            .update("quantity", it.quantity + 1)
                    }
                }
            }
    }

    fun removeFromCart(item: CartItem) {
        if (item.id.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("carritos")
                .document(userId)
                .collection("items")
                .document(item.id)
                .delete()
        }
    }

    fun clearCart() {
        FirebaseFirestore.getInstance()
            .collection("carritos")
            .document(userId)
            .collection("items")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = FirebaseFirestore.getInstance().batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
    }
}
