package com.shopapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shopapp.data.model.Address

object FirestoreAddressRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun col(uid: String) =
        db.collection("users").document(uid).collection("addresses")

    // ─── Leer ─────────────────────────────────────────────────────────────────

    fun getAll(uid: String, callback: (List<Address>) -> Unit) {
        col(uid).get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents
                    .mapNotNull { doc -> parseAddress(doc.data ?: return@mapNotNull null) }
                    .sortedByDescending { it.isDefault }
                callback(list)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // ─── Agregar ──────────────────────────────────────────────────────────────

    fun add(uid: String, address: Address, callback: (List<Address>) -> Unit) {
        if (address.isDefault) {
            // Primero quitar isDefault de todos, luego agregar el nuevo
            col(uid).get().addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "isDefault", false)
                }
                batch.set(col(uid).document(address.id), addressToMap(address))
                batch.commit()
                    .addOnSuccessListener { getAll(uid, callback) }
                    .addOnFailureListener { callback(emptyList()) }
            }.addOnFailureListener { callback(emptyList()) }
        } else {
            col(uid).document(address.id).set(addressToMap(address))
                .addOnSuccessListener { getAll(uid, callback) }
                .addOnFailureListener { callback(emptyList()) }
        }
    }

    // ─── Eliminar ─────────────────────────────────────────────────────────────

    fun delete(uid: String, addressId: String, callback: (List<Address>) -> Unit) {
        col(uid).document(addressId).delete()
            .addOnSuccessListener { getAll(uid, callback) }
            .addOnFailureListener { callback(emptyList()) }
    }

    // ─── Establecer predeterminada ────────────────────────────────────────────

    fun setDefault(uid: String, addressId: String, callback: (List<Address>) -> Unit) {
        col(uid).get().addOnSuccessListener { snapshot ->
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isDefault", doc.id == addressId)
            }
            batch.commit()
                .addOnSuccessListener { getAll(uid, callback) }
                .addOnFailureListener { callback(emptyList()) }
        }.addOnFailureListener { callback(emptyList()) }
    }

    // ─── Serialización ────────────────────────────────────────────────────────

    private fun addressToMap(a: Address): Map<String, Any> = mapOf(
        "id"            to a.id,
        "label"         to a.label,
        "recipientName" to a.recipientName,
        "street"        to a.street,
        "city"          to a.city,
        "state"         to a.state,
        "zipCode"       to a.zipCode,
        "phone"         to a.phone,
        "isDefault"     to a.isDefault
    )

    private fun parseAddress(map: Map<String, Any>): Address? {
        return try {
            val id = map["id"] as? String ?: return null
            Address(
                id            = id,
                label         = map["label"] as? String ?: "Otro",
                recipientName = map["recipientName"] as? String ?: "",
                street        = map["street"] as? String ?: "",
                city          = map["city"] as? String ?: "",
                state         = map["state"] as? String ?: "",
                zipCode       = map["zipCode"] as? String ?: "",
                phone         = map["phone"] as? String ?: "",
                isDefault     = map["isDefault"] as? Boolean ?: false
            )
        } catch (e: Exception) { null }
    }
}
