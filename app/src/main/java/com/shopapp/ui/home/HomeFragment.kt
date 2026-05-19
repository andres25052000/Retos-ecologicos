package com.shopapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shopapp.R
import com.shopapp.data.model.ActivityChallenge
import com.shopapp.databinding.FragmentHomeBinding
import com.shopapp.databinding.ItemHomeEcoChallengeBinding
import com.shopapp.ui.home.adapter.BannerAdapter
import com.shopapp.ui.home.adapter.CategoryAdapter
import com.shopapp.ui.home.adapter.ProductAdapter

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = requireActivity() as MainActivity
        val viewModel = mainActivity.shopViewModel

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            binding.tvUserName.text =
                user?.fullName?.split(" ")?.firstOrNull() ?: "Explorar tienda"
        }

        // Notification bell
        binding.ivNotification.setOnClickListener {
            mainActivity.navigateToNotifications()
        }

        // Banners with navigation
        val bannerAdapter = BannerAdapter { banner ->
            mainActivity.navigateToCategoryProducts(banner.categoryTarget)
        }
        binding.vpBanners.adapter = bannerAdapter
        binding.dotsIndicator.attachTo(binding.vpBanners)
        viewModel.banners.observe(viewLifecycleOwner) { bannerAdapter.submitList(it) }

        val categoryAdapter = CategoryAdapter { category ->
            mainActivity.navigateToCategoryProducts(category.name)
        }
        binding.rvCategories.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false
            )
            adapter = categoryAdapter
        }
        viewModel.categories.observe(viewLifecycleOwner) { categoryAdapter.submitList(it) }

        val featuredAdapter = ProductAdapter(
            onProductClick = { mainActivity.navigateToProductDetail(it.id) },
            onFavoriteClick = { viewModel.toggleWishlist(it.id) },
            onAddToCartClick = { viewModel.addToCart(it) }
        )
        binding.rvFeatured.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false
            )
            adapter = featuredAdapter
        }
        viewModel.featuredProducts.observe(viewLifecycleOwner) { featuredAdapter.submitList(it) }

        val popularAdapter = ProductAdapter(
            onProductClick = { mainActivity.navigateToProductDetail(it.id) },
            onFavoriteClick = { viewModel.toggleWishlist(it.id) },
            onAddToCartClick = { viewModel.addToCart(it) }
        )
        binding.rvPopular.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false
            )
            adapter = popularAdapter
        }
        viewModel.popularProducts.observe(viewLifecycleOwner) { popularAdapter.submitList(it) }

        binding.searchBar.setOnClickListener { findNavController().navigate(R.id.searchFragment) }
        binding.tvSeeAllFeatured.setOnClickListener { mainActivity.navigateToCategoryProducts("Todos") }
        binding.tvSeeAllPopular.setOnClickListener { mainActivity.navigateToCategoryProducts("Todos") }
        binding.tvSeeAllCategories.setOnClickListener { mainActivity.navigateToCategoryProducts("Todos") }

        // ── Daily eco challenges ────────────────────────────────────────────
        viewModel.dailyActivityChallenges.observe(viewLifecycleOwner) { challenges ->
            renderDailyChallenges(challenges)
        }
        viewModel.refreshDailyActivityChallenges()

        binding.tvSeeAllChallenges.setOnClickListener {
            mainActivity.navigateToEcoTab()
        }
    }

    override fun onResume() {
        super.onResume()
        val viewModel = (requireActivity() as MainActivity).shopViewModel
        viewModel.refreshDailyActivityChallenges()
    }

    private fun renderDailyChallenges(challenges: List<ActivityChallenge>) {
        binding.layoutDailyChallenges.removeAllViews()
        if (challenges.isEmpty()) return

        challenges.forEach { challenge ->
            val itemBinding = ItemHomeEcoChallengeBinding.inflate(
                layoutInflater, binding.layoutDailyChallenges, false
            )
            itemBinding.tvHomeStatus.text = challenge.statusIcon
            itemBinding.tvHomeIcon.text = challenge.icon
            itemBinding.tvHomeTitle.text = challenge.title
            itemBinding.tvHomeProgress.text =
                "${challenge.currentProgress} / ${challenge.targetCount} ${challenge.unit}"

            if (challenge.isCompleted) {
                itemBinding.tvHomePts.text = "¡Hecho!"
            } else {
                itemBinding.tvHomePts.text = "+${challenge.ecoPoints}"
            }

            binding.layoutDailyChallenges.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
