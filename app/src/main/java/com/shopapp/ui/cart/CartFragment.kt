package com.shopapp.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopapp.R
import com.shopapp.databinding.FragmentCartBinding
import com.shopapp.ui.home.MainActivity
import com.shopapp.util.toCOP

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel

        val cartAdapter = CartAdapter(
            onQuantityChange = { productId, qty -> viewModel.updateCartQuantity(productId, qty) },
            onDelete = { productId -> viewModel.removeFromCart(productId) }
        )

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }

        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            if (items.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.layoutCart.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.layoutCart.visibility = View.VISIBLE
                cartAdapter.submitList(items.toMutableList())
                binding.tvItemCount.text = "${items.sumOf { it.quantity }} artículos"
            }
        }

        viewModel.cartTotal.observe(viewLifecycleOwner) { total ->
            val shipping = 15900.0
            binding.tvSubtotal.text = total.toCOP()
            binding.tvShipping.text = shipping.toCOP()
            binding.tvTotal.text = (total + shipping).toCOP()
        }

        binding.btnShopNow.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        binding.btnCheckout.setOnClickListener {
            mainActivity.navigateToCheckout()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
