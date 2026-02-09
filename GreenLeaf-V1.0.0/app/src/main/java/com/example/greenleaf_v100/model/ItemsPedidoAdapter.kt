package com.example.greenleaf_v100.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.databinding.ItemItemPedidoBinding

class ItemsPedidoAdapter :
    ListAdapter<ItemPedido, ItemsPedidoAdapter.ItemViewHolder>(DiffCallback()) {

    inner class ItemViewHolder(private val binding: ItemItemPedidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemPedido) {
            binding.tvNombrePlanta.text = item.nombre
            binding.tvPrecio.text = "$${"%.2f".format(item.precio)} x${item.cantidad}"

            Glide.with(binding.ivPlanta.context)
                .load(item.imagenUrl)
                .into(binding.ivPlanta)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemItemPedidoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemPedido>() {
        override fun areItemsTheSame(oldItem: ItemPedido, newItem: ItemPedido) =
            oldItem.plantaId == newItem.plantaId

        override fun areContentsTheSame(oldItem: ItemPedido, newItem: ItemPedido) =
            oldItem == newItem
    }
}
