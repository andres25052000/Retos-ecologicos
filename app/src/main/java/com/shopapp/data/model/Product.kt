package com.shopapp.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double,
    val imageUrl: String,
    val category: String,
    val rating: Float,
    val reviewCount: Int,
    val inStock: Boolean = true,
    val isNew: Boolean = false,
    val discount: Int = 0,
    val isFavorite: Boolean = false,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val ecoPoints: Int = 0
) : Parcelable {
    val discountedPrice: Double
        get() = if (discount > 0) price * (1 - discount / 100.0) else price

    val hasDiscount: Boolean
        get() = discount > 0
}
