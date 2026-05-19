package com.shopapp.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.shopapp.databinding.FragmentCategoryProductsBinding
import com.shopapp.ui.home.MainActivity
import com.shopapp.ui.home.adapter.ProductAdapter

class CategoryProductsFragment : Fragment() {

    private var _binding: FragmentCategoryProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel
        val categoryName = arguments?.getString("categoryName") ?: "Todos"

        binding.tvCategoryTitle.text = categoryName
        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        val adapter = ProductAdapter(
            onProductClick = { mainActivity.navigateToProductDetail(it.id) },
            onFavoriteClick = { viewModel.toggleWishlist(it.id) },
            onAddToCartClick = { viewModel.addToCart(it) }
        )

        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            this.adapter = adapter
        }

        viewModel.loadCategoryProducts(categoryName)
        viewModel.categoryProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
