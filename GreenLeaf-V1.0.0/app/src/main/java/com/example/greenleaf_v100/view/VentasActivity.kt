package com.example.greenleaf_v100.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ActivityVentasBinding
import com.example.greenleaf_v100.model.PedidosAdapter
import com.example.greenleaf_v100.viewmodel.PedidosViewModel
import com.example.greenleaf_v100.viewmodel.UserType

class VentasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVentasBinding
    private lateinit var adapter: PedidosAdapter
    private val pedidosViewModel: PedidosViewModel by viewModels() // Asegúrate de tener esta clase definida

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityVentasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tipoUsuarioStr = intent.getStringExtra("TIPO_USUARIO")
        val tipoUsuario = tipoUsuarioStr?.let { UserType.valueOf(it) }

        if (tipoUsuario == UserType.ADMIN) {
            // Mostrar opciones admin
            binding.barraAdmin.selectedItemId = R.id.nav_ventas
            binding.barraAdmin.visibility = View.VISIBLE
        } else if (tipoUsuario == UserType.CLIENTE) {
            binding.barraAdmin.visibility = View.INVISIBLE
        }

        binding.barraAdmin.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, CatalogoActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                R.id.nav_ventas -> true // Ya estamos en VentasActivity, no necesitamos recrearla
                R.id.nav_perfil -> {
                    val intent = Intent(this, PerfilActivity::class.java)
                    intent.putExtra("TIPO_USUARIO", tipoUsuario?.name)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Configurar RecyclerView
        adapter = PedidosAdapter(
            onEntregadoClick = { pedidoId -> marcarComoEntregado(pedidoId) },
            onEliminadoClick = { pedidoId -> eliminarPedido(pedidoId) },
            onItemClick = { pedido ->
                val intent = Intent(this, DetallePedidoActivity::class.java).apply {
                    putExtra("PEDIDO", pedido)
                }
                startActivity(intent)
            }
        )
        binding.rvPedidos.adapter = adapter
        binding.rvPedidos.layoutManager = LinearLayoutManager(this)

        // Observar cambios en los pedidos
        pedidosViewModel.pedidos.observe(this) { pedidos ->
            adapter.submitList(pedidos)
        }

        // Cargar pedidos
        pedidosViewModel.cargarPedidos()
    }

    private fun marcarComoEntregado(pedidoId: String) {
        pedidosViewModel.marcarComoEntregado(pedidoId)
    }

    private fun eliminarPedido(pedidoId: String) {
        pedidosViewModel.eliminarPedido(pedidoId)
    }
}