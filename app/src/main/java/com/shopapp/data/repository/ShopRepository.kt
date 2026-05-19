package com.shopapp.data.repository

import com.shopapp.data.model.*
import java.util.*

object ShopRepository {

    // ─── Sample Products ───────────────────────────────────────────────────────

    private val sampleProducts = mutableListOf(
        Product(
            id = "1", name = "Botella Acero Inoxidable", category = "Hogar",
            description = "Botella reutilizable de acero inoxidable de doble pared. Mantiene bebidas frías 24h y calientes 12h. Sin BPA, libre de plástico. Reduce hasta 500 botellas desechables al año.",
            price = 119900.0, originalPrice = 119900.0,
            imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400&h=400&fit=crop",
            rating = 4.9f, reviewCount = 512, isNew = true, discount = 25,
            colors = listOf("Verde", "Negro", "Blanco", "Gris"),
            ecoPoints = 50
        ),
        Product(
            id = "2", name = "Camiseta Algodón Orgánico", category = "Ropa",
            description = "Camiseta confeccionada 100% con algodón orgánico certificado GOTS. Cultivo sin pesticidas ni químicos. Tinte vegetal natural, suave y transpirable. Producción justa y sostenible.",
            price = 129900.0, originalPrice = 129900.0,
            imageUrl = "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop",
            rating = 4.6f, reviewCount = 189,
            sizes = listOf("XS", "S", "M", "L", "XL", "XXL"),
            colors = listOf("Marrón", "Blanco", "Verde", "Gris"),
            ecoPoints = 35
        ),
        Product(
            id = "3", name = "Kit Cocina Bambú", category = "Hogar",
            description = "Set de utensilios de cocina fabricados en bambú sostenible: espátula, cuchara, tenedor y palillos. Antibacterial natural, resistente al calor. Alternativa 100% biodegradable al plástico.",
            price = 99900.0, originalPrice = 99900.0,
            imageUrl = "https://images.unsplash.com/photo-1590736969596-f3dcba124f2c?w=400&h=400&fit=crop",
            rating = 4.7f, reviewCount = 278, discount = 24, isNew = true,
            colors = listOf("Marrón", "Beige"),
            ecoPoints = 45
        ),
        Product(
            id = "4", name = "Mochila Lona Reciclada", category = "Accesorios",
            description = "Mochila fabricada con lona de algodón reciclado y correas de cinta reciclada. Capacidad 25L, bolsillo para laptop 15\", cierres metálicos duraderos. Huella de carbono reducida en un 60%.",
            price = 239900.0, originalPrice = 239900.0,
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400&h=400&fit=crop",
            rating = 4.5f, reviewCount = 134, discount = 25,
            colors = listOf("Marrón", "Negro", "Verde"),
            ecoPoints = 40
        ),
        Product(
            id = "5", name = "Panel Solar Portátil", category = "Energía",
            description = "Panel solar plegable de 20W con cargador USB-A y USB-C. Eficiencia del 23%, ideal para camping y viajes. Carga smartphones, tablets y powerbanks sin electricidad de red.",
            price = 329900.0, originalPrice = 329900.0,
            imageUrl = "https://images.unsplash.com/photo-1509391366360-2e959784a276?w=400&h=400&fit=crop",
            rating = 4.8f, reviewCount = 321, isNew = true,
            colors = listOf("Negro", "Gris"),
            ecoPoints = 80
        ),
        Product(
            id = "6", name = "Jeans Denim Reciclado", category = "Ropa",
            description = "Jeans fabricados con 80% denim reciclado post-consumo. Proceso de producción con 70% menos agua que el denim convencional. Corte slim cómodo, resistente y de bajo impacto ambiental.",
            price = 199900.0, originalPrice = 199900.0,
            imageUrl = "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=400&fit=crop",
            rating = 4.4f, reviewCount = 167, discount = 21,
            sizes = listOf("28", "30", "32", "34", "36", "38"),
            colors = listOf("Negro", "Gris", "Azul"),
            ecoPoints = 30
        ),
        Product(
            id = "7", name = "Jabón Natural Artesanal", category = "Cuidado Personal",
            description = "Jabón sólido artesanal elaborado con aceites esenciales de lavanda, árbol de té y manteca de karité. Sin sulfatos, sin parabenos, sin microplásticos. Envase 100% compostable.",
            price = 45900.0, originalPrice = 45900.0,
            imageUrl = "https://images.unsplash.com/photo-1600857544200-b2f468e04b4a?w=400&h=400&fit=crop",
            rating = 4.7f, reviewCount = 445, ecoPoints = 25
        ),
        Product(
            id = "8", name = "Bolsas Compra Reutilizables", category = "Accesorios",
            description = "Set de 5 bolsas de red de algodón orgánico en diferentes tamaños. Resistentes hasta 10kg, lavables a máquina. Perfectas para frutas, verduras y compras a granel. Eliminan el uso de plástico.",
            price = 79900.0, originalPrice = 99900.0,
            imageUrl = "https://images.unsplash.com/photo-1591195853828-11db59a44f43?w=400&h=400&fit=crop",
            rating = 4.6f, reviewCount = 389, discount = 20,
            colors = listOf("Blanco", "Verde", "Marrón"),
            ecoPoints = 30
        ),
        Product(
            id = "9", name = "Zapatillas Cáñamo Natural", category = "Calzado",
            description = "Zapatillas confeccionadas con fibra de cáñamo natural y suela de caucho reciclado. Transpirables, ligeras y biodegradables. Cultivo de cáñamo sin pesticidas y de bajo consumo hídrico.",
            price = 239900.0, originalPrice = 299900.0,
            imageUrl = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400&h=400&fit=crop",
            rating = 4.5f, reviewCount = 98, discount = 20, isNew = true,
            sizes = listOf("36", "37", "38", "39", "40", "41", "42", "43"),
            colors = listOf("Marrón", "Negro", "Verde"),
            ecoPoints = 40
        ),
        Product(
            id = "10", name = "Compostador Doméstico", category = "Hogar",
            description = "Compostador compacto para uso doméstico. Convierte residuos orgánicos en abono en 4 semanas. Capacidad 15L, sistema de ventilación y filtro de carbono anti-olores. Incluye guía de compostaje.",
            price = 139900.0, originalPrice = 169900.0,
            imageUrl = "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=400&h=400&fit=crop",
            rating = 4.3f, reviewCount = 211, discount = 18, isNew = true,
            colors = listOf("Verde", "Negro"),
            ecoPoints = 60
        ),
        Product(
            id = "11", name = "Chaqueta Polar Reciclado", category = "Ropa",
            description = "Chaqueta de forro polar fabricada con 100% botellas PET recicladas. Cada prenda reutiliza 30 botellas plásticas. Cálida, ligera e impermeable. Certificación Global Recycled Standard.",
            price = 249900.0, originalPrice = 249900.0,
            imageUrl = "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=400&h=400&fit=crop",
            rating = 4.7f, reviewCount = 156, isNew = true,
            sizes = listOf("XS", "S", "M", "L", "XL"),
            colors = listOf("Verde", "Negro", "Gris"),
            ecoPoints = 45
        ),
        Product(
            id = "12", name = "Crema Solar Mineral Bio", category = "Cuidado Personal",
            description = "Protector solar SPF50 de fórmula mineral con óxido de zinc. Biodegradable y segura para arrecifes de coral. Sin químicos dañinos, sin nanopartículas. Envase de aluminio 100% reciclable.",
            price = 77900.0, originalPrice = 99900.0,
            imageUrl = "https://images.unsplash.com/photo-1556228720-195a672e8a03?w=400&h=400&fit=crop",
            rating = 4.8f, reviewCount = 287, discount = 22, ecoPoints = 20
        )
    )

