package com.shopapp.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.shopapp.databinding.FragmentSearchBinding
import com.shopapp.ui.home.adapter.ProductAdapter

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel

        val adapter = ProductAdapter(
            onProductClick = { mainActivity.navigateToProductDetail(it.id) },
            onFavoriteClick = { viewModel.toggleWishlist(it.id) },
            onAddToCartClick = { viewModel.addToCart(it) }
        )

        binding.rvResults.apply {
            layoutManager = GridLayoutManager(context, 2)
            this.adapter = adapter
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                viewModel.searchProducts(query)
            }
        })

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = binding.etSearch.text?.toString()?.trim() ?: ""
                viewModel.searchProducts(query)
                true
            } else false
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            val query = binding.etSearch.text?.toString()?.trim() ?: ""
            if (query.isBlank()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvResults.visibility = View.GONE
                binding.tvEmptyTitle.text = "Busca tus productos"
                binding.tvEmptySubtitle.text = "Escribe el nombre del producto que deseas encontrar"
            } else if (results.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rvResults.visibility = View.GONE
                binding.tvEmptyTitle.text = "Sin resultados"
                binding.tvEmptySubtitle.text = "No encontramos productos para \"$query\""
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.rvResults.visibility = View.VISIBLE
                adapter.submitList(results)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
