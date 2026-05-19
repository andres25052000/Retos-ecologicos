package com.shopapp.ui.profile

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shopapp.data.model.Order
import com.shopapp.databinding.ItemOrderBinding
import com.shopapp.util.toCOP

class OrdersAdapter(private val orders: List<Order>) :
    RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            binding.tvOrderId.text = "Pedido #${order.id.takeLast(6)}"
            binding.tvDate.text = order.formattedDate
            binding.tvItemCount.text = "${order.itemCount} artículos"
            binding.tvTotal.text = order.totalAmount.toCOP()
            binding.tvStatus.text = order.status.label
            try {
                binding.tvStatus.setBackgroundColor(order.status.colorRes)
            } catch (e: Exception) {
                binding.tvStatus.setBackgroundColor(Color.parseColor("#2D6A4F"))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(orders[position])
    override fun getItemCount() = orders.size
}
