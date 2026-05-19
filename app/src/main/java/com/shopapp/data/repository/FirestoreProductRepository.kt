package com.shopapp.data.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shopapp.data.model.Product

object FirestoreProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION = "products"

    /** Obtiene todos los productos de Firestore ordenados por nombre */
    fun getAllProducts(callback: (List<Product>) -> Unit) {
        db.collection(COLLECTION)
            .orderBy("name")
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { it.toProduct() }
                callback(products)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    /**
     * Agrega un nuevo producto. Si el ID está vacío, Firestore genera uno automáticamente.
     */
    fun addProduct(product: Product, callback: (Boolean, String) -> Unit) {
        val docRef = if (product.id.isBlank()) {
            db.collection(COLLECTION).document()
        } else {
            db.collection(COLLECTION).document(product.id)
        }
        val finalProduct = if (product.id.isBlank()) product.copy(id = docRef.id) else product
        docRef.set(finalProduct.toMap())
            .addOnSuccessListener { callback(true, finalProduct.id) }
            .addOnFailureListener { callback(false, "") }
    }

    /** Actualiza un producto existente */
    fun updateProduct(product: Product, callback: (Boolean) -> Unit) {
        db.collection(COLLECTION).document(product.id)
            .set(product.toMap())
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    /** Elimina un producto por su ID */
    fun deleteProduct(productId: String, callback: (Boolean) -> Unit) {
        db.collection(COLLECTION).document(productId).delete()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    /**
     * Sube los productos locales a Firestore (solo la primera vez).
     * Se llama automáticamente si Firestore está vacío al iniciar la app.
     */
    fun seedProducts(products: List<Product>, callback: (Boolean) -> Unit) {
        val batch = db.batch()
        products.forEach { product ->
            val ref = db.collection(COLLECTION).document(product.id)
            batch.set(ref, product.toMap())
        }
        batch.commit()
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    // ─── Mapeo Firestore ↔ Product ──────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toProduct(): Product? {
        return try {
            val d = data ?: return null
            Product(
                id = id,
                name = d["name"] as? String ?: return null,
                description = d["description"] as? String ?: "",
                price = (d["price"] as? Number)?.toDouble() ?: return null,
                originalPrice = (d["originalPrice"] as? Number)?.toDouble() ?: 0.0,
                imageUrl = d["imageUrl"] as? String ?: "",
                category = d["category"] as? String ?: "",
                rating = (d["rating"] as? Number)?.toFloat() ?: 0f,
                reviewCount = (d["reviewCount"] as? Number)?.toInt() ?: 0,
                inStock = d["inStock"] as? Boolean ?: true,
                isNew = d["isNew"] as? Boolean ?: false,
                discount = (d["discount"] as? Number)?.toInt() ?: 0,
                sizes = (d["sizes"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                colors = (d["colors"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                ecoPoints = (d["ecoPoints"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) { null }
    }

    private fun Product.toMap(): Map<String, Any> = hashMapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "price" to price,
        "originalPrice" to originalPrice,
        "imageUrl" to imageUrl,
        "category" to category,
        "rating" to rating.toDouble(),
        "reviewCount" to reviewCount,
        "inStock" to inStock,
        "isNew" to isNew,
        "discount" to discount,
        "sizes" to sizes,
        "colors" to colors,
        "ecoPoints" to ecoPoints
    )
}
