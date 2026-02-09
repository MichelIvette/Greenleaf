package com.example.greenleaf_v100.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Pedido(
    val id: String = "",
    val clienteId: String = "",
    val clienteEmail: String = "",
    val items: List<ItemPedido> = emptyList(),
    val fecha: Long = 0L,
    val estado: String = "pendiente",
    val direccion: String = ""
) : Parcelable

@Parcelize
data class ItemPedido(
    val plantaId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0,
    val imagenUrl: String = ""
) : Parcelable