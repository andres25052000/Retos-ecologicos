package com.shopapp.data.model

data class PaymentMethod(
    val id: String,
    val type: PaymentType,
    // Campos de tarjeta (solo para CREDIT / DEBIT)
    val cardHolder: String = "",
    val lastFour: String = "",
    val brand: CardBrand = CardBrand.OTHER,
    val expiryMonth: String = "",
    val expiryYear: String = "",
    // Campo PSE
    val bankName: String = "",
    val isDefault: Boolean = false
) {
    val maskedNumber: String
        get() = when (type) {
            PaymentType.CREDIT, PaymentType.DEBIT -> "**** **** **** $lastFour"
            PaymentType.PSE                       -> bankName.ifBlank { "Débito bancario en línea" }
            PaymentType.CASH_ON_DELIVERY          -> "Pago al recibir el pedido"
        }

    val brandIcon: String
        get() = when (type) {
            PaymentType.CREDIT -> when (brand) {
                CardBrand.VISA       -> "💳 Visa Crédito"
                CardBrand.MASTERCARD -> "💳 Mastercard Crédito"
                CardBrand.AMEX       -> "💳 Amex"
                CardBrand.OTHER      -> "💳 Tarjeta Crédito"
            }
            PaymentType.DEBIT -> when (brand) {
                CardBrand.VISA       -> "💳 Visa Débito"
                CardBrand.MASTERCARD -> "💳 Mastercard Débito"
                else                 -> "💳 Tarjeta Débito"
            }
            PaymentType.PSE              -> "🏦 PSE"
            PaymentType.CASH_ON_DELIVERY -> "💵 Efectivo"
        }

    val isCard: Boolean
        get() = type == PaymentType.CREDIT || type == PaymentType.DEBIT
}

enum class PaymentType { CREDIT, DEBIT, PSE, CASH_ON_DELIVERY }
enum class CardBrand    { VISA, MASTERCARD, AMEX, OTHER }
