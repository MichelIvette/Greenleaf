package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.databinding.ActivityLoginBinding
import com.example.greenleaf_v100.viewmodel.LoginViewModel
import com.example.greenleaf_v100.viewmodel.UserType
import java.io.Serializable

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    // Aquí guardamos el tipo tras el login
    private var tipoUsuario: UserType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            // Leer valores
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etPassword.text.toString().trim()

            // Validación local
            var hasError = false
            if (email.isEmpty()) {
                binding.tilEmail.error = "Correo obligatorio"
                hasError = true
            } else {
                binding.tilEmail.error = null
            }
            if (pass.isEmpty()) {
                binding.tilPassword.error = "Contraseña obligatoria"
                hasError = true
            } else {
                binding.tilPassword.error = null
            }
            if (hasError) return@setOnClickListener

            // Llamar al ViewModel
            viewModel.login(email, pass)
        }


        binding.tvRegisterClient.setOnClickListener {
            startActivity(Intent(this, RegistroClientesActivity::class.java))
        }

        viewModel.loginResult.observe(this) { result ->
            if (!result.isSuccess) {
                // Mostrar el error devuelto por el ViewModel bajo contraseña
                binding.tilPassword.error = result.errorMessage
                return@observe
            }

            // Guardamos el tipo y mostramos mensaje
            tipoUsuario = result.userType
            val mensaje = when (tipoUsuario) {
                UserType.ADMIN   -> "El usuario es un administrador"
                UserType.CLIENTE -> "El usuario es un cliente"
                else             -> "Tipo de usuario desconocido"
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()

            // Limpiar errores
            binding.tilPassword.error = null

            // Navegar según tipo
            when (tipoUsuario) {
                UserType.ADMIN -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    finish()
                }
                UserType.CLIENTE -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name) // "CLIENTE"
                    startActivity(intent)
                    finish()
                }
                else -> {
                    binding.tilPassword.error = "Tipo de usuario desconocido"
                }
            }
        }
    }
}
