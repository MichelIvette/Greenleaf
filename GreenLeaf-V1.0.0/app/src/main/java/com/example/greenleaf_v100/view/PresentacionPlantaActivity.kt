package com.example.greenleaf_v100.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityPresentacionPlantaBinding
import com.example.greenleaf_v100.model.ModelPlanta
import com.example.greenleaf_v100.viewmodel.CarritoViewModel
import androidx.activity.viewModels
import com.example.greenleaf_v100.model.CartItem
import com.google.firebase.auth.FirebaseAuth

class PresentacionPlantaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresentacionPlantaBinding
    private lateinit var planta: ModelPlanta
    private val cartViewModel: CarritoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPresentacionPlantaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar datos del intent
        val id = intent.getStringExtra("ID") ?: ""
        val nombre = intent.getStringExtra("NOMBRE") ?: ""
        val descripcion = intent.getStringExtra("DESCRIPCION") ?: ""
        val consejo = intent.getStringExtra("CONSEJO") ?: ""
        val tipo = intent.getStringExtra("TIPO") ?: ""
        val precio = intent.getStringExtra("PRECIO") ?: "0.0" // Cambiado a String
        val stock = intent.getIntExtra("STOCK", 0)
        val riego = intent.getStringExtra("RIEGO") ?: ""
        val estancia = intent.getStringExtra("ESTANCIA") ?: ""
        val fotoUrl = intent.getStringExtra("FOTO_URL") ?: ""

        // Crear objeto planta
        planta = ModelPlanta(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            consejo = consejo,
            tipo = tipo,
            precio = precio,
            stock = stock,
            riego = riego,
            estancia = estancia,
            fotoUrl = fotoUrl
        )

        // Mostrar info en pantalla
        mostrarInformacionPlanta()

        // Configurar ViewModel
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("Usuario no logueado")
        cartViewModel.setUserId(uid)

        // Observar cambios en el carrito
        cartViewModel.cartItems.observe(this) { items ->
            val enCarrito = items.any { it.id == planta.id }
            actualizarBotonCarrito(enCarrito)
        }

        // Configurar click del botón
        binding.btnAddCart.setOnClickListener {
            toggleCarrito()
        }
    }

    private fun mostrarInformacionPlanta() {
        with(binding) {
            tvNombreP.text = planta.nombre
            tvDescripcionP.text = planta.descripcion
            tvConsejo.text = planta.consejo
            tvA.text = "Necesita ${planta.tipo}"
            tvB.text = "Ideal para ${planta.estancia}"
            tvC.text = "Ocupa un riego ${planta.riego}"
            tvStockP.text = "Cantidad disponible: ${planta.stock}"

            Glide.with(this@PresentacionPlantaActivity)
                .load(planta.fotoUrl)
                .into(ivPlantaP)

            iconA.setImageResource(getIconoTipo(planta.tipo))
            iconB.setImageResource(getIconoEstancia(planta.estancia))
            iconC.setImageResource(getIconoRiego(planta.riego))
        }
    }

    private fun toggleCarrito() {
        val item = CartItem(
            id = planta.id,
            userId = FirebaseAuth.getInstance().currentUser?.uid ?: return,
            name = planta.nombre,
            price = planta.precio.toDoubleOrNull() ?: 0.0,
            imageUrl = planta.fotoUrl
        )

        val enCarrito = cartViewModel.cartItems.value?.any { it.id == planta.id } ?: false

        if (enCarrito) {
            cartViewModel.removeFromCart(item)
            Toast.makeText(this, "${planta.nombre} eliminada del carrito", Toast.LENGTH_SHORT).show()
        } else {
            cartViewModel.addToCart(item)
            Toast.makeText(this, "${planta.nombre} agregada al carrito", Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarBotonCarrito(enCarrito: Boolean) {
        binding.btnAddCart.text = if (enCarrito) {
            "Eliminar del carrito"
        } else {
            "Añadir al carrito"
        }
    }

    private fun getIconoTipo(tipo: String): Int = when (tipo.lowercase()) {
        "sol" -> R.drawable.icona_sol
        "sombra" -> R.drawable.icona_sombra
        "sombra parcial" -> R.drawable.icona_solysombra
        else -> R.drawable.icona_solysombra
    }

    private fun getIconoEstancia(estancia: String): Int = when (estancia.lowercase()) {
        "interior" -> R.drawable.iconb_interior
        "exterior" -> R.drawable.iconb_exterior
        else -> R.drawable.iconb_exterior
    }

    private fun getIconoRiego(riego: String): Int = when (riego.lowercase()) {
        "alto" -> R.drawable.iconc_altor
        "moderado", "bajo" -> R.drawable.iconc_bajor
        else -> R.drawable.iconc_bajor
    }


}
