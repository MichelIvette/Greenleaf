package com.example.greenleaf_v100.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

enum class UserType { ADMIN, CLIENTE }

data class LoginResult(
    val isSuccess: Boolean,
    val userType: UserType? = null,
    val profileData: Map<String, Any>? = null,
    val errorMessage: String? = null
)

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = LoginResult(
                isSuccess = false,
                errorMessage = "Correo y contraseña son obligatorios"
            )
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authRes ->
                val uid = authRes.user?.uid ?: run {
                    _loginResult.value = LoginResult(false, errorMessage = "UID inválido")
                    return@addOnSuccessListener
                }

                // Verificar administrador
                db.collection("admins").document(uid).get()
                    .addOnSuccessListener { docAdmin ->
                        if (docAdmin.exists()) {
                            val deleted = docAdmin.getBoolean("deleted") ?: false
                            if (deleted) {
                                auth.signOut()
                                _loginResult.value = LoginResult(
                                    isSuccess = false,
                                    errorMessage = "Cuenta de administrador eliminada"
                                )
                            } else {
                                _loginResult.value = LoginResult(
                                    isSuccess = true,
                                    userType = UserType.ADMIN,
                                    profileData = docAdmin.data
                                )
                            }
                        } else {
                            // Verificar cliente
                            db.collection("clientes").document(uid).get()
                                .addOnSuccessListener { docCli ->
                                    if (docCli.exists()) {
                                        val deleted = docCli.getBoolean("deleted") ?: false
                                        if (deleted) {
                                            auth.signOut()
                                            _loginResult.value = LoginResult(
                                                isSuccess = false,
                                                errorMessage = "Cuenta de cliente eliminada"
                                            )
                                        } else {
                                            _loginResult.value = LoginResult(
                                                isSuccess = true,
                                                userType = UserType.CLIENTE,
                                                profileData = docCli.data
                                            )
                                        }
                                    } else {
                                        auth.signOut()
                                        _loginResult.value = LoginResult(
                                            isSuccess = false,
                                            errorMessage = "Usuario no encontrado"
                                        )
                                    }
                                }
                                .addOnFailureListener { e ->
                                    auth.signOut()
                                    _loginResult.value = LoginResult(
                                        isSuccess = false,
                                        errorMessage = e.localizedMessage
                                    )
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        auth.signOut()
                        _loginResult.value = LoginResult(
                            isSuccess = false,
                            errorMessage = e.localizedMessage
                        )
                    }
            }
            .addOnFailureListener { e ->
                // Mapear errores de FirebaseAuth a mensajes en español
                val msg = when (e) {
                    is FirebaseAuthInvalidUserException ->
                        when (e.errorCode) {
                            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo."
                            "ERROR_USER_DISABLED"  -> "La cuenta ha sido desactivada."
                            else                   -> "Usuario inválido."
                        }
                    is FirebaseAuthInvalidCredentialsException ->
                        when (e.errorCode) {
                            "ERROR_WRONG_PASSWORD" -> "Contraseña incorrecta."
                            "ERROR_INVALID_EMAIL"  -> "Formato de correo inválido."
                            else                   -> "El correo o contraseña es incorrecto."
                        }
                    is FirebaseNetworkException ->
                        "Error de red. Verifica tu conexión."
                    else ->
                        "Error de autenticación. Intenta de nuevo."
                }
                _loginResult.value = LoginResult(false, errorMessage = msg)
            }
    }
}
