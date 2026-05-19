package com.shopapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shopapp.data.model.Address

object AddressRepository {

    private const val PREF_NAME   = "shopapp_addresses"
    private const val KEY_LIST    = "address_list"
    private val gson              = Gson()
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getAll(): List<Address> {
        val json = prefs.getString(KEY_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<Address>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun add(address: Address): List<Address> {
        val list = getAll().toMutableList()
        val updated = if (address.isDefault) {
            list.map { it.copy(isDefault = false) }.toMutableList()
        } else list
        updated.add(address)
        save(updated)
        return updated
    }

    fun update(address: Address): List<Address> {
        val list = getAll().toMutableList()
        val updated = if (address.isDefault) {
            list.map { it.copy(isDefault = false) }.toMutableList()
        } else list
        val idx = updated.indexOfFirst { it.id == address.id }
        if (idx >= 0) updated[idx] = address
        save(updated)
        return updated
    }

    fun delete(id: String): List<Address> {
        val updated = getAll().filter { it.id != id }
        save(updated)
        return updated
    }

    fun setDefault(id: String): List<Address> {
        val updated = getAll().map { it.copy(isDefault = it.id == id) }
        save(updated)
        return updated
    }

    fun clear() {
        prefs.edit().remove(KEY_LIST).apply()
    }

    private fun save(list: List<Address>) {
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply()
    }
}
