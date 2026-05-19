package com.shopapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shopapp.AppSession
import com.shopapp.R
import com.shopapp.databinding.FragmentProfileBinding
import com.shopapp.data.repository.FirebaseAuthRepository
import com.shopapp.data.repository.FirestoreOrderRepository
import com.shopapp.data.repository.UserRepository
import com.shopapp.ui.auth.AuthActivity
import com.shopapp.ui.home.MainActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.tvUserName.text = user.fullName
            binding.tvUserEmail.text = user.email
            binding.tvAvatar.text = user.fullName.firstOrNull()?.uppercase() ?: "U"
        }

        viewModel.cartCount.observe(viewLifecycleOwner) { count ->
            binding.tvCartCount.text = count.toString()
        }

        viewModel.wishlistProducts.observe(viewLifecycleOwner) { list ->
            binding.tvWishlistCount.text = list.size.toString()
        }

        // Cargar conteo de pedidos desde Firestore
        loadOrdersCount()

        // Botón Admin (solo visible para administradores)
        if (AppSession.isAdmin) {
            binding.dividerAdmin.visibility = View.VISIBLE
            binding.menuAdmin.root.visibility = View.VISIBLE
            binding.menuAdmin.ivIcon.setImageResource(R.drawable.ic_eco)
            binding.menuAdmin.tvTitle.text = "Panel de Administrador"
            binding.menuAdmin.root.setOnClickListener {
                mainActivity.navigateToAdminDashboard()
            }
        } else {
            binding.menuAdmin.root.visibility = View.GONE
        }

        setupMenuItems()

        binding.btnEditProfile.setOnClickListener { mainActivity.navigateToEditProfile() }

        binding.btnLogout.setOnClickListener {
            // Cerrar sesión en Firebase y limpiar datos de sesión local
            FirebaseAuthRepository.logout()
            AppSession.clear()
            UserRepository.logout()

            startActivity(Intent(requireContext(), AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }
    }

    private fun setupMenuItems() {
        val mainActivity = requireActivity() as MainActivity

        binding.menuOrders.ivIcon.setImageResource(R.drawable.ic_orders)
        binding.menuOrders.tvTitle.text = "Mis Pedidos"
        binding.menuOrders.root.setOnClickListener { mainActivity.navigateToOrders() }

        binding.menuWishlist.ivIcon.setImageResource(R.drawable.ic_wishlist)
        binding.menuWishlist.tvTitle.text = "Lista de deseos"
        binding.menuWishlist.root.setOnClickListener { mainActivity.navigateToWishlist() }

        binding.menuAddresses.ivIcon.setImageResource(R.drawable.ic_address)
        binding.menuAddresses.tvTitle.text = "Direcciones"
        binding.menuAddresses.root.setOnClickListener { mainActivity.navigateToAddresses() }

        binding.menuPayment.ivIcon.setImageResource(R.drawable.ic_payment)
        binding.menuPayment.tvTitle.text = "Métodos de pago"
        binding.menuPayment.root.setOnClickListener { mainActivity.navigateToPaymentMethods() }

        binding.menuNotifications.ivIcon.setImageResource(R.drawable.ic_notification)
        binding.menuNotifications.tvTitle.text = "Notificaciones"
        binding.menuNotifications.root.setOnClickListener { mainActivity.navigateToNotifications() }

        binding.menuHelp.ivIcon.setImageResource(R.drawable.ic_help)
        binding.menuHelp.tvTitle.text = "Ayuda"
        binding.menuHelp.root.setOnClickListener { mainActivity.navigateToHelp() }
    }

    private fun loadOrdersCount() {
        val uid = AppSession.userId
        if (uid.isBlank()) {
            binding.tvOrdersCount.text = "0"
            return
        }
        FirestoreOrderRepository.getUserOrders(uid) { orders ->
            activity?.runOnUiThread {
                binding.tvOrdersCount.text = orders.size.toString()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadOrdersCount()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