    private val cartItems = mutableListOf<CartItem>()
    private val wishlistIds = mutableSetOf<String>()
    private val orders = mutableListOf<Order>()

    // ─── Products ──────────────────────────────────────────────────────────────

    /** Reemplaza los productos locales con los de Firestore */
    fun syncFromFirestore(products: List<Product>) {
        sampleProducts.clear()
        sampleProducts.addAll(products)
    }

    fun getAllProducts(): List<Product> = sampleProducts.toList()

    fun getProductById(id: String): Product? = sampleProducts.find { it.id == id }

    fun getProductsByCategory(category: String): List<Product> =
        sampleProducts.filter { it.category == category }

    fun getFeaturedProducts(): List<Product> =
        sampleProducts.filter { it.isNew || it.hasDiscount }.take(6)

    fun getNewProducts(): List<Product> =
        sampleProducts.filter { it.isNew }.take(6)

    fun getPopularProducts(): List<Product> =
        sampleProducts.sortedByDescending { it.reviewCount }.take(8)

    fun searchProducts(query: String): List<Product> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return sampleProducts.filter {
            it.name.lowercase().contains(q) ||
                    it.category.lowercase().contains(q) ||
                    it.description.lowercase().contains(q)
        }
    }

    fun getCategories(): List<Category> = listOf(
        Category("all", "Todos", "🌿", sampleProducts.size),
        Category("home", "Hogar", "🏡", sampleProducts.count { it.category == "Hogar" }),
        Category("clothes", "Ropa", "👕", sampleProducts.count { it.category == "Ropa" }),
        Category("shoes", "Calzado", "👟", sampleProducts.count { it.category == "Calzado" }),
        Category("accessories", "Accesorios", "♻️", sampleProducts.count { it.category == "Accesorios" }),
        Category("personal", "Cuidado Personal", "🌱", sampleProducts.count { it.category == "Cuidado Personal" }),
        Category("energy", "Energía", "☀️", sampleProducts.count { it.category == "Energía" })
    )

    // ─── Cart ──────────────────────────────────────────────────────────────────

    fun getCartItems(): List<CartItem> = cartItems.toList()

    fun addToCart(product: Product, quantity: Int = 1, size: String = "", color: String = ""): Boolean {
        val index = cartItems.indexOfFirst { it.product.id == product.id && it.selectedSize == size && it.selectedColor == color }
        return if (index >= 0) {
            val existing = cartItems[index]
            cartItems[index] = existing.copy(quantity = existing.quantity + quantity)
            true
        } else {
            cartItems.add(CartItem(product, quantity, size, color))
            true
        }
    }

    fun removeFromCart(productId: String, size: String = "", color: String = ""): Boolean {
        val item = cartItems.find { it.product.id == productId && it.selectedSize == size && it.selectedColor == color }
            ?: cartItems.find { it.product.id == productId }
        return if (item != null) {
            cartItems.remove(item)
            true
        } else false
    }

    fun updateCartQuantity(productId: String, quantity: Int, size: String = "", color: String = ""): Boolean {
        val index = cartItems.indexOfFirst { it.product.id == productId && it.selectedSize == size && it.selectedColor == color }
            .takeIf { it >= 0 } ?: cartItems.indexOfFirst { it.product.id == productId }
        return if (index >= 0) {
            if (quantity <= 0) cartItems.removeAt(index)
            else cartItems[index] = cartItems[index].copy(quantity = quantity)
            true
        } else false
    }

    fun clearCart() = cartItems.clear()

    fun getCartTotal(): Double = cartItems.sumOf { it.totalPrice }

    fun getCartItemCount(): Int = cartItems.sumOf { it.quantity }

    // ─── Wishlist ──────────────────────────────────────────────────────────────

    fun toggleWishlist(productId: String): Boolean {
        return if (wishlistIds.contains(productId)) {
            wishlistIds.remove(productId)
            val idx = sampleProducts.indexOfFirst { it.id == productId }
            if (idx >= 0) sampleProducts[idx] = sampleProducts[idx].copy(isFavorite = false)
            false
        } else {
            wishlistIds.add(productId)
            val idx = sampleProducts.indexOfFirst { it.id == productId }
            if (idx >= 0) sampleProducts[idx] = sampleProducts[idx].copy(isFavorite = true)
            true
        }
    }

    fun isInWishlist(productId: String) = wishlistIds.contains(productId)

    fun getWishlistProducts(): List<Product> =
        sampleProducts.filter { wishlistIds.contains(it.id) }

    // ─── Orders ───────────────────────────────────────────────────────────────

    fun placeOrder(address: String = "Calle Principal 123, Bogotá"): Order? {
        if (cartItems.isEmpty()) return null
        val ecoPoints = cartItems.sumOf { it.product.ecoPoints * it.quantity }
        val order = Order(
            id = "ORD${System.currentTimeMillis()}",
            items = cartItems.toList(),
            totalAmount = getCartTotal() + 15900.0,
            status = OrderStatus.CONFIRMED,
            createdAt = Date(),
            address = address,
            ecoPointsEarned = ecoPoints
        )
        orders.add(0, order)
        clearCart()
        return order
    }

    fun getOrders(): List<Order> = orders.toList()

    // ─── Banners (promo) ───────────────────────────────────────────────────────

    fun getBanners(): List<BannerItem> = listOf(
        BannerItem("b1", "¡Por el planeta!", "Productos 100% ecológicos y sostenibles", "#10B981", "Todos"),
        BannerItem("b2", "Nueva colección verde", "Ropa y accesorios de materiales reciclados", "#2D6A4F", "Ropa"),
        BannerItem("b3", "Energía solar", "Paneles y productos de energía renovable", "#1A1A2E", "Energía")
    )
}

data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val color: String,
    val categoryTarget: String = "Todos"
)
