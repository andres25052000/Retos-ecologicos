package com.shopapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shopapp.data.model.Order

object FirestoreOrderRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "orders"

    /** Guarda un pedido en Firestore asociado al usuario */
    fun saveOrder(
        order: Order,
        userId: String,
        userEmail: String,
        callback: (Boolean) -> Unit = {}
    ) {
        val itemsData = order.items.map { item ->
            hashMapOf(
                "productId" to item.product.id,
                "productName" to item.product.name,
                "quantity" to item.quantity,
                "unitPrice" to item.product.discountedPrice,
                "totalPrice" to item.totalPrice,
                "imageUrl" to item.product.imageUrl,
                "category" to item.product.category,
                "selectedSize" to item.selectedSize,
                "selectedColor" to item.selectedColor
            )
        }

        val data = hashMapOf(
            "id" to order.id,
            "userId" to userId,
            "userEmail" to userEmail,
            "totalAmount" to order.totalAmount,
            "status" to order.status.name,
            "createdAt" to order.createdAt.time,
            "address" to order.address,
            "ecoPointsEarned" to order.ecoPointsEarned,
            "itemCount" to order.itemCount,
            "items" to itemsData
        )

        db.collection(COLLECTION).document(order.id).set(data)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    /** Obtiene los pedidos de un usuario específico */
    fun getUserOrders(userId: String, callback: (List<Map<String, Any>>) -> Unit) {
        db.collection(COLLECTION)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.documents.mapNotNull { it.data }
                callback(orders)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    /** Obtiene TODOS los pedidos de todos los usuarios (solo admin) */
    fun getAllOrders(callback: (List<Map<String, Any>>) -> Unit) {
        db.collection(COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val orders = snapshot.documents.mapNotNull { it.data }
                callback(orders)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    /** Actualiza el estado de un pedido (solo admin) */
    fun updateOrderStatus(orderId: String, status: String, callback: (Boolean) -> Unit) {
        db.collection(COLLECTION).document(orderId)
            .update("status", status)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
