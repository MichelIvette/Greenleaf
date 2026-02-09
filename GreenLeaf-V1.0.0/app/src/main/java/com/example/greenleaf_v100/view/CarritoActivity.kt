package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityCarritoBinding
import com.example.greenleaf_v100.databinding.ActivityCatalogoBinding
import com.example.greenleaf_v100.databinding.ActivityPerfilBinding
import com.example.greenleaf_v100.databinding.ActivityVentasBinding
import com.example.greenleaf_v100.model.CarritoAdapter
import com.example.greenleaf_v100.model.CartItem
import com.example.greenleaf_v100.viewmodel.CarritoViewModel
import com.example.greenleaf_v100.viewmodel.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding

    private lateinit var adapter: CarritoAdapter
    private val cartViewModel: CarritoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }


        if (tipoUsuario == UserType.CLIENTE) {
            // Mostrar opciones admin
            binding.barraCliente.selectedItemId = R.id.nav_carrito
            binding.barraCliente.visibility = View.VISIBLE

        } else if (tipoUsuario == UserType.ADMIN) {

        }

        binding.barraCliente.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }

                R.id.nav_carrito -> true

                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }

                R.id.nav_favoritos -> {
                    val intent = Intent(this, FavoritosActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }


        //Carrito:
        // Configurar RecyclerView
        adapter = CarritoAdapter(
            onItemClick = { item -> /* Manejar click si es necesario */ },
            onRemoveClick = { item -> cartViewModel.removeFromCart(item) }
        )
        binding.reciclerCarrito.adapter = adapter
        binding.reciclerCarrito.layoutManager = LinearLayoutManager(this)

        // Configurar ViewModel
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no logueado")
        cartViewModel.setUserId(uid)

        // Observar cambios en el carrito
        cartViewModel.cartItems.observe(this) { items ->
            adapter.submitList(items)
            calcularTotal(items)
        }

        // Configurar botón de comprar
        binding.btnComprar.setOnClickListener {
            realizarCompra()
        }

    }

    private fun calcularTotal(items: List<CartItem>) {
        val total = items.sumOf { it.price * it.quantity }
        binding.tvTotal.text = "Total: $${"%.2f".format(total)}"
    }

    private fun realizarCompra() {
        val user = FirebaseAuth.getInstance().currentUser
        val uid = user?.uid ?: return
        val userEmail = user.email ?: ""

        // Obtener los items del carrito
        val items = adapter.currentList

        // Crear objeto Pedido
        val pedido = hashMapOf(
            "clienteId" to uid,
            "clienteEmail" to userEmail,
            "items" to items.map {
                hashMapOf(
                    "plantaId" to it.id,
                    "nombre" to it.name,
                    "precio" to it.price,
                    "cantidad" to it.quantity,
                    "imagenUrl" to it.imageUrl
                )
            },
            "fecha" to System.currentTimeMillis(),
            "estado" to "pendiente", // pendiente, entregado, cancelado
            "direccion" to "Dirección del cliente" // Deberías obtener esto de la base de datos
        )

        // Guardar en Firebase
        FirebaseFirestore.getInstance().collection("pedidos")
            .add(pedido)
            .addOnSuccessListener {
                cartViewModel.clearCart()
                Toast.makeText(this, "Compra realizada con éxito", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, OrdenConfirmadaActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al realizar la compra: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
