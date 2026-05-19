package com.shopapp.data.model

import java.text.SimpleDateFormat
import java.util.*

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    var isRead: Boolean = false,
    /** "product" | "order" | "category" | null  — null = sin navegación */
    val targetType: String? = null,
    /** productId, orderId o nombre de categoría según targetType */
    val targetId: String? = null
) {
    val formattedTime: String
        get() {
            val diff = System.currentTimeMillis() - timestamp
            return when {
                diff < 60_000     -> "Ahora mismo"
                diff < 3_600_000  -> "${diff / 60_000} min"
                diff < 86_400_000 -> "${diff / 3_600_000} h"
                else              -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
            }
        }

    val icon: String
        get() = when (type) {
            NotificationType.ORDER   -> "📦"
            NotificationType.ECO     -> "🌱"
            NotificationType.PROMO   -> "🏷️"
            NotificationType.SYSTEM  -> "🔔"
            NotificationType.PRODUCT -> "🛍️"
        }
}

enum class NotificationType { ORDER, ECO, PROMO, SYSTEM, PRODUCT }
