package com.shopapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopapp.AppSession
import com.shopapp.data.model.Order
import com.shopapp.data.model.OrderStatus
import com.shopapp.data.repository.FirestoreOrderRepository
import com.shopapp.databinding.FragmentOrdersBinding
import com.shopapp.ui.home.MainActivity
import java.util.Date

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext())

        loadOrders()
    }

    private fun loadOrders() {
        showLoading()

        val uid = AppSession.userId
        if (uid.isBlank()) {
            showEmpty()
            return
        }

        FirestoreOrderRepository.getUserOrders(uid) { rawOrders ->
            val orders = rawOrders.mapNotNull { map -> parseOrder(map) }
            activity?.runOnUiThread {
                if (orders.isEmpty()) {
                    showEmpty()
                } else {
                    showOrders(orders)
                }
            }
        }
    }

    private fun parseOrder(map: Map<String, Any>): Order? {
        return try {
            val id          = map["id"] as? String ?: return null
            val totalAmount = (map["totalAmount"] as? Number)?.toDouble() ?: 0.0
            val statusName  = map["status"] as? String ?: "PENDING"
            val createdAt   = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            val address     = map["address"] as? String ?: ""
            val ecoPoints   = (map["ecoPointsEarned"] as? Number)?.toInt() ?: 0
            val itemCount   = (map["itemCount"] as? Number)?.toInt() ?: 0

            val status = try {
                OrderStatus.valueOf(statusName)
            } catch (e: IllegalArgumentException) {
                OrderStatus.PENDING
            }

            // Construimos un Order de solo visualización (sin lista de items detallada)
            Order(
                id            = id,
                items         = List(itemCount) { dummyCartItem() },
                totalAmount   = totalAmount,
                status        = status,
                createdAt     = Date(createdAt),
                address       = address,
                ecoPointsEarned = ecoPoints
            )
        } catch (e: Exception) {
            null
        }
    }

    /** Item vacío solo para que itemCount funcione correctamente en el adapter */
    private fun dummyCartItem(): com.shopapp.data.model.CartItem {
        val emptyProduct = com.shopapp.data.model.Product(
            id            = "",
            name          = "",
            description   = "",
            price         = 0.0,
            originalPrice = 0.0,
            imageUrl      = "",
            category      = "",
            rating        = 0f,
            reviewCount   = 0
        )
        return com.shopapp.data.model.CartItem(product = emptyProduct, quantity = 1)
    }

    private fun showLoading() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.layoutEmpty.visibility   = View.GONE
        binding.rvOrders.visibility      = View.GONE
    }

    private fun showEmpty() {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutEmpty.visibility   = View.VISIBLE
        binding.rvOrders.visibility      = View.GONE
    }

    private fun showOrders(orders: List<Order>) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutEmpty.visibility   = View.GONE
        binding.rvOrders.visibility      = View.VISIBLE
        binding.rvOrders.adapter         = OrdersAdapter(orders)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
