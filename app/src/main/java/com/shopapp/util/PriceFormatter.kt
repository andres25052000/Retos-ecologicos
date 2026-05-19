package com.shopapp.util

fun Double.toCOP(): String {
    val amount = this.toLong()
    val formatted = String.format("%,d", amount).replace(",", ".")
    return "$ $formatted"
}
