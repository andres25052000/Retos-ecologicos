package com.shopapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopapp.data.model.Product
import com.shopapp.data.repository.FirestoreOrderRepository
import com.shopapp.data.repository.FirestoreProductRepository
import com.shopapp.data.repository.FirestoreUserRepository
import com.shopapp.data.repository.NotificationRepository
import com.shopapp.data.repository.ShopRepository
import com.shopapp.util.toCOP

class AdminViewModel : ViewModel() {

    // ─── Products ──────────────────────────────────────────────────────────────

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    // ─── Orders (mapa de datos crudos de Firestore) ────────────────────────────

    private val _orders = MutableLiveData<List<Map<String, Any>>>()
    val orders: LiveData<List<Map<String, Any>>> = _orders

    // ─── Users ────────────────────────────────────────────────────────────────

    private val _users = MutableLiveData<List<Map<String, Any>>>()
    val users: LiveData<List<Map<String, Any>>> = _users

    // ─── Estado ───────────────────────────────────────────────────────────────

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _productSaved = MutableLiveData<Boolean?>(null)
    val productSaved: LiveData<Boolean?> = _productSaved

    // ─── Acciones de productos ─────────────────────────────────────────────────

    fun loadProducts() {
        _isLoading.value = true
        FirestoreProductRepository.getAllProducts { firestoreProducts ->
            if (firestoreProducts.isNotEmpty()) {
                ShopRepository.syncFromFirestore(firestoreProducts)
            }
            _products.postValue(firestoreProducts.ifEmpty { ShopRepository.getAllProducts() })
            _isLoading.postValue(false)
        }
    }

    fun addProduct(product: Product) {
        _isLoading.value = true
        FirestoreProductRepository.addProduct(product) { success, _ ->
            if (success) {
                // Notificación para clientes
                NotificationRepository.addProductAddedNotification(product.id, product.name)
                _message.postValue("✅ Producto agregado exitosamente")
                _productSaved.postValue(true)
                loadProducts()
            } else {
                _message.postValue("❌ Error al guardar el producto")
                _productSaved.postValue(false)
                _isLoading.postValue(false)
            }
        }
    }

    fun updateProduct(product: Product) {
        _isLoading.value = true
        // Detectar qué cambió para el mensaje de la notificación
        val old = ShopRepository.getProductById(product.id)
        FirestoreProductRepository.updateProduct(product) { success ->
            if (success) {
                val detail = buildChangeDetail(old, product)
                NotificationRepository.addProductUpdatedNotification(product.id, product.name, detail)
                _message.postValue("✅ Producto actualizado")
                _productSaved.postValue(true)
                loadProducts()
            } else {
                _message.postValue("❌ Error al actualizar el producto")
                _productSaved.postValue(false)
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteProduct(productId: String) {
        val productName = ShopRepository.getProductById(productId)?.name ?: "Producto"
        FirestoreProductRepository.deleteProduct(productId) { success ->
            if (success) {
                NotificationRepository.addProductRemovedNotification(productName)
                _message.postValue("🗑️ Producto eliminado")
                loadProducts()
            } else {
                _message.postValue("❌ Error al eliminar el producto")
            }
        }
    }

    /** Construye una descripción legible de los cambios entre dos versiones del producto. */
    private fun buildChangeDetail(old: Product?, new: Product): String {
        if (old == null) return "información actualizada"
        val changes = mutableListOf<String>()
        if (old.price != new.price)
            changes.add("precio: ${new.price.toCOP()}")
        if (old.colors != new.colors && new.colors.isNotEmpty())
            changes.add("colores disponibles actualizados")
        if (old.inStock != new.inStock)
            changes.add(if (new.inStock) "volvió a estar en stock" else "temporalmente agotado")
        if (old.discount != new.discount && new.discount > 0)
            changes.add("descuento del ${new.discount}%")
        return if (changes.isEmpty()) "información actualizada" else changes.joinToString(", ")
    }

    // ─── Acciones de pedidos ───────────────────────────────────────────────────

    fun loadAllOrders() {
        _isLoading.value = true
        FirestoreOrderRepository.getAllOrders { orders ->
            _orders.postValue(orders)
            _isLoading.postValue(false)
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        FirestoreOrderRepository.updateOrderStatus(orderId, status) { success ->
            if (success) {
                _message.postValue("Estado del pedido actualizado")
                loadAllOrders()
            }
        }
    }

    // ─── Acciones de usuarios ──────────────────────────────────────────────────

    fun loadAllUsers() {
        FirestoreUserRepository.getAllUsers { users ->
            _users.postValue(users)
        }
    }

    // ─── Estadísticas ──────────────────────────────────────────────────────────

    val productsCount: Int get() = _products.value?.size ?: 0
    val ordersCount: Int get() = _orders.value?.size ?: 0
    val usersCount: Int get() = _users.value?.size ?: 0

    // ─── Utils ─────────────────────────────────────────────────────────────────

    fun clearMessage() { _message.value = null }
    fun clearProductSaved() { _productSaved.value = null }
}
