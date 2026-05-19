package com.shopapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.shopapp.AppSession
import com.shopapp.data.model.*
import com.shopapp.data.repository.BannerItem
import com.shopapp.data.repository.EcoRepository
import com.shopapp.data.repository.FirestoreOrderRepository
import com.shopapp.data.repository.FirestoreProductRepository
import com.shopapp.data.repository.FirestoreUserRepository
import com.shopapp.data.repository.NotificationRepository
import com.shopapp.data.repository.ShopRepository
import com.shopapp.data.repository.UserRepository

class ShopViewModel : ViewModel() {

    // ─── Products ──────────────────────────────────────────────────────────────

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _featuredProducts = MutableLiveData<List<Product>>()
    val featuredProducts: LiveData<List<Product>> = _featuredProducts

    private val _newProducts = MutableLiveData<List<Product>>()
    val newProducts: LiveData<List<Product>> = _newProducts

    private val _popularProducts = MutableLiveData<List<Product>>()
    val popularProducts: LiveData<List<Product>> = _popularProducts

    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> = _selectedProduct

    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults

    private val _categoryProducts = MutableLiveData<List<Product>>()
    val categoryProducts: LiveData<List<Product>> = _categoryProducts

    // ─── Categories ────────────────────────────────────────────────────────────

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    // ─── Cart ──────────────────────────────────────────────────────────────────

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _cartCount = MutableLiveData<Int>(0)
    val cartCount: LiveData<Int> = _cartCount

    private val _cartTotal = MutableLiveData<Double>(0.0)
    val cartTotal: LiveData<Double> = _cartTotal

    // ─── Wishlist ──────────────────────────────────────────────────────────────

    private val _wishlistProducts = MutableLiveData<List<Product>>()
    val wishlistProducts: LiveData<List<Product>> = _wishlistProducts

    // ─── Banners ───────────────────────────────────────────────────────────────

    private val _banners = MutableLiveData<List<BannerItem>>()
    val banners: LiveData<List<BannerItem>> = _banners

    // ─── User ──────────────────────────────────────────────────────────────────

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // ─── Messages ──────────────────────────────────────────────────────────────

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _orderPlaced = MutableLiveData<Order?>()
    val orderPlaced: LiveData<Order?> = _orderPlaced

    // ─── Eco ───────────────────────────────────────────────────────────────────

    private val _ecoPoints = MutableLiveData<Int>(0)
    val ecoPoints: LiveData<Int> = _ecoPoints

    private val _ecoChallenges = MutableLiveData<List<EcoChallenge>>()
    val ecoChallenges: LiveData<List<EcoChallenge>> = _ecoChallenges

    // ─── Init ──────────────────────────────────────────────────────────────────

    init {
        loadAll()
    }

    private fun loadAll() {
        // 1. Mostrar datos locales inmediatamente (respuesta rápida)
        refreshProducts()
        refreshCart()
        _categories.value = ShopRepository.getCategories()
        _banners.value = ShopRepository.getBanners()
        _wishlistProducts.value = ShopRepository.getWishlistProducts()
        _currentUser.value = UserRepository.getCurrentUser()
        refreshEco()

        // 2. Sincronizar con Firestore en segundo plano
        syncProductsFromFirestore()
    }

    /**
     * Intenta cargar productos desde Firestore.
     * Si Firestore está vacío, sube los productos locales (primera vez).
     * Si Firestore tiene datos, reemplaza los datos locales.
     */
    private fun syncProductsFromFirestore() {
        FirestoreProductRepository.getAllProducts { firestoreProducts ->
            if (firestoreProducts.isNotEmpty()) {
                // Sincronizar caché local con Firestore
                ShopRepository.syncFromFirestore(firestoreProducts)
                refreshProducts()
                _categories.postValue(ShopRepository.getCategories())
            } else {
                // Primera vez: subir productos locales a Firestore
                val localProducts = ShopRepository.getAllProducts()
                FirestoreProductRepository.seedProducts(localProducts) { }
            }
        }
    }

    private fun refreshProducts() {
        _products.value = ShopRepository.getAllProducts()
        _featuredProducts.value = ShopRepository.getFeaturedProducts()
        _newProducts.value = ShopRepository.getNewProducts()
        _popularProducts.value = ShopRepository.getPopularProducts()
    }

