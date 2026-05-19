package com.shopapp.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shopapp.data.model.CartItem
import com.shopapp.databinding.ItemCartBinding
import com.shopapp.util.toCOP

class CartAdapter(
    private val onQuantityChange: (String, Int) -> Unit,
    private val onDelete: (String) -> Unit
) : ListAdapter<CartItem, CartAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            binding.tvName.text = item.product.name
            binding.tvPrice.text = item.totalPrice.toCOP()
            binding.tvQuantity.text = item.quantity.toString()

            val variant = listOfNotNull(
                item.selectedSize.takeIf { it.isNotBlank() }?.let { "Talla: $it" },
                item.selectedColor.takeIf { it.isNotBlank() }?.let { "Color: $it" }
            ).joinToString(" · ")

            if (variant.isNotBlank()) {
                binding.tvVariant.visibility = View.VISIBLE
                binding.tvVariant.text = variant
            } else {
                binding.tvVariant.visibility = View.GONE
            }

            Glide.with(binding.ivProduct.context)
                .load(item.product.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .centerCrop()
                .into(binding.ivProduct)

            binding.ivMinus.setOnClickListener {
                onQuantityChange(item.product.id, item.quantity - 1)
            }
            binding.ivPlus.setOnClickListener {
                onQuantityChange(item.product.id, item.quantity + 1)
            }
            binding.ivDelete.setOnClickListener {
                onDelete(item.product.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(o: CartItem, n: CartItem) =
            o.product.id == n.product.id && o.selectedSize == n.selectedSize && o.selectedColor == n.selectedColor
        override fun areContentsTheSame(o: CartItem, n: CartItem) = o == n
    }
}
