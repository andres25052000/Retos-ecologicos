package com.shopapp.data.model

data class User(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String = "",
    val avatarUrl: String = "",
    val address: String = "",
    val ecoPoints: Int = 0
)
