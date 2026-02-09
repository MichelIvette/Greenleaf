package com.example.greenleaf_v100.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.greenleaf_v100.databinding.ActivityCarritoClienteBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.greenleaf_v100.viewmodel.CarritoViewModel

class CarritoClienteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarritoClienteBinding
    private val viewModel: CarritoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Verificar tipo de usuario
        FirebaseFirestore.getInstance()
            .collection("clientes")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { snap ->
                val tipo = snap.getString("tipoUsuario") ?: ""
                if (!tipo.equals("clientes", ignoreCase = true)) {
                    finish()
                    return@addOnSuccessListener
                }

                val adapter = CartAdapter { item ->
                    viewModel.removeFromCart(item)
                    Toast.makeText(this, "Eliminado del carrito", Toast.LENGTH_SHORT).show()
                }
                binding.rvCart.adapter = adapter

                viewModel.cartItems.observe(this) { list ->
                    adapter.submitList(list)
                }
            }
    }
}
