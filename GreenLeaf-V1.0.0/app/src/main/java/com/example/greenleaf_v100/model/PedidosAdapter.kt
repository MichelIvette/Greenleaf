package com.example.greenleaf_v100.model

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.R
import com.example.greenleaf_v100.databinding.ItemPedidoBinding
import android.view.LayoutInflater

class PedidosAdapter(
    private val onEntregadoClick: (String) -> Unit,
    private val onEliminadoClick: (String) -> Unit,
    private val onItemClick: (Pedido) -> Unit
) : ListAdapter<Pedido, PedidosAdapter.PedidoViewHolder>(PedidoDiffCallback()) {

    inner class PedidoViewHolder(private val binding: ItemPedidoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pedido: Pedido) {
            // Aquí configuramos la vista con los datos del pedido
            binding.tvNombre.text = "Pedido #${pedido.id.take(8)}"
            binding.tvDireccion.text = pedido.direccion
            binding.tvCorreo.text = pedido.clienteEmail

            // Cargar imagen del cliente (puedes usar una imagen por defecto o cargarla desde Firebase)
            Glide.with(binding.ivCliente.context)
                .load(R.drawable.icon_usuario) // Imagen por defecto
                .circleCrop()
                .into(binding.ivCliente)


            binding.root.setOnClickListener { onItemClick(pedido) }

            binding.btnEntregado.setOnClickListener {
                onEntregadoClick(pedido.id)
            }

            binding.btnEliminado.setOnClickListener {
                onEliminadoClick(pedido.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PedidoDiffCallback : DiffUtil.ItemCallback<Pedido>() {
        override fun areItemsTheSame(oldItem: Pedido, newItem: Pedido): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pedido, newItem: Pedido): Boolean {
            return oldItem == newItem
        }
    }
}