package com.example.greenleaf_v100.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModelAdmin(
    val nombre: String = "",
    val paterno: String = "",
    val materno: String = "",
    val email: String = "",
    val password: String = "",
    val fechaNacimiento: String = "",
    val domicilioFiscal: String = "",
    val fotoUrl: String = "",
    val tipoUsuario: String = "administrador"
) : Parcelable
