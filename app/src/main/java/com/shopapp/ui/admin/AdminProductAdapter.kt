package com.shopapp.ui.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shopapp.data.model.Product
import com.shopapp.databinding.ItemAdminProductBinding
import com.shopapp.util.toCOP

class AdminProductAdapter(
    private val onEdit: (Product) -> Unit,
    private val onDelete: (Product) -> Unit
) : ListAdapter<Product, AdminProductAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemAdminProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvName.text = product.name
            binding.tvCategory.text = product.category
            binding.tvPrice.text = product.discountedPrice.toCOP()
            binding.tvEco.text = "🌱 ${product.ecoPoints} pts"

            // Badge de stock
            if (product.inStock) {
                binding.tvStockBadge.text = "En stock"
                binding.tvStockBadge.setBackgroundColor(Color.parseColor("#10B981"))
            } else {
                binding.tvStockBadge.text = "Agotado"
                binding.tvStockBadge.setBackgroundColor(Color.parseColor("#EF4444"))
            }

            Glide.with(binding.ivProduct.context)
                .load(product.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .centerCrop()
                .into(binding.ivProduct)

            binding.ivEdit.setOnClickListener { onEdit(product) }
            binding.ivDelete.setOnClickListener { onDelete(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(o: Product, n: Product) = o.id == n.id
        override fun areContentsTheSame(o: Product, n: Product) = o == n
    }
}
