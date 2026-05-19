package com.shopapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopapp.AppSession
import com.shopapp.data.model.CardBrand
import com.shopapp.data.model.PaymentMethod
import com.shopapp.data.model.PaymentType
import com.shopapp.data.repository.FirestorePaymentRepository
import com.shopapp.databinding.DialogAddPaymentBinding
import com.shopapp.databinding.DialogAddPseBinding
import com.shopapp.databinding.FragmentPaymentMethodsBinding
import com.shopapp.databinding.ItemPaymentMethodBinding
import com.shopapp.ui.home.MainActivity
import java.util.UUID

class PaymentMethodsFragment : Fragment() {

    private var _binding: FragmentPaymentMethodsBinding? = null
    private val binding get() = _binding!!

    private val uid get() = AppSession.userId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentMethodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity
        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }
        binding.btnAddPayment.setOnClickListener { showMethodTypeChooser() }
        loadPayments()
    }

    // ─── Carga ────────────────────────────────────────────────────────────────

    private fun loadPayments() {
        showLoading()
        FirestorePaymentRepository.getAll(uid) { methods ->
            activity?.runOnUiThread { renderPayments(methods) }
        }
    }

    // ─── Selector de tipo de pago ──────────────────────────────────────────────

    private fun showMethodTypeChooser() {
        val options = arrayOf(
            "💳  Tarjeta crédito / débito",
            "🏦  PSE — Débito bancario",
            "💵  Efectivo con contraentrega"
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Selecciona el método de pago")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddCardDialog()
                    1 -> showAddPseDialog()
                    2 -> addCashOnDelivery()
                }
            }
            .show()
    }

    // ─── Agregar tarjeta ───────────────────────────────────────────────────────

    private fun showAddCardDialog() {
        val dialogBinding = DialogAddPaymentBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val cardNumber = dialogBinding.etCardNumber.text?.toString()?.trim() ?: ""
            val holder     = dialogBinding.etCardHolder.text?.toString()?.trim() ?: ""
            val expiry     = dialogBinding.etExpiry.text?.toString()?.trim() ?: ""
            val cvv        = dialogBinding.etCvv.text?.toString()?.trim() ?: ""

            when {
                cardNumber.length < 13 -> { dialogBinding.tilCardNumber.error = "Número inválido"; return@setOnClickListener }
                holder.isBlank()       -> { dialogBinding.tilCardHolder.error = "Ingresa el titular"; return@setOnClickListener }
                expiry.length < 4      -> { dialogBinding.tilExpiry.error = "Fecha inválida (MMAA)"; return@setOnClickListener }
                cvv.length < 3         -> { dialogBinding.tilCvv.error = "CVV inválido"; return@setOnClickListener }
            }

            val brand = when {
                cardNumber.startsWith("4") -> CardBrand.VISA
                cardNumber.startsWith("5") -> CardBrand.MASTERCARD
                cardNumber.startsWith("3") -> CardBrand.AMEX
                else                       -> CardBrand.OTHER
            }

            val method = PaymentMethod(
                id          = UUID.randomUUID().toString(),
                type        = PaymentType.CREDIT,
                cardHolder  = holder,
                lastFour    = cardNumber.takeLast(4),
                brand       = brand,
                expiryMonth = expiry.take(2),
                expiryYear  = expiry.takeLast(2),
                isDefault   = dialogBinding.switchDefault.isChecked
            )
            dialog.dismiss()
            showLoading()
            FirestorePaymentRepository.add(uid, method) { updated ->
                activity?.runOnUiThread {
                    renderPayments(updated)
                    Toast.makeText(requireContext(), "Tarjeta guardada ✓", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    // ─── Agregar PSE ───────────────────────────────────────────────────────────

    private fun showAddPseDialog() {
        val dialogBinding = DialogAddPseBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        val banks = listOf(
            "Bancolombia", "Banco de Bogotá", "Davivienda", "BBVA Colombia",
            "Banco Popular", "Banco Falabella", "Nequi", "Daviplata",
            "Banco de Occidente", "Colpatria", "Banco Agrario", "Otro"
        )
        dialogBinding.etBank.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, banks)
        )
        val accountTypes = listOf("Cuenta de ahorros", "Cuenta corriente")
        dialogBinding.etAccountType.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, accountTypes)
        )

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val bank        = dialogBinding.etBank.text?.toString()?.trim() ?: ""
            val accountType = dialogBinding.etAccountType.text?.toString()?.trim() ?: ""
            val holder      = dialogBinding.etHolder.text?.toString()?.trim() ?: ""

            when {
                bank.isBlank()   -> { dialogBinding.tilBank.error   = "Selecciona un banco"; return@setOnClickListener }
                holder.isBlank() -> { dialogBinding.tilHolder.error  = "Ingresa el titular";  return@setOnClickListener }
            }

            val method = PaymentMethod(
                id         = UUID.randomUUID().toString(),
                type       = PaymentType.PSE,
                cardHolder = holder,
                bankName   = if (accountType.isNotBlank()) "$bank — $accountType" else bank
            )
            dialog.dismiss()
            showLoading()
            FirestorePaymentRepository.add(uid, method) { updated ->
                activity?.runOnUiThread {
                    renderPayments(updated)
                    Toast.makeText(requireContext(), "PSE guardado ✓", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    // ─── Agregar Efectivo contraentrega ────────────────────────────────────────

    private fun addCashOnDelivery() {
        showLoading()
        FirestorePaymentRepository.getAll(uid) { existing ->
            if (existing.any { it.type == PaymentType.CASH_ON_DELIVERY }) {
                activity?.runOnUiThread {
                    renderPayments(existing)
                    Toast.makeText(requireContext(), "Ya tienes el pago en efectivo activado", Toast.LENGTH_SHORT).show()
                }
                return@getAll
            }
            val method = PaymentMethod(
                id   = UUID.randomUUID().toString(),
                type = PaymentType.CASH_ON_DELIVERY
            )
            FirestorePaymentRepository.add(uid, method) { updated ->
                activity?.runOnUiThread {
                    renderPayments(updated)
                    Toast.makeText(requireContext(), "Pago en efectivo activado ✓", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ─── Renderizar lista ──────────────────────────────────────────────────────

    private fun renderPayments(methods: List<PaymentMethod>) {
        binding.layoutLoading.visibility = View.GONE
        binding.layoutPayments.removeAllViews()

        if (methods.isEmpty()) {
            binding.layoutEmpty.visibility   = View.VISIBLE
            binding.scrollContent.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility   = View.GONE
            binding.scrollContent.visibility = View.VISIBLE

            methods.forEach { method ->
                val item = ItemPaymentMethodBinding.inflate(layoutInflater, binding.layoutPayments, false)
                item.tvBrand.text        = method.brandIcon
                item.tvMaskedNumber.text = method.maskedNumber
                item.tvCardHolder.text   = method.cardHolder
                item.tvExpiry.text       = if (method.isCard && method.expiryMonth.isNotBlank())
                    "Vence: ${method.expiryMonth}/${method.expiryYear}" else ""

                item.tvDefaultBadge.visibility = if (method.isDefault) View.VISIBLE else View.GONE
                item.btnSetDefault.visibility  = if (method.isDefault) View.GONE   else View.VISIBLE

                item.btnSetDefault.setOnClickListener {
                    showLoading()
                    FirestorePaymentRepository.setDefault(uid, method.id) { updated ->
                        activity?.runOnUiThread {
                            renderPayments(updated)
                            Toast.makeText(requireContext(), "Método principal actualizado", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                item.ivDelete.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Eliminar método de pago")
                        .setMessage("¿Deseas eliminar este método de pago?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            showLoading()
                            FirestorePaymentRepository.delete(uid, method.id) { updated ->
                                activity?.runOnUiThread { renderPayments(updated) }
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                binding.layoutPayments.addView(item.root)
            }
        }
    }

    // ─── Estado de carga ──────────────────────────────────────────────────────

    private fun showLoading() {
        binding.layoutLoading.visibility  = View.VISIBLE
        binding.layoutEmpty.visibility    = View.GONE
        binding.scrollContent.visibility  = View.GONE
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
