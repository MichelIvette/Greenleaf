package com.example.greenleaf_v100.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.greenleaf_v100.databinding.ItemCarritoBinding
import com.example.greenleaf_v100.model.CartItem

class CartAdapter(
    private val onRemove: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private var items = listOf<CartItem>()

    inner class CartViewHolder(val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CartItem) {
            with(binding) {
                tvNombre.text = item.name
                tvPrecio.text = "$${item.price}"
                Glide.with(ivPlanta.context).load(item.imageUrl).into(ivPlanta)
                btnEliminar.setOnClickListener { onRemove(item) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CartViewHolder(ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) =
        holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    fun submitList(list: List<CartItem>) {
        items = list
        notifyDataSetChanged()
    }
}
