package com.example.greenleaf_v100.model

data class ModelPlanta(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var precio: String = "",
    var fotoUrl: String = "",
    var tipo: String = "",         // spnTipo
    var estancia: String = "",     // spnEstancia
    var riego: String = "",        // spnRiego
    var consejo: String = "",
    var stock: Int = 0,
    var esFavorito: Boolean = false
)
