package com.shopapp.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.shopapp.R
import com.shopapp.databinding.ActivityMainBinding
import com.shopapp.viewmodel.AdminViewModel
import com.shopapp.viewmodel.ShopViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var shopViewModel: ShopViewModel
    lateinit var adminViewModel: AdminViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        shopViewModel = ViewModelProvider(this)[ShopViewModel::class.java]
        adminViewModel = ViewModelProvider(this)[AdminViewModel::class.java]

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showBottomNav = destination.id in listOf(
                R.id.homeFragment, R.id.searchFragment,
                R.id.cartFragment, R.id.ecoChallengesFragment, R.id.profileFragment
            )
            binding.bottomNavigation.visibility = if (showBottomNav) View.VISIBLE else View.GONE
        }

        shopViewModel.cartCount.observe(this) { count ->
            binding.cartBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
            binding.cartBadge.text = if (count > 9) "9+" else count.toString()
        }
    }

    // ─── Navegación de tienda ──────────────────────────────────────────────────

    fun navigateToProductDetail(productId: String) {
        val bundle = Bundle().apply { putString("productId", productId) }
        navController.navigate(R.id.productDetailFragment, bundle)
    }

    fun navigateToCategoryProducts(categoryName: String) {
        val bundle = Bundle().apply { putString("categoryName", categoryName) }
        navController.navigate(R.id.categoryProductsFragment, bundle)
    }

    fun navigateToEditProfile()     { navController.navigate(R.id.editProfileFragment) }
    fun navigateToOrders()          { navController.navigate(R.id.ordersFragment) }
    fun navigateToWishlist()        { navController.navigate(R.id.wishlistFragment) }
    fun navigateToAddresses()       { navController.navigate(R.id.addressesFragment) }
    fun navigateToPaymentMethods()  { navController.navigate(R.id.paymentMethodsFragment) }
    fun navigateToNotifications()   { navController.navigate(R.id.notificationsFragment) }
    fun navigateToHelp()            { navController.navigate(R.id.helpFragment) }
    fun navigateToCheckout()        { navController.navigate(R.id.checkoutFragment) }
    fun navigateToEcoTab()          { navController.navigate(R.id.ecoChallengesFragment) }

    // ─── Navegación admin ─────────────────────────────────────────────────────

    fun navigateToAdminDashboard()  { navController.navigate(R.id.adminDashboardFragment) }
    fun navigateToAdminProducts()   { navController.navigate(R.id.adminProductsFragment) }
    fun navigateToAdminOrders()     { navController.navigate(R.id.adminOrdersFragment) }

    fun navigateToAdminAddEditProduct(productId: String) {
        val bundle = Bundle().apply { putString("productId", productId) }
        navController.navigate(R.id.adminAddEditProductFragment, bundle)
    }

    fun navigateUp() { navController.navigateUp() }
}
