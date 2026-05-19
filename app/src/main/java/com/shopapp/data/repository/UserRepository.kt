package com.shopapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.shopapp.data.model.User

object UserRepository {

    private const val PREF_NAME = "shopapp_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_USER_ADDRESS = "user_address"
    private const val KEY_ECO_POINTS = "eco_points"
    private const val KEY_PURCHASE_COUNT = "purchase_count"
    private const val KEY_CATEGORIES_BOUGHT = "categories_bought"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun login(email: String, password: String): Result<User> {
        // Simulate auth - accept any valid email/password
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(Exception("Correo y contraseña requeridos"))
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Correo electrónico inválido"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        val user = User(
            id = "user_${System.currentTimeMillis()}",
            fullName = prefs.getString(KEY_USER_NAME, "")?.takeIf { it.isNotBlank() } ?: extractNameFromEmail(email),
            email = email,
            phone = prefs.getString(KEY_USER_PHONE, "") ?: ""
        )
        saveUser(user)
        return Result.success(user)
    }

    fun register(fullName: String, email: String, password: String, phone: String): Result<User> {
        if (fullName.isBlank()) return Result.failure(Exception("El nombre es requerido"))
        if (email.isBlank()) return Result.failure(Exception("El correo es requerido"))
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return Result.failure(Exception("Correo electrónico inválido"))
        }
        if (password.length < 6) {
            return Result.failure(Exception("La contraseña debe tener al menos 6 caracteres"))
        }

        val user = User(
            id = "user_${System.currentTimeMillis()}",
            fullName = fullName,
            email = email,
            phone = phone
        )
        saveUser(user)
        return Result.success(user)
    }

    private fun saveUser(user: User) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.fullName)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_PHONE, user.phone)
            .apply()
    }

    fun getCurrentUser(): User? {
        if (!isLoggedIn()) return null
        return User(
            id = prefs.getString(KEY_USER_ID, "") ?: "",
            fullName = prefs.getString(KEY_USER_NAME, "") ?: "",
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            phone = prefs.getString(KEY_USER_PHONE, "") ?: "",
            address = prefs.getString(KEY_USER_ADDRESS, "") ?: "",
            ecoPoints = prefs.getInt(KEY_ECO_POINTS, 0)
        )
    }

    fun addEcoPoints(points: Int) {
        val current = prefs.getInt(KEY_ECO_POINTS, 0)
        prefs.edit().putInt(KEY_ECO_POINTS, current + points).apply()
    }

    fun getTotalEcoPoints(): Int = prefs.getInt(KEY_ECO_POINTS, 0)

    fun incrementPurchaseCount() {
        val count = prefs.getInt(KEY_PURCHASE_COUNT, 0)
        prefs.edit().putInt(KEY_PURCHASE_COUNT, count + 1).apply()
    }

    fun getPurchaseCount(): Int = prefs.getInt(KEY_PURCHASE_COUNT, 0)

    fun addCategoryBought(category: String) {
        val existing = prefs.getStringSet(KEY_CATEGORIES_BOUGHT, mutableSetOf()) ?: mutableSetOf()
        val updated = existing.toMutableSet().also { it.add(category) }
        prefs.edit().putStringSet(KEY_CATEGORIES_BOUGHT, updated).apply()
    }

    fun getCategoriesBought(): Set<String> =
        prefs.getStringSet(KEY_CATEGORIES_BOUGHT, emptySet()) ?: emptySet()

    fun updateProfile(fullName: String, phone: String, address: String): Result<User> {
        val current = getCurrentUser() ?: return Result.failure(Exception("No hay sesión activa"))
        val updated = current.copy(fullName = fullName, phone = phone, address = address)
        prefs.edit()
            .putString(KEY_USER_NAME, fullName)
            .putString(KEY_USER_PHONE, phone)
            .putString(KEY_USER_ADDRESS, address)
            .apply()
        return Result.success(updated)
    }

    /** Guarda datos de sesión Firebase en SharedPreferences para acceso local rápido */
    fun saveUserFromFirebase(uid: String, name: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, uid)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
    }

    private fun extractNameFromEmail(email: String): String {
        val local = email.substringBefore("@")
        return local.replace(".", " ").replaceFirstChar { it.uppercase() }
    }
}
