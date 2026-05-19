package com.shopapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shopapp.databinding.FragmentAdminDashboardBinding
import com.shopapp.ui.home.MainActivity

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val adminViewModel = mainActivity.adminViewModel

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        // Navegar a productos
        binding.cardProducts.setOnClickListener {
            mainActivity.navigateToAdminProducts()
        }

        // Navegar a pedidos
        binding.cardOrders.setOnClickListener {
            mainActivity.navigateToAdminOrders()
        }

        // Observar stats
        adminViewModel.products.observe(viewLifecycleOwner) { products ->
            binding.tvProductsCount.text = products.size.toString()
        }

        adminViewModel.orders.observe(viewLifecycleOwner) { orders ->
            binding.tvOrdersCount.text = orders.size.toString()
        }

        adminViewModel.users.observe(viewLifecycleOwner) { users ->
            binding.tvUsersCount.text = users.size.toString()
        }

        // Cargar datos
        adminViewModel.loadProducts()
        adminViewModel.loadAllOrders()
        adminViewModel.loadAllUsers()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
