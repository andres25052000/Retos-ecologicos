package com.shopapp.data.model

data class Address(
    val id: String,
    val label: String,        // Casa, Trabajo, Otro
    val recipientName: String,
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val phone: String,
    val isDefault: Boolean = false
) {
    val fullAddress: String
        get() = "$street, $city, $state $zipCode"
}
