package com.shopapp.ui.admin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shopapp.data.model.OrderStatus
import com.shopapp.databinding.ItemAdminOrderBinding
import com.shopapp.util.toCOP
import java.text.SimpleDateFormat
import java.util.*

class AdminOrdersAdapter(
    private var orders: List<Map<String, Any>>,
    private val onStatusChange: (orderId: String, status: String) -> Unit
) : RecyclerView.Adapter<AdminOrdersAdapter.ViewHolder>() {

    private val statusOptions = OrderStatus.values().map { it.name }
    private val statusLabels = OrderStatus.values().map { it.label }

    inner class ViewHolder(private val binding: ItemAdminOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Map<String, Any>) {
            val orderId = order["id"] as? String ?: ""
            val userEmail = order["userEmail"] as? String ?: "—"
            val totalAmount = (order["totalAmount"] as? Number)?.toDouble() ?: 0.0
            val statusStr = order["status"] as? String ?: "CONFIRMED"
            val createdAt = (order["createdAt"] as? Number)?.toLong() ?: 0L
            val itemCount = (order["itemCount"] as? Number)?.toInt() ?: 0
            val ecoPoints = (order["ecoPointsEarned"] as? Number)?.toInt() ?: 0

            binding.tvOrderId.text = "Pedido #${orderId.takeLast(8)}"
            binding.tvUserEmail.text = "👤 $userEmail"
            binding.tvTotal.text = totalAmount.toCOP()
            binding.tvItemsSummary.text = "$itemCount artículo(s)  •  🌱 +$ecoPoints pts eco"

            // Fecha
            if (createdAt > 0) {
                val date = Date(createdAt)
                val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                binding.tvDate.text = formatter.format(date)
            }

            // Badge de estado
            val currentStatus = try { OrderStatus.valueOf(statusStr) } catch (e: Exception) { OrderStatus.CONFIRMED }
            binding.tvStatus.text = currentStatus.label
            binding.tvStatus.setBackgroundColor(currentStatus.colorRes)

            // Spinner de estado
            val spinnerAdapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_spinner_item,
                statusLabels
            ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

            binding.spinnerStatus.adapter = spinnerAdapter
            binding.spinnerStatus.setSelection(statusOptions.indexOf(statusStr).coerceAtLeast(0))

            // Listener del spinner (ignorar el primer disparo automático)
            var firstCall = true
            binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, pos: Int, id: Long) {
                    if (firstCall) { firstCall = false; return }
                    val newStatus = statusOptions[pos]
                    if (newStatus != statusStr) {
                        onStatusChange(orderId, newStatus)
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdminOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(orders[position])

    override fun getItemCount() = orders.size

    fun updateOrders(newOrders: List<Map<String, Any>>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
