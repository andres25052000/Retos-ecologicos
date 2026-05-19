package com.shopapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopapp.databinding.FragmentAdminOrdersBinding
import com.shopapp.ui.home.MainActivity

class AdminOrdersFragment : Fragment() {

    private var _binding: FragmentAdminOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val adminViewModel = mainActivity.adminViewModel

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        val ordersAdapter = AdminOrdersAdapter(
            orders = emptyList(),
            onStatusChange = { orderId, status ->
                adminViewModel.updateOrderStatus(orderId, status)
            }
        )

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ordersAdapter
        }

        adminViewModel.orders.observe(viewLifecycleOwner) { orders ->
            ordersAdapter.updateOrders(orders)
            binding.tvOrdersCount.text = "${orders.size}"
            binding.layoutEmpty.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
            binding.rvOrders.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
        }

        adminViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        adminViewModel.message.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            adminViewModel.clearMessage()
        }

        // Cargar pedidos
        adminViewModel.loadAllOrders()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
