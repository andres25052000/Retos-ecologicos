package com.shopapp.util

/**
 * Lista centralizada de colores disponibles para los productos.
 * El campo [name] es lo que se guarda en Firestore y se muestra al usuario.
 * El campo [hex]  se usa solo para pintar el círculo de color en pantalla.
 */
object ColorConstants {

    data class ColorOption(val name: String, val hex: String)

    val ALL_COLORS = listOf(
        ColorOption("Blanco",        "#FAFAFA"),
        ColorOption("Negro",         "#212121"),
        ColorOption("Gris",          "#9E9E9E"),
        ColorOption("Beige",         "#D7CCC8"),
        ColorOption("Rojo",          "#E53935"),
        ColorOption("Rosa",          "#EC407A"),
        ColorOption("Morado",        "#8E24AA"),
        ColorOption("Azul marino",   "#1A237E"),
        ColorOption("Azul",          "#1E88E5"),
        ColorOption("Celeste",       "#29B6F6"),
        ColorOption("Verde",         "#43A047"),
        ColorOption("Verde oscuro",  "#2E7D32"),
        ColorOption("Amarillo",      "#FDD835"),
        ColorOption("Naranja",       "#FB8C00"),
        ColorOption("Marrón",        "#6D4C41"),
        ColorOption("Dorado",        "#FFD600"),
        ColorOption("Plateado",      "#B0BEC5")
    )

    /** Devuelve el HEX de un color a partir de su nombre (ignora mayúsculas). */
    fun hexForName(name: String): String =
        ALL_COLORS.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }?.hex ?: "#9E9E9E"

    /** Devuelve el nombre de un color a partir de su HEX (para migrar datos viejos). */
    fun nameForHex(hex: String): String? =
        ALL_COLORS.firstOrNull { it.hex.equals(hex.trim(), ignoreCase = true) }?.name
}
