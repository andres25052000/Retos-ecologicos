package com.shopapp.ui.product

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.shopapp.R
import com.shopapp.data.model.Product
import com.shopapp.databinding.FragmentProductDetailBinding
import com.shopapp.ui.home.MainActivity
import com.shopapp.util.ColorConstants
import com.shopapp.util.toCOP

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private var quantity = 1
    private var selectedSize = ""
    private var selectedColor = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel
        val productId = arguments?.getString("productId") ?: return

        viewModel.selectProduct(productId)

        viewModel.selectedProduct.observe(viewLifecycleOwner) { product ->
            product ?: return@observe
            bindProduct(product, viewModel)
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }
    }

    private fun bindProduct(product: Product, viewModel: com.shopapp.viewmodel.ShopViewModel) {
        binding.tvName.text = product.name
        binding.tvCategory.text = product.category
        binding.tvDescription.text = product.description
        binding.tvRating.text = product.rating.toString()
        binding.tvReviews.text = "(${product.reviewCount} reseñas)"
        binding.tvStock.text = if (product.inStock) "En stock ✓" else "Agotado"
        binding.tvStock.setTextColor(
            if (product.inStock) requireContext().getColor(R.color.success)
            else requireContext().getColor(R.color.error)
        )

        val price = if (product.hasDiscount) product.discountedPrice else product.price
        binding.tvPrice.text = price.toCOP()

        if (product.hasDiscount) {
            binding.tvOriginalPrice.visibility = View.VISIBLE
            val old = product.originalPrice
            binding.tvOriginalPrice.text = old.toCOP()
            binding.tvOriginalPrice.paintFlags = binding.tvOriginalPrice.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            binding.tvDiscount.visibility = View.VISIBLE
            binding.tvDiscount.text = "-${product.discount}%"
        } else {
            binding.tvOriginalPrice.visibility = View.GONE
            binding.tvDiscount.visibility = View.GONE
        }

        if (product.ecoPoints > 0) {
            binding.tvEcoPointsBadge.visibility = View.VISIBLE
            binding.tvEcoPointsBadge.text = "🌱 +${product.ecoPoints} pts eco"
        } else {
            binding.tvEcoPointsBadge.visibility = View.GONE
        }

        // Favorite
        val updateFavIcon = {
            binding.ivFavorite.setImageResource(
                if (viewModel.isInWishlist(product.id)) R.drawable.ic_favorite
                else R.drawable.ic_favorite_border
            )
        }
        updateFavIcon()
        binding.ivFavorite.setOnClickListener {
            viewModel.toggleWishlist(product.id)
            updateFavIcon()
        }

        Glide.with(binding.ivProduct.context)
            .load(product.imageUrl)
            .placeholder(android.R.color.darker_gray)
            .centerCrop()
            .into(binding.ivProduct)

        // Sizes
        if (product.sizes.isNotEmpty()) {
            binding.tvSizeLabel.visibility = View.VISIBLE
            binding.rvSizes.visibility = View.VISIBLE
            selectedSize = product.sizes.first()
            val sizeAdapter = SizeAdapter(product.sizes, selectedSize) { size -> selectedSize = size }
            binding.rvSizes.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = sizeAdapter
            }
        }

        // Colors
        if (product.colors.isNotEmpty()) {
            binding.tvColorLabel.visibility = View.VISIBLE
            binding.rvColors.visibility = View.VISIBLE
            selectedColor = product.colors.first()
            applyColorOverlay(selectedColor)
            val colorAdapter = ColorAdapter(product.colors, selectedColor) { color ->
                selectedColor = color
                applyColorOverlay(color)
            }
            binding.rvColors.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = colorAdapter
            }
        }

        // Quantity
        binding.tvQuantity.text = quantity.toString()
        binding.ivMinus.setOnClickListener {
            if (quantity > 1) { quantity--; binding.tvQuantity.text = quantity.toString() }
        }
        binding.ivPlus.setOnClickListener {
            quantity++; binding.tvQuantity.text = quantity.toString()
        }

        binding.btnAddToCart.setOnClickListener {
            if (!product.inStock) {
                Toast.makeText(requireContext(), "Producto agotado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addToCart(product, quantity, selectedSize, selectedColor)
        }
    }

    /**
     * Pinta el overlay de la imagen con el color seleccionado.
     * [colorName] es el nombre (ej. "Rojo") — busca el HEX en ColorConstants.
     */
    private fun applyColorOverlay(colorName: String) {
        val hex = ColorConstants.hexForName(colorName)
        try {
            val color = Color.parseColor(hex)
            binding.colorOverlay.setBackgroundColor(color)
            binding.colorOverlay.visibility = View.VISIBLE
        } catch (e: Exception) {
            binding.colorOverlay.visibility = View.GONE
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
