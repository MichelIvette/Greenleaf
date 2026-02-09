package com.example.greenleaf_v100.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val auth      = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage   = FirebaseStorage.getInstance()

    // ─── LiveData básicos ─────────────────────────────────────────────────────
    private val _userName       = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _email          = MutableLiveData<String>()
    val email: LiveData<String> = _email

    private val _role           = MutableLiveData<String>()
    val role: LiveData<String> = _role

    private val _photoUri       = MutableLiveData<Uri?>()
    val photoUri: LiveData<Uri?> = _photoUri

    private val _passwordMasked = MutableLiveData<String>()
    val passwordMasked: LiveData<String> = _passwordMasked

    private val _errorMessage   = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // ─── LiveData extras (cliente/admin) ───────────────────────────────────────
    private val _firstName        = MutableLiveData<String>()
    val firstName: LiveData<String> = _firstName

    private val _lastNamePat      = MutableLiveData<String>()
    val lastNamePat: LiveData<String> = _lastNamePat

    private val _lastNameMat      = MutableLiveData<String>()
    val lastNameMat: LiveData<String> = _lastNameMat

    private val _fechaNacimiento  = MutableLiveData<String>()
    val fechaNacimiento: LiveData<String> = _fechaNacimiento

    private val _domicilioFiscal  = MutableLiveData<String>()
    val domicilioFiscal: LiveData<String> = _domicilioFiscal

    init {
        cargarDatosUsuario()
    }

    /** Limpia mensaje de error. */
    fun clearError() {
        _errorMessage.value = null
    }

    /** Carga todos los datos del usuario desde Auth y Firestore. */
    private fun cargarDatosUsuario() {
        val user = auth.currentUser ?: run {
            _errorMessage.value = "Usuario no autenticado"
            return
        }
        val uid = user.uid

        // 1) Email y contraseña enmascarada
        _email.value          = user.email ?: ""
        _passwordMasked.value = "********"

        // 2) Foto de perfil
        _photoUri.value = user.photoUrl

        // 3) Intentamos cargar como cliente…
        firestore.collection("clientes")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    poblarDesdeDocumento(doc.getData()!!)
                    _role.value = "Cliente"
                } else {
                    // …si no existe, lo cargamos como admin
                    firestore.collection("admins")
                        .document(uid)
                        .get()
                        .addOnSuccessListener { doc2 ->
                            if (doc2.exists()) {
                                poblarDesdeDocumento(doc2.getData()!!)
                                _role.value = "Administrador"
                            } else {
                                _errorMessage.value = "Perfil no encontrado en Firestore"
                            }
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = e.localizedMessage
                        }
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.localizedMessage
            }
    }

    /** Pobla los LiveData a partir del mapa de Firestore. */
    private fun poblarDesdeDocumento(data: Map<String, Any>) {
        _firstName.value       = data["nombre"]          as? String? ?: ""
        _lastNamePat.value     = data["paterno"]         as? String? ?: ""
        _lastNameMat.value     = data["materno"]         as? String? ?: ""
        _fechaNacimiento.value = data["fechaNacimiento"] as? String? ?: ""
        _domicilioFiscal.value = data["domicilioFiscal"] as? String? ?: ""
        // Para el saludo usamos el firstName
        _userName.value        = _firstName.value ?: ""
    }

    // ─── Actualizaciones de datos ──────────────────────────────────────────────

    /**
     * Actualiza un campo de perfil en Firestore y refresca el LiveData correspondiente.
     *
     * @param campo  Nombre del campo en Firestore ("nombre", "paterno", etc.).
     * @param valor  Nuevo valor para ese campo.
     */
    fun actualizarCampo(
        campo: String,
        valor: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }
        val colec = if (_role.value == "Cliente") "clientes" else "admins"
        firestore.collection(colec)
            .document(user.uid)
            .update(campo, valor)
            .addOnSuccessListener {
                // Refrescar el LiveData modificado
                when (campo) {
                    "nombre"          -> _firstName.value       = valor
                    "paterno"         -> _lastNamePat.value     = valor
                    "materno"         -> _lastNameMat.value     = valor
                    "fechaNacimiento" -> _fechaNacimiento.value = valor
                    "domicilioFiscal" -> _domicilioFiscal.value = valor
                }
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

    /**
     * Elimina lógicamente el registro estableciendo “deleted” = true.
     */
    fun eliminarRegistro(onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }
        val colec = if (_role.value == "Cliente") "clientes" else "admins"
        firestore.collection(colec)
            .document(user.uid)
            .update("deleted", true)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

    /**
     * Actualiza el correo en FirebaseAuth y en Firestore (campo “email”).
     */
    fun actualizarCorreo(nuevoEmail: String, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }
        user.updateEmail(nuevoEmail)
            .addOnSuccessListener {
                val colec = if (_role.value == "Cliente") "clientes" else "admins"
                firestore.collection(colec)
                    .document(user.uid)
                    .update("email", nuevoEmail)
                _email.value = nuevoEmail
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

    /**
     * Re-autentica con la contraseña actual y luego actualiza a la nueva.
     */
    fun actualizarContrasena(
        actual: String,
        nuevo: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }
        val cred = EmailAuthProvider.getCredential(user.email ?: "", actual)
        user.reauthenticate(cred)
            .addOnSuccessListener {
                user.updatePassword(nuevo)
                    .addOnSuccessListener {
                        _passwordMasked.value = "********"
                        onComplete(true, null)
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, "Re-autenticación fallida: ${e.localizedMessage}")
            }
    }

    /**
     * Sube la imagen a Storage y actualiza el photoUrl en FirebaseAuth.
     */
    fun actualizarFotoPerfil(uriLocal: Uri, onComplete: (Boolean, String?) -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete(false, "No autenticado")
            return
        }
        val uid        = user.uid
        val storageRef = storage.reference.child("users/$uid/profile.jpg")
        storageRef.putFile(uriLocal)
            .addOnSuccessListener {
                storageRef.downloadUrl
                    .addOnSuccessListener { descargaUri ->
                        val req = UserProfileChangeRequest.Builder()
                            .setPhotoUri(descargaUri)
                            .build()
                        user.updateProfile(req)
                            .addOnSuccessListener {
                                _photoUri.value = descargaUri
                                onComplete(true, null)
                            }
                            .addOnFailureListener { e ->
                                onComplete(false, e.localizedMessage)
                            }
                    }
                    .addOnFailureListener { e ->
                        onComplete(false, e.localizedMessage)
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, e.localizedMessage)
            }
    }

    /** Cierra sesión en FirebaseAuth. */
    fun logout() {
        auth.signOut()
    }
}
