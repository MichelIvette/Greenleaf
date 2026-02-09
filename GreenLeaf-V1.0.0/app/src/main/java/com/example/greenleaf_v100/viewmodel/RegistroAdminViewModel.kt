package com.example.greenleaf_v100.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

data class RegistroResult(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

class RegistroAdminViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _registroResult = MutableLiveData<RegistroResult>()
    val registroResult: LiveData<RegistroResult> = _registroResult

    fun registerAdmin(
        nombre: String,
        paterno: String,
        materno: String,
        email: String,
        password: String,
        fechaNacimiento: String,
        domicilioFiscal: String,
        fotoBmp: Bitmap
    ) {
        // Validar campos no vacíos
        if (listOf(nombre, paterno, materno, email, password, fechaNacimiento, domicilioFiscal)
                .any { it.isBlank() }
        ) {
            _registroResult.value = RegistroResult(false, "Todos los campos son obligatorios")
            return
        }

        // Límite 8 admins
        db.collection("admins")
            .get()
            .addOnSuccessListener { snap ->
                if (snap.size() >= 8) {
                    _registroResult.value = RegistroResult(false, "Límite de 8 administradores alcanzado")
                    return@addOnSuccessListener
                }

                // Crear usuario
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { resAuth ->
                        val uid = resAuth.user?.uid ?: return@addOnSuccessListener

                        // Subir foto
                        val baos = ByteArrayOutputStream().apply {
                            fotoBmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
                        }
                        val ref = FirebaseStorage.getInstance()
                            .reference
                            .child("admins/$uid/photo.jpg")

                        ref.putBytes(baos.toByteArray())
                            .addOnSuccessListener {
                                ref.downloadUrl
                                    .addOnSuccessListener { uri ->
                                        // Guardar datos
                                        val data = mapOf(
                                            "nombre"           to nombre,
                                            "paterno"          to paterno,
                                            "materno"          to materno,
                                            "email"            to email,
                                            "tipoUsuario"      to "administrador",
                                            "timestamp"        to FieldValue.serverTimestamp(),
                                            "fechaNacimiento"  to fechaNacimiento,
                                            "domicilioFiscal"  to domicilioFiscal,
                                            "fotoUrl"          to uri.toString()
                                        )
                                        db.collection("admins")
                                            .document(uid)
                                            .set(data)
                                            .addOnSuccessListener {
                                                _registroResult.value = RegistroResult(true)
                                            }
                                            .addOnFailureListener { e ->
                                                _registroResult.value = RegistroResult(false, e.localizedMessage)
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        _registroResult.value = RegistroResult(false, e.localizedMessage)
                                    }
                            }
                            .addOnFailureListener { e ->
                                _registroResult.value = RegistroResult(false, e.localizedMessage)
                            }
                    }
                    .addOnFailureListener { e ->
                        _registroResult.value = RegistroResult(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                _registroResult.value = RegistroResult(false, e.localizedMessage)
            }
    }
}
