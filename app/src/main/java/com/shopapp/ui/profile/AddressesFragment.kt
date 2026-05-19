package com.shopapp.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shopapp.AppSession
import com.shopapp.data.model.Address
import com.shopapp.data.repository.FirestoreAddressRepository
import com.shopapp.databinding.DialogAddAddressBinding
import com.shopapp.databinding.FragmentAddressesBinding
import com.shopapp.databinding.ItemAddressBinding
import com.shopapp.ui.home.MainActivity
import java.util.UUID

class AddressesFragment : Fragment() {

    private var _binding: FragmentAddressesBinding? = null
    private val binding get() = _binding!!

    private val uid get() = AppSession.userId

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddressesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity
        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }
        binding.btnAddAddress.setOnClickListener { showAddDialog() }
        loadAddresses()
    }

    // ─── Carga ────────────────────────────────────────────────────────────────

    private fun loadAddresses() {
        showLoading()
        FirestoreAddressRepository.getAll(uid) { addresses ->
            activity?.runOnUiThread { renderAddresses(addresses) }
        }
    }

    // ─── Render ───────────────────────────────────────────────────────────────

    private fun renderAddresses(addresses: List<Address>) {
        binding.layoutLoading.visibility  = View.GONE
        binding.layoutAddresses.removeAllViews()

        if (addresses.isEmpty()) {
            binding.layoutEmpty.visibility   = View.VISIBLE
            binding.scrollContent.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility   = View.GONE
            binding.scrollContent.visibility = View.VISIBLE

            addresses.forEach { address ->
                val item = ItemAddressBinding.inflate(layoutInflater, binding.layoutAddresses, false)
                item.tvAddressLabel.text = address.label
                item.tvRecipient.text    = address.recipientName
                item.tvAddressFull.text  = address.fullAddress
                item.tvPhone.text        = "Tel: ${address.phone}"
                item.tvDefaultBadge.visibility = if (address.isDefault) View.VISIBLE else View.GONE
                item.btnSetDefault.visibility  = if (address.isDefault) View.GONE   else View.VISIBLE

                item.btnSetDefault.setOnClickListener {
                    showLoading()
                    FirestoreAddressRepository.setDefault(uid, address.id) { updated ->
                        activity?.runOnUiThread {
                            renderAddresses(updated)
                            Toast.makeText(requireContext(), "Dirección principal actualizada", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                item.ivDelete.setOnClickListener {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Eliminar dirección")
                        .setMessage("¿Deseas eliminar esta dirección?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            showLoading()
                            FirestoreAddressRepository.delete(uid, address.id) { updated ->
                                activity?.runOnUiThread { renderAddresses(updated) }
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
                binding.layoutAddresses.addView(item.root)
            }
        }
    }

    // ─── Agregar ──────────────────────────────────────────────────────────────

    private fun showAddDialog() {
        val dialogBinding = DialogAddAddressBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnSave.setOnClickListener {
            val label  = dialogBinding.etLabel.text?.toString()?.trim() ?: ""
            val name   = dialogBinding.etName.text?.toString()?.trim() ?: ""
            val street = dialogBinding.etStreet.text?.toString()?.trim() ?: ""
            val city   = dialogBinding.etCity.text?.toString()?.trim() ?: ""
            val state  = dialogBinding.etState.text?.toString()?.trim() ?: ""
            val zip    = dialogBinding.etZip.text?.toString()?.trim() ?: ""
            val phone  = dialogBinding.etPhone.text?.toString()?.trim() ?: ""

            if (name.isBlank() || street.isBlank() || city.isBlank()) {
                Toast.makeText(requireContext(), "Completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val address = Address(
                id            = UUID.randomUUID().toString(),
                label         = label.ifBlank { "Otro" },
                recipientName = name,
                street        = street,
                city          = city,
                state         = state,
                zipCode       = zip,
                phone         = phone,
                isDefault     = dialogBinding.switchDefault.isChecked
            )

            dialog.dismiss()
            showLoading()
            FirestoreAddressRepository.add(uid, address) { updated ->
                activity?.runOnUiThread {
                    renderAddresses(updated)
                    Toast.makeText(requireContext(), "Dirección guardada ✓", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    // ─── Estado de carga ──────────────────────────────────────────────────────

    private fun showLoading() {
        binding.layoutLoading.visibility  = View.VISIBLE
        binding.layoutEmpty.visibility    = View.GONE
        binding.scrollContent.visibility  = View.GONE
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
