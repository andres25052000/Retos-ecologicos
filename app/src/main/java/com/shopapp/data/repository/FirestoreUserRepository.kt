package com.shopapp.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreUserRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "users"

    /**
     * Correo que automáticamente recibe el rol "admin" al registrarse.
     * Cámbialo por el correo que usarás como administrador.
     * También puedes asignar el rol manualmente en Firebase Console
     * editando el campo "role" del documento del usuario en la colección "users".
     */
    const val ADMIN_EMAIL = "admin@shopapp.com"

    /**
     * Crea el perfil del usuario en Firestore después del registro.
     * El correo ADMIN_EMAIL recibe automáticamente rol "admin".
     */
    fun createUserProfile(
        uid: String,
        name: String,
        email: String,
        phone: String = "",
        callback: (role: String) -> Unit
    ) {
        val role = if (email.trim().lowercase() == ADMIN_EMAIL.lowercase()) "admin" else "customer"
        val data = hashMapOf(
            "uid" to uid,
            "fullName" to name,
            "email" to email,
            "phone" to phone,
            "role" to role,
            "ecoPoints" to 0,
            "purchaseCount" to 0,
            "createdAt" to System.currentTimeMillis()
        )
        db.collection(COLLECTION).document(uid).set(data)
            .addOnSuccessListener { callback(role) }
            .addOnFailureListener { callback(role) }
    }

    /** Obtiene solo el rol del usuario (llamada rápida para el splash) */
    fun getUserRole(uid: String, callback: (String) -> Unit) {
        db.collection(COLLECTION).document(uid).get()
            .addOnSuccessListener { doc ->
                callback(doc.getString("role") ?: "customer")
            }
            .addOnFailureListener { callback("customer") }
    }

    /** Obtiene el perfil completo del usuario */
    fun getUserProfile(uid: String, callback: (Map<String, Any>?) -> Unit) {
        db.collection(COLLECTION).document(uid).get()
            .addOnSuccessListener { doc -> callback(doc.data) }
            .addOnFailureListener { callback(null) }
    }

    /** Lista todos los usuarios (solo admin) */
    fun getAllUsers(callback: (List<Map<String, Any>>) -> Unit) {
        db.collection(COLLECTION)
            .orderBy("createdAt")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents.mapNotNull { it.data?.also { d -> d["uid"] = it.id } }
                callback(users)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    /** Incrementa los eco puntos del usuario en Firestore */
    fun addEcoPoints(uid: String, points: Int) {
        if (uid.isBlank()) return
        db.collection(COLLECTION).document(uid)
            .update("ecoPoints", FieldValue.increment(points.toLong()))
    }

    /** Incrementa el contador de compras */
    fun incrementPurchaseCount(uid: String) {
        if (uid.isBlank()) return
        db.collection(COLLECTION).document(uid)
            .update("purchaseCount", FieldValue.increment(1))
    }

    /** Cambia el rol de un usuario (solo admin) */
    fun setUserRole(uid: String, role: String, callback: (Boolean) -> Unit) {
        db.collection(COLLECTION).document(uid)
            .update("role", role)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
