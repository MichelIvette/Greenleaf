package com.example.greenleaf_v100.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityDetallePedidoBinding
import com.example.greenleaf_v100.model.ItemsPedidoAdapter
import com.example.greenleaf_v100.model.Pedido

class DetallePedidoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetallePedidoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetallePedidoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el pedido pasado como extra
        val pedido = intent.getParcelableExtra<Pedido>("PEDIDO")
            ?: throw IllegalStateException("Pedido no proporcionado")

        // Mostrar los datos del pedido
        mostrarDetallesPedido(pedido)
    }

    private fun mostrarDetallesPedido(pedido: Pedido) {
        // Mostrar información básica del cliente
        binding.tvNombreCliente.text = "${pedido.clienteEmail.substringBefore("@")}"
        binding.tvCorreoCliente.text = "${pedido.clienteEmail}"
        binding.tvDireccionCliente.text = "${pedido.direccion}"

        // Mostrar total
        val total = pedido.items.sumOf { it.precio * it.cantidad }
        binding.tvTotalPagar.text = "$${"%.2f".format(total)}"

        // Mostrar lista de plantas compradas
        val adapter = ItemsPedidoAdapter()
        binding.rvItemsPedido.adapter = adapter
        binding.rvItemsPedido.layoutManager = LinearLayoutManager(this)
        adapter.submitList(pedido.items)
    }
}