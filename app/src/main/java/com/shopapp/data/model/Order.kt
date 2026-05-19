package com.shopapp.data.model

import java.text.SimpleDateFormat
import java.util.*

data class Order(
    val id: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val status: OrderStatus,
    val createdAt: Date = Date(),
    val address: String = "",
    val ecoPointsEarned: Int = 0
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(createdAt)

    val itemCount: Int
        get() = items.sumOf { it.quantity }
}

enum class OrderStatus(val label: String, val colorRes: Int) {
    PENDING("Pendiente", 0xFFF59E0B.toInt()),
    CONFIRMED("Confirmado", 0xFF3B82F6.toInt()),
    SHIPPED("Enviado", 0xFF8B5CF6.toInt()),
    DELIVERED("Entregado", 0xFF10B981.toInt()),
    CANCELLED("Cancelado", 0xFFEF4444.toInt())
}
