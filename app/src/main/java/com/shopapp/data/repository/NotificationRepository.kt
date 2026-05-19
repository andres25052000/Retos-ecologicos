package com.shopapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shopapp.data.model.AppNotification
import com.shopapp.data.model.NotificationType

object NotificationRepository {

    private const val PREF_NAME = "shopapp_notifications"
    private const val KEY_LIST  = "notification_list"
    private val gson            = Gson()
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (getAll().isEmpty()) seedInitialNotifications()
    }

    fun getAll(): List<AppNotification> {
        val json = prefs.getString(KEY_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<AppNotification>>() {}.type
        return gson.fromJson<List<AppNotification>>(json, type)
            ?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    fun addOrderNotification(orderId: String, ecoPoints: Int) {
        val list = getAll().toMutableList()
        list.add(0, AppNotification(
            id         = "notif_${System.currentTimeMillis()}",
            title      = "¡Pedido confirmado! 📦",
            message    = "Tu pedido #${orderId.takeLast(6)} fue confirmado. Ganaste $ecoPoints pts eco 🌱",
            type       = NotificationType.ORDER,
            targetType = "order",
            targetId   = orderId
        ))
        save(list)
    }

    /** Nuevo producto agregado por el admin */
    fun addProductAddedNotification(productId: String, productName: String) {
        val list = getAll().toMutableList()
        list.add(0, AppNotification(
            id         = "notif_${System.currentTimeMillis()}",
            title      = "🆕 Nuevo producto disponible",
            message    = "$productName ya está disponible en la tienda",
            type       = NotificationType.PRODUCT,
            targetType = "product",
            targetId   = productId
        ))
        save(list)
    }

    /** Producto actualizado (precio, colores, etc.) */
    fun addProductUpdatedNotification(productId: String, productName: String, detail: String) {
        val list = getAll().toMutableList()
        list.add(0, AppNotification(
            id         = "notif_${System.currentTimeMillis()}",
            title      = "📝 Producto actualizado",
            message    = "$productName: $detail",
            type       = NotificationType.PRODUCT,
            targetType = "product",
            targetId   = productId
        ))
        save(list)
    }

    /** Producto eliminado */
    fun addProductRemovedNotification(productName: String) {
        val list = getAll().toMutableList()
        list.add(0, AppNotification(
            id      = "notif_${System.currentTimeMillis()}",
            title   = "🚫 Producto no disponible",
            message = "$productName ya no está disponible en la tienda",
            type    = NotificationType.PRODUCT
        ))
        save(list)
    }

    fun markRead(id: String) {
        val updated = getAll().map { if (it.id == id) it.copy(isRead = true) else it }
        save(updated)
    }

    fun markAllRead() {
        val updated = getAll().map { it.copy(isRead = true) }
        save(updated)
    }

    fun delete(id: String) {
        val updated = getAll().filter { it.id != id }
        save(updated)
    }

    fun clearAll() {
        prefs.edit().remove(KEY_LIST).apply()
    }

    fun unreadCount(): Int = getAll().count { !it.isRead }

    private fun seedInitialNotifications() {
        val seed = listOf(
            AppNotification(
                id        = "notif_welcome",
                title     = "¡Bienvenido a EcoShop! 🌿",
                message   = "Gracias por unirte. Cada compra suma puntos eco y ayuda al planeta.",
                type      = NotificationType.SYSTEM,
                timestamp = System.currentTimeMillis() - 86_400_000
            ),
            AppNotification(
                id        = "notif_promo1",
                title     = "Oferta especial 🏷️",
                message   = "25% OFF en toda la categoría Hogar esta semana. ¡Aprovéchalo!",
                type      = NotificationType.PROMO,
                timestamp = System.currentTimeMillis() - 3_600_000
            ),
            AppNotification(
                id        = "notif_eco1",
                title     = "¡Consejo ecológico! 🌱",
                message   = "Usar una botella reutilizable puede eliminar hasta 500 botellas plásticas al año.",
                type      = NotificationType.ECO,
                timestamp = System.currentTimeMillis() - 7_200_000
            )
        )
        save(seed)
    }

    private fun save(list: List<AppNotification>) {
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply()
    }
}
