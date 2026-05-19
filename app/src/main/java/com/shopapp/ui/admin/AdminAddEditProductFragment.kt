package com.shopapp.ui.admin

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.shopapp.data.model.Product
import com.shopapp.data.repository.ShopRepository
import com.shopapp.databinding.FragmentAdminAddEditProductBinding
import com.shopapp.ui.home.MainActivity
import com.shopapp.util.ColorConstants
import java.util.UUID

class AdminAddEditProductFragment : Fragment() {

    private var _binding: FragmentAdminAddEditProductBinding? = null
    private val binding get() = _binding!!

    private val categories = listOf(
        "Hogar", "Ropa", "Calzado", "Accesorios", "Cuidado Personal", "Energía", "Otro"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminAddEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val adminViewModel = mainActivity.adminViewModel
        val productId = arguments?.getString("productId") ?: ""
        val isEditing = productId.isNotBlank()

        // Configurar título
        binding.tvFormTitle.text = if (isEditing) "Editar Producto" else "Agregar Producto"

        // Dropdown de categorías
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        binding.etCategory.setAdapter(categoryAdapter)

        // Construir chips de colores
        setupColorChips(emptyList())

        // Si es edición, pre-llenar el formulario
        if (isEditing) {
            val product = ShopRepository.getProductById(productId)
            if (product != null) {
                prefillForm(product)
            }
        }

        binding.ivBack.setOnClickListener { mainActivity.navigateUp() }

        // Guardar
        binding.btnSave.setOnClickListener {
            val product = buildProductFromForm(productId) ?: return@setOnClickListener
            if (isEditing) {
                adminViewModel.updateProduct(product)
            } else {
                adminViewModel.addProduct(product)
            }
        }

        // Observar resultado
        adminViewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !loading
        }

        adminViewModel.productSaved.observe(viewLifecycleOwner) { saved ->
            saved ?: return@observe
            if (saved) {
                // Recargar productos en ShopViewModel también
                mainActivity.shopViewModel.reloadProductsFromFirestore()
                mainActivity.navigateUp()
            }
            adminViewModel.clearProductSaved()
        }

        adminViewModel.message.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            adminViewModel.clearMessage()
        }
    }

    private fun prefillForm(product: Product) {
        binding.etName.setText(product.name)
        binding.etCategory.setText(product.category, false)
        binding.etDescription.setText(product.description)
        binding.etPrice.setText(product.price.toLong().toString())
        binding.etOriginalPrice.setText(product.originalPrice.toLong().toString())
        binding.etDiscount.setText(if (product.discount > 0) product.discount.toString() else "")
        binding.etEcoPoints.setText(product.ecoPoints.toString())
        binding.etImageUrl.setText(product.imageUrl)
        binding.etSizes.setText(product.sizes.joinToString(","))
        binding.cbIsNew.isChecked = product.isNew
        binding.cbInStock.isChecked = product.inStock

        // Marcar los chips de colores que ya tiene el producto
        setupColorChips(product.colors)
    }

    /**
     * Crea un chip por cada color disponible.
     * [selectedColors] indica cuáles deben aparecer marcados.
     */
    private fun setupColorChips(selectedColors: List<String>) {
        binding.chipGroupColors.removeAllViews()

        ColorConstants.ALL_COLORS.forEach { colorOption ->
            val chip = Chip(requireContext()).apply {
                text = colorOption.name
                isCheckable = true
                isChecked = selectedColors.any { it.equals(colorOption.name, ignoreCase = true) }

                // Círculo de color como icono del chip
                val circle = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    try { setColor(Color.parseColor(colorOption.hex)) }
                    catch (e: Exception) { setColor(Color.GRAY) }
                    setSize(40, 40)
                }
                chipIcon = circle
                isChipIconVisible = true
                chipIconTint = null          // no aplicar tinte al icono — mostrar color real
                chipIconSize = 18f * resources.displayMetrics.density  // 18dp en px
            }
            binding.chipGroupColors.addView(chip)
        }
    }

    /** Lee los nombres de los chips marcados como seleccionados. */
    private fun getSelectedColors(): List<String> {
        val selected = mutableListOf<String>()
        for (i in 0 until binding.chipGroupColors.childCount) {
            val chip = binding.chipGroupColors.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) selected.add(chip.text.toString())
        }
        return selected
    }

    private fun buildProductFromForm(existingId: String): Product? {
        val name         = binding.etName.text?.toString()?.trim() ?: ""
        val category     = binding.etCategory.text?.toString()?.trim() ?: ""
        val description  = binding.etDescription.text?.toString()?.trim() ?: ""
        val priceStr     = binding.etPrice.text?.toString()?.trim() ?: ""
        val originalPriceStr = binding.etOriginalPrice.text?.toString()?.trim() ?: ""
        val discountStr  = binding.etDiscount.text?.toString()?.trim() ?: "0"
        val ecoPointsStr = binding.etEcoPoints.text?.toString()?.trim() ?: "0"
        val imageUrl     = binding.etImageUrl.text?.toString()?.trim() ?: ""
        val sizesStr     = binding.etSizes.text?.toString()?.trim() ?: ""

        // Validaciones
        if (name.isBlank()) {
            binding.etName.error = "El nombre es requerido"
            binding.etName.requestFocus()
            return null
        }
        if (category.isBlank()) {
            Toast.makeText(requireContext(), "Selecciona una categoría", Toast.LENGTH_SHORT).show()
            return null
        }
        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0) {
            binding.etPrice.error = "Precio inválido"
            binding.etPrice.requestFocus()
            return null
        }

        val originalPrice = originalPriceStr.toDoubleOrNull() ?: price
        val discount  = discountStr.toIntOrNull() ?: 0
        val ecoPoints = ecoPointsStr.toIntOrNull() ?: 0
        val sizes  = if (sizesStr.isBlank()) emptyList()
                     else sizesStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val colors = getSelectedColors()   // Nombres de los chips marcados

        // Rating y reviewCount se mantienen si es edición, o se ponen en 0 para nuevo
        val existing = if (existingId.isNotBlank()) ShopRepository.getProductById(existingId) else null

        return Product(
            id = if (existingId.isNotBlank()) existingId else UUID.randomUUID().toString(),
            name = name,
            category = category,
            description = description,
            price = price,
            originalPrice = originalPrice,
            discount = discount.coerceIn(0, 99),
            imageUrl = imageUrl,
            rating = existing?.rating ?: 0f,
            reviewCount = existing?.reviewCount ?: 0,
            inStock = binding.cbInStock.isChecked,
            isNew = binding.cbIsNew.isChecked,
            sizes = sizes,
            colors = colors,
            ecoPoints = ecoPoints
        )
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
