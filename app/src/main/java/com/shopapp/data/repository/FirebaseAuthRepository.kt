package com.shopapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseAuthRepository {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun login(email: String, password: String, callback: (Result<FirebaseUser>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                callback(Result.success(result.user!!))
            }
            .addOnFailureListener { e ->
                val msg = when {
                    e.message?.contains("password", true) == true ||
                    e.message?.contains("credential", true) == true ->
                        "Correo o contraseña incorrectos"
                    e.message?.contains("no user", true) == true ||
                    e.message?.contains("user not found", true) == true ->
                        "No existe una cuenta con este correo"
                    e.message?.contains("network", true) == true ->
                        "Error de conexión. Verifica tu internet"
                    e.message?.contains("disabled", true) == true ->
                        "Esta cuenta ha sido desactivada"
                    else -> "Error al iniciar sesión"
                }
                callback(Result.failure(Exception(msg)))
            }
    }

    fun register(email: String, password: String, callback: (Result<FirebaseUser>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                callback(Result.success(result.user!!))
            }
            .addOnFailureListener { e ->
                val msg = when {
                    e.message?.contains("email already", true) == true ||
                    e.message?.contains("already in use", true) == true ->
                        "Ya existe una cuenta con este correo"
                    e.message?.contains("network", true) == true ->
                        "Error de conexión. Verifica tu internet"
                    e.message?.contains("weak-password", true) == true ->
                        "La contraseña debe tener al menos 6 caracteres"
                    else -> "Error al registrarse"
                }
                callback(Result.failure(Exception(msg)))
            }
    }

    fun logout() {
        auth.signOut()
    }

    fun sendPasswordReset(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { callback(true, "Se envió un enlace de recuperación a $email") }
            .addOnFailureListener { callback(false, "No se pudo enviar el correo. Verifica la dirección.") }
    }
}
