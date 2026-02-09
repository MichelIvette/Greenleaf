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

data class RegistroResult_C(
    val isSuccess: Boolean,
    val errorMessage: String? = null
)

class RegistroClientesViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _registroResult = MutableLiveData<RegistroResult_C>()
    val registroResult: LiveData<RegistroResult_C> = _registroResult

    /**
     * Registra un nuevo cliente con todos los campos, sube la foto y limita a 8 registros.
     */
    fun registerCliente(
        nombre: String,
        paterno: String,
        materno: String,
        email: String,
        password: String,
        fechaNacimiento: String,
        domicilioFiscal: String,
        fotoBmp: Bitmap
    ) {
        // 1) Validar no vacíos
        if (listOf(nombre, paterno, materno, email, password, fechaNacimiento, domicilioFiscal)
                .any { it.isBlank() }
        ) {
            _registroResult.value = RegistroResult_C(false, "Todos los campos son obligatorios")
            return
        }

        // 2) Comprobar límite de 8 clientes
        db.collection("clientes")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.size() >= 8) {
                    _registroResult.value =
                        RegistroResult_C(false, "Límite de 8 clientes alcanzado")
                    return@addOnSuccessListener
                }

                // 3) Crear usuario en Auth
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { authRes ->
                        val uid = authRes.user?.uid ?: return@addOnSuccessListener

                        // 4) Comprimir y subir la foto
                        val baos = ByteArrayOutputStream().apply {
                            fotoBmp.compress(Bitmap.CompressFormat.JPEG, 80, this)
                        }
                        val storageRef = FirebaseStorage.getInstance()
                            .reference
                            .child("clientes/$uid/photo.jpg")

                        storageRef.putBytes(baos.toByteArray())
                            .continueWithTask { task ->
                                if (!task.isSuccessful) throw task.exception!!
                                storageRef.downloadUrl
                            }
                            .addOnSuccessListener { uri ->
                                // 5) Guardar todos los datos en Firestore
                                val data = mapOf(
                                    "nombre"           to nombre,
                                    "paterno"          to paterno,
                                    "materno"          to materno,
                                    "email"            to email,
                                    "password"         to password,
                                    "fechaNacimiento"  to fechaNacimiento,
                                    "domicilioFiscal"  to domicilioFiscal,
                                    "fotoUrl"          to uri.toString(),
                                    "tipoUsuario"      to "clientes",
                                    "timestamp"        to FieldValue.serverTimestamp()
                                )
                                db.collection("clientes")
                                    .document(uid)
                                    .set(data)
                                    .addOnSuccessListener {
                                        _registroResult.value = RegistroResult_C(true)
                                    }
                                    .addOnFailureListener { e ->
                                        _registroResult.value =
                                            RegistroResult_C(false, e.localizedMessage)
                                    }
                            }
                            .addOnFailureListener { e ->
                                _registroResult.value =
                                    RegistroResult_C(false, e.localizedMessage)
                            }
                    }
                    .addOnFailureListener { e ->
                        _registroResult.value =
                            RegistroResult_C(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                _registroResult.value =
                    RegistroResult_C(false, e.localizedMessage)
            }
    }
}
