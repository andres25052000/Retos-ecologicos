package com.shopapp.data.model

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val productCount: Int = 0
)
