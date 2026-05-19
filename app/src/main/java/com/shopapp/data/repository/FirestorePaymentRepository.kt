package com.shopapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shopapp.data.model.CardBrand
import com.shopapp.data.model.PaymentMethod
import com.shopapp.data.model.PaymentType

object FirestorePaymentRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun col(uid: String) =
        db.collection("users").document(uid).collection("paymentMethods")

    // ─── Leer ─────────────────────────────────────────────────────────────────

    fun getAll(uid: String, callback: (List<PaymentMethod>) -> Unit) {
        col(uid).get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents
                    .mapNotNull { doc -> parsePayment(doc.data ?: return@mapNotNull null) }
                    .sortedByDescending { it.isDefault }
                callback(list)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // ─── Agregar ──────────────────────────────────────────────────────────────

    fun add(uid: String, method: PaymentMethod, callback: (List<PaymentMethod>) -> Unit) {
        if (method.isDefault) {
            col(uid).get().addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isDefault", false)
                }
                batch.set(col(uid).document(method.id), paymentToMap(method))
                batch.commit()
                    .addOnSuccessListener { getAll(uid, callback) }
                    .addOnFailureListener { callback(emptyList()) }
            }.addOnFailureListener { callback(emptyList()) }
        } else {
            col(uid).document(method.id).set(paymentToMap(method))
                .addOnSuccessListener { getAll(uid, callback) }
                .addOnFailureListener { callback(emptyList()) }
        }
    }

    // ─── Eliminar ─────────────────────────────────────────────────────────────

    fun delete(uid: String, methodId: String, callback: (List<PaymentMethod>) -> Unit) {
        col(uid).document(methodId).delete()
            .addOnSuccessListener { getAll(uid, callback) }
            .addOnFailureListener { callback(emptyList()) }
    }

    // ─── Establecer predeterminado ────────────────────────────────────────────

    fun setDefault(uid: String, methodId: String, callback: (List<PaymentMethod>) -> Unit) {
        col(uid).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isDefault", doc.id == methodId)
            }
            batch.commit()
                .addOnSuccessListener { getAll(uid, callback) }
                .addOnFailureListener { callback(emptyList()) }
        }.addOnFailureListener { callback(emptyList()) }
    }

    // ─── Serialización ────────────────────────────────────────────────────────

    private fun paymentToMap(m: PaymentMethod): Map<String, Any> = mapOf(
        "id"          to m.id,
        "type"        to m.type.name,
        "cardHolder"  to m.cardHolder,
        "lastFour"    to m.lastFour,
        "brand"       to m.brand.name,
        "expiryMonth" to m.expiryMonth,
        "expiryYear"  to m.expiryYear,
        "bankName"    to m.bankName,
        "isDefault"   to m.isDefault
    )

    private fun parsePayment(map: Map<String, Any>): PaymentMethod? {
        return try {
            val id = map["id"] as? String ?: return null

            val type = try {
                PaymentType.valueOf(map["type"] as? String ?: "CREDIT")
            } catch (e: Exception) { PaymentType.CREDIT }

            val brand = try {
                CardBrand.valueOf(map["brand"] as? String ?: "OTHER")
            } catch (e: Exception) { CardBrand.OTHER }

            PaymentMethod(
                id          = id,
                type        = type,
                cardHolder  = map["cardHolder"] as? String ?: "",
                lastFour    = map["lastFour"] as? String ?: "",
                brand       = brand,
                expiryMonth = map["expiryMonth"] as? String ?: "",
                expiryYear  = map["expiryYear"] as? String ?: "",
                bankName    = map["bankName"] as? String ?: "",
                isDefault   = map["isDefault"] as? Boolean ?: false
            )
        } catch (e: Exception) { null }
    }
}
