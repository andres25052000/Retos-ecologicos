package com.shopapp

/**
 * Singleton que guarda el estado de la sesión actual del usuario en memoria.
 * Se llena después del login (Firebase Auth + rol de Firestore).
 */
object AppSession {
    var userId: String = ""
    var userEmail: String = ""
    var userRole: String = "customer"   // "admin" | "customer"

    // Fallback: si el correo coincide con el admin también se concede acceso
    val isAdmin: Boolean get() = userRole == "admin" ||
        userEmail.trim().lowercase() == "admin@shopapp.com"

    val isLoggedIn: Boolean get() = userId.isNotBlank()

    fun clear() {
        userId = ""
        userEmail = ""
        userRole = "customer"
    }
}
