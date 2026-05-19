package com.shopapp.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.shopapp.AppSession
import com.shopapp.data.model.Address
import com.shopapp.data.model.PaymentMethod
import com.shopapp.data.repository.FirestoreAddressRepository
import com.shopapp.data.repository.FirestorePaymentRepository
import com.shopapp.databinding.FragmentCheckoutBinding
import com.shopapp.databinding.ItemCheckoutSelectBinding
import com.shopapp.ui.home.MainActivity
import com.shopapp.util.toCOP

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val uid get() = AppSession.userId

    private var selectedAddressId: String? = null
    private var selectedPaymentId: String? = null

    private var addresses: List<Address>       = emptyList()
    private var payments:  List<PaymentMethod> = emptyList()

    // Control para saber cuándo terminaron las dos cargas paralelas
    private var addressesLoaded = false
    private var paymentsLoaded  = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        binding.btnAddAddress.setOnClickListener { mainActivity.navigateToAddresses() }
        binding.btnAddPayment.setOnClickListener { mainActivity.navigateToPaymentMethods() }

        binding.btnConfirm.setOnClickListener {
            if (selectedAddressId == null || selectedPaymentId == null) {
                binding.tvRequirementHint.visibility = View.VISIBLE
                return@setOnClickListener
            }
            binding.tvRequirementHint.visibility = View.GONE
            viewModel.placeOrder()
        }

        viewModel.cartTotal.observe(viewLifecycleOwner) { total ->
            val shipping = 15900.0
            binding.tvSubtotal.text = total.toCOP()
            binding.tvShipping.text = shipping.toCOP()
            binding.tvTotal.text    = (total + shipping).toCOP()
        }

        viewModel.orderPlaced.observe(viewLifecycleOwner) { order ->
            order ?: return@observe
            val msg = "✅ ¡Pedido confirmado! +${order.ecoPointsEarned} pts eco 🌱"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            viewModel.clearOrderPlaced()
            mainActivity.navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar desde Firestore por si el usuario agregó una dirección/pago y volvió
        loadData()
    }

    // ─── Carga paralela ───────────────────────────────────────────────────────

    private fun loadData() {
        showLoading()
        addressesLoaded = false
        paymentsLoaded  = false

        FirestoreAddressRepository.getAll(uid) { result ->
            addresses = result
            // Pre-selección: mantener selección si sigue válida, si no: default → primero
            if (selectedAddressId != null && addresses.none { it.id == selectedAddressId })
                selectedAddressId = null
            if (selectedAddressId == null)
                selectedAddressId = addresses.firstOrNull { it.isDefault }?.id
                    ?: addresses.firstOrNull()?.id

            addressesLoaded = true
            activity?.runOnUiThread { checkBothLoaded() }
        }

        FirestorePaymentRepository.getAll(uid) { result ->
            payments = result
            if (selectedPaymentId != null && payments.none { it.id == selectedPaymentId })
                selectedPaymentId = null
            if (selectedPaymentId == null)
                selectedPaymentId = payments.firstOrNull { it.isDefault }?.id
                    ?: payments.firstOrNull()?.id

            paymentsLoaded = true
            activity?.runOnUiThread { checkBothLoaded() }
        }
    }

    private fun checkBothLoaded() {
        if (!addressesLoaded || !paymentsLoaded) return
        showContent()
        renderAddresses()
        renderPayments()
        updateConfirmButton()
    }

    // ─── Addresses ────────────────────────────────────────────────────────────

    private fun renderAddresses() {
        binding.layoutAddresses.removeAllViews()

        if (addresses.isEmpty()) {
            binding.layoutNoAddress.visibility = View.VISIBLE
        } else {
            binding.layoutNoAddress.visibility = View.GONE
            addresses.forEach { address ->
                val itemBinding = ItemCheckoutSelectBinding.inflate(
                    layoutInflater, binding.layoutAddresses, false
                )
                itemBinding.tvTitle.text    = "${address.label}  •  ${address.recipientName}"
                itemBinding.tvSubtitle.text = address.fullAddress

                val isSelected = address.id == selectedAddressId
                itemBinding.ivCheck.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
                setItemStyle(itemBinding, isSelected)

                itemBinding.root.setOnClickListener {
                    selectedAddressId = address.id
                    renderAddresses()
                    updateConfirmButton()
                }
                binding.layoutAddresses.addView(itemBinding.root)
            }
        }
    }

    // ─── Payments ─────────────────────────────────────────────────────────────

    private fun renderPayments() {
        binding.layoutPayments.removeAllViews()

        if (payments.isEmpty()) {
            binding.layoutNoPayment.visibility = View.VISIBLE
        } else {
            binding.layoutNoPayment.visibility = View.GONE
            payments.forEach { payment ->
                val itemBinding = ItemCheckoutSelectBinding.inflate(
                    layoutInflater, binding.layoutPayments, false
                )
                itemBinding.tvTitle.text    = payment.brandIcon
                itemBinding.tvSubtitle.text = if (payment.isCard)
                    "${payment.maskedNumber}  •  ${payment.cardHolder}"
                else
                    payment.maskedNumber

                val isSelected = payment.id == selectedPaymentId
                itemBinding.ivCheck.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
                setItemStyle(itemBinding, isSelected)

                itemBinding.root.setOnClickListener {
                    selectedPaymentId = payment.id
                    renderPayments()
                    updateConfirmButton()
                }
                binding.layoutPayments.addView(itemBinding.root)
            }
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun setItemStyle(itemBinding: ItemCheckoutSelectBinding, selected: Boolean) {
        val ctx = itemBinding.root.context
        val strokeColor = if (selected)
            ctx.getColor(com.shopapp.R.color.primary)
        else
            ctx.getColor(com.shopapp.R.color.divider)

        val bg = android.graphics.drawable.GradientDrawable().apply {
            shape         = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius  = 12f * ctx.resources.displayMetrics.density
            setColor(ctx.getColor(com.shopapp.R.color.surface))
            setStroke((if (selected) 2 else 1).dpToPx(ctx), strokeColor)
        }
        itemBinding.root.background = bg
    }

    private fun Int.dpToPx(ctx: android.content.Context): Int =
        (this * ctx.resources.displayMetrics.density).toInt()

    private fun updateConfirmButton() {
        val bothSelected = selectedAddressId != null && selectedPaymentId != null
        binding.btnConfirm.isEnabled = bothSelected
        if (bothSelected) binding.tvRequirementHint.visibility = View.GONE
    }

    private fun showLoading() {
        binding.layoutLoading.visibility = View.VISIBLE
        binding.scrollContent.visibility = View.GONE
    }

    private fun showContent() {
        binding.layoutLoading.visibility = View.GONE
        binding.scrollContent.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
