package com.shopapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.shopapp.databinding.FragmentWishlistBinding
import com.shopapp.ui.home.MainActivity
import com.shopapp.ui.home.adapter.ProductAdapter

class WishlistFragment : Fragment() {

    private var _binding: FragmentWishlistBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWishlistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        val adapter = ProductAdapter(
            onProductClick  = { mainActivity.navigateToProductDetail(it.id) },
            onFavoriteClick = { product ->
                viewModel.toggleWishlist(product.id)
            },
            onAddToCartClick = { viewModel.addToCart(it) }
        )

        binding.rvWishlist.apply {
            layoutManager = GridLayoutManager(context, 2)
            this.adapter = adapter
        }

        viewModel.wishlistProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            binding.tvCount.text = "${products.size} artículos"
            if (products.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvWishlist.visibility  = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvWishlist.visibility  = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
