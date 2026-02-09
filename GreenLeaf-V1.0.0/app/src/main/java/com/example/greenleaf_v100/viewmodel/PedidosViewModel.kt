package com.example.greenleaf_v100.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.greenleaf_v100.model.Pedido
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PedidosViewModel : ViewModel() {
    private val _pedidos = MutableLiveData<List<Pedido>>()
    val pedidos: LiveData<List<Pedido>> = _pedidos

    fun cargarPedidos() {
        FirebaseFirestore.getInstance().collection("pedidos")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("PedidosViewModel", "Error al cargar pedidos", error)
                    return@addSnapshotListener
                }

                val pedidosList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Pedido::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _pedidos.postValue(pedidosList)
            }
    }

    fun marcarComoEntregado(pedidoId: String) {
        FirebaseFirestore.getInstance().collection("pedidos")
            .document(pedidoId)
            .update("estado", "entregado")
            .addOnSuccessListener {
                // Actualización exitosa
            }
            .addOnFailureListener { e ->
                Log.e("PedidosViewModel", "Error al marcar como entregado", e)
            }
    }

    fun eliminarPedido(pedidoId: String) {
        FirebaseFirestore.getInstance().collection("pedidos")
            .document(pedidoId)
            .delete()
            .addOnSuccessListener {
                // Eliminación exitosa
            }
            .addOnFailureListener { e ->
                Log.e("PedidosViewModel", "Error al eliminar pedido", e)
            }
    }
}