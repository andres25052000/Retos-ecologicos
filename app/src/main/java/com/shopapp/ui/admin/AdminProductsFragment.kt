package com.shopapp.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.shopapp.databinding.FragmentAdminProductsBinding
import com.shopapp.ui.home.MainActivity

class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val adminViewModel = mainActivity.adminViewModel

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        val productAdapter = AdminProductAdapter(
            onEdit = { product ->
                mainActivity.navigateToAdminAddEditProduct(product.id)
            },
            onDelete = { product ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar producto")
                    .setMessage("¿Estás seguro de eliminar \"${product.name}\"?\n\nEsta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar") { _, _ ->
                        adminViewModel.deleteProduct(product.id)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        )

        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productAdapter
        }

        adminViewModel.products.observe(viewLifecycleOwner) { products ->
            productAdapter.submitList(products)
            binding.tvProductCount.text = "${products.size}"
        }

        adminViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        adminViewModel.message.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            adminViewModel.clearMessage()
        }

        binding.fabAddProduct.setOnClickListener {
            mainActivity.navigateToAdminAddEditProduct("")
        }

        // Recargar productos
        adminViewModel.loadProducts()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
