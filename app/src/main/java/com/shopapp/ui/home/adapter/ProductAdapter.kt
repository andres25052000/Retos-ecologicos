package com.shopapp.ui.home.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.shopapp.data.model.Product
import com.shopapp.databinding.ItemProductBinding
import com.shopapp.util.toCOP

class ProductAdapter(
    private val onProductClick: (Product) -> Unit,
    private val onFavoriteClick: (Product) -> Unit,
    private val onAddToCartClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvName.text = product.name
            binding.tvCategory.text = product.category
            binding.tvRating.text = "${product.rating} (${product.reviewCount})"

            val price = if (product.hasDiscount) product.discountedPrice else product.price
            binding.tvPrice.text = price.toCOP()

            if (product.hasDiscount) {
                binding.tvDiscount.visibility = android.view.View.VISIBLE
                binding.tvDiscount.text = "-${product.discount}%"
            } else {
                binding.tvDiscount.visibility = android.view.View.GONE
            }

            if (product.isNew && !product.hasDiscount) {
                binding.tvNewBadge.visibility = android.view.View.VISIBLE
            } else {
                binding.tvNewBadge.visibility = android.view.View.GONE
            }

            // Favorite icon
            binding.ivFavorite.setImageResource(
                if (product.isFavorite) com.shopapp.R.drawable.ic_favorite
                else com.shopapp.R.drawable.ic_favorite_border
            )

            Glide.with(binding.ivProduct.context)
                .load(product.imageUrl)
                .placeholder(android.R.color.darker_gray)
                .centerCrop()
                .into(binding.ivProduct)

            binding.root.setOnClickListener { onProductClick(product) }
            binding.ivFavorite.setOnClickListener { onFavoriteClick(product) }
            binding.ivAddCart.setOnClickListener { onAddToCartClick(product) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) =
            oldItem == newItem && oldItem.isFavorite == newItem.isFavorite
    }
}
