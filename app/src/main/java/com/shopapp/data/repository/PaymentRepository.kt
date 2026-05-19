package com.shopapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shopapp.data.model.PaymentMethod

object PaymentRepository {

    private const val PREF_NAME = "shopapp_payments"
    private const val KEY_LIST  = "payment_list"
    private val gson            = Gson()
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getAll(): List<PaymentMethod> {
        val json = prefs.getString(KEY_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<PaymentMethod>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun add(method: PaymentMethod): List<PaymentMethod> {
        val list = getAll().toMutableList()
        val updated = if (method.isDefault) {
            list.map { it.copy(isDefault = false) }.toMutableList()
        } else list
        updated.add(method)
        save(updated)
        return updated
    }

    fun delete(id: String): List<PaymentMethod> {
        val updated = getAll().filter { it.id != id }
        save(updated)
        return updated
    }

    fun setDefault(id: String): List<PaymentMethod> {
        val updated = getAll().map { it.copy(isDefault = it.id == id) }
        save(updated)
        return updated
    }

    fun clear() {
        prefs.edit().remove(KEY_LIST).apply()
    }

    private fun save(list: List<PaymentMethod>) {
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply()
    }
}