    // ─── Product Actions ───────────────────────────────────────────────────────

    fun selectProduct(productId: String) {
        _selectedProduct.value = ShopRepository.getProductById(productId)
    }

    fun searchProducts(query: String) {
        _searchResults.value = ShopRepository.searchProducts(query)
    }

    fun loadCategoryProducts(category: String) {
        _categoryProducts.value = if (category == "Todos") {
            ShopRepository.getAllProducts()
        } else {
            ShopRepository.getProductsByCategory(category)
        }
    }

    /** Recarga productos desde Firestore (llamado por admin después de cambios) */
    fun reloadProductsFromFirestore() {
        FirestoreProductRepository.getAllProducts { products ->
            if (products.isNotEmpty()) {
                ShopRepository.syncFromFirestore(products)
                refreshProducts()
                _categories.postValue(ShopRepository.getCategories())
            }
        }
    }

    // ─── Cart Actions ──────────────────────────────────────────────────────────

    fun addToCart(product: Product, quantity: Int = 1, size: String = "", color: String = "") {
        ShopRepository.addToCart(product, quantity, size, color)
        refreshCart()
        _message.value = "¡Agregado al carrito!"
    }

    fun removeFromCart(productId: String) {
        ShopRepository.removeFromCart(productId)
        refreshCart()
    }

    fun updateCartQuantity(productId: String, quantity: Int) {
        ShopRepository.updateCartQuantity(productId, quantity)
        refreshCart()
    }

    private fun refreshCart() {
        _cartItems.value = ShopRepository.getCartItems()
        _cartCount.value = ShopRepository.getCartItemCount()
        _cartTotal.value = ShopRepository.getCartTotal()
    }

    // ─── Wishlist Actions ──────────────────────────────────────────────────────

    fun toggleWishlist(productId: String): Boolean {
        val added = ShopRepository.toggleWishlist(productId)
        _wishlistProducts.value = ShopRepository.getWishlistProducts()
        _message.value = if (added) "Agregado a favoritos" else "Eliminado de favoritos"
        // Refresh all product lists so the heart icon updates immediately
        _products.value = ShopRepository.getAllProducts()
        _featuredProducts.value = ShopRepository.getFeaturedProducts()
        _popularProducts.value = ShopRepository.getPopularProducts()
        _selectedProduct.value?.let {
            if (it.id == productId) _selectedProduct.value = ShopRepository.getProductById(productId)
        }
        return added
    }

    fun isInWishlist(productId: String) = ShopRepository.isInWishlist(productId)

    // ─── Order Actions ────────────────────────────────────────────────────────

    fun placeOrder() {
        val user = _currentUser.value
        val order = ShopRepository.placeOrder(user?.address ?: "Dirección de envío")
        if (order != null) {
            // Actualizar datos locales
            UserRepository.addEcoPoints(order.ecoPointsEarned)
            UserRepository.incrementPurchaseCount()
            order.items.forEach { UserRepository.addCategoryBought(it.product.category) }
            NotificationRepository.addOrderNotification(order.id, order.ecoPointsEarned)
            _currentUser.value = UserRepository.getCurrentUser()
            refreshEco()

            // Guardar en Firestore (si está autenticado)
            val uid = AppSession.userId
            val email = AppSession.userEmail
            if (uid.isNotBlank()) {
                FirestoreOrderRepository.saveOrder(order, uid, email)
                FirestoreUserRepository.addEcoPoints(uid, order.ecoPointsEarned)
                FirestoreUserRepository.incrementPurchaseCount(uid)
            }
        }
        _orderPlaced.value = order
        refreshCart()
    }

    fun refreshEco() {
        _ecoPoints.value = UserRepository.getTotalEcoPoints()
        _ecoChallenges.value = EcoRepository.getChallenges(
            totalEcoPoints = UserRepository.getTotalEcoPoints(),
            purchaseCount = UserRepository.getPurchaseCount(),
            categoriesBought = UserRepository.getCategoriesBought()
        )
    }

    // ─── User Actions ─────────────────────────────────────────────────────────

    fun refreshUser() {
        _currentUser.value = UserRepository.getCurrentUser()
    }

    fun clearMessage() { _message.value = null }

    fun clearOrderPlaced() { _orderPlaced.value = null }
}
