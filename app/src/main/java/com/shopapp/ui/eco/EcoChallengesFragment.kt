package com.shopapp.ui.eco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shopapp.data.model.EcoChallenge
import com.shopapp.data.repository.EcoRepository
import com.shopapp.data.repository.UserRepository
import com.shopapp.databinding.FragmentEcoChallengesBinding
import com.shopapp.databinding.ItemEcoChallengeBinding
import com.shopapp.ui.home.MainActivity

class EcoChallengesFragment : Fragment() {

    private var _binding: FragmentEcoChallengesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEcoChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = (requireActivity() as MainActivity).shopViewModel

        viewModel.ecoPoints.observe(viewLifecycleOwner) { points ->
            updateLevelCard(points)
        }

        viewModel.ecoChallenges.observe(viewLifecycleOwner) { challenges ->
            renderChallenges(challenges)
        }

        viewModel.refreshEco()
    }

    private fun updateLevelCard(points: Int) {
        val level = EcoRepository.getLevelForPoints(points)
        val nextLevel = EcoRepository.getNextLevel(points)
        val progressPercent = EcoRepository.progressToNextLevel(points)
        val ptsToNext = EcoRepository.pointsToNextLevel(points)

        binding.tvLevelEmoji.text = level.emoji
        binding.tvLevelName.text = level.name
        binding.tvLevelDescription.text = level.description
        binding.tvTotalPoints.text = points.toString()

        binding.tvNextLevelLabel.text = if (nextLevel != null) {
            "Faltan $ptsToNext pts para ${nextLevel.emoji} ${nextLevel.name}"
        } else {
            "¡Nivel máximo alcanzado! Eres un Héroe Ecológico ⭐"
        }

        // Animate progress bar
        binding.vLevelProgress.post {
            val parentWidth = (binding.vLevelProgress.parent as View).width
            val fillWidth = (parentWidth * progressPercent / 100)
            binding.vLevelProgress.layoutParams = binding.vLevelProgress.layoutParams.also {
                it.width = fillWidth.coerceAtLeast(16)
            }
            binding.vLevelProgress.requestLayout()
        }

        // Stats
        val purchaseCount = UserRepository.getPurchaseCount()
        val categories = UserRepository.getCategoriesBought()
        binding.tvStatPurchases.text = purchaseCount.toString()
        binding.tvStatCategories.text = categories.size.toString()
        binding.tvStatCo2.text = "${points / 10} kg"
    }

    private fun renderChallenges(challenges: List<EcoChallenge>) {
        binding.layoutChallenges.removeAllViews()
        challenges.forEach { challenge ->
            val itemBinding = ItemEcoChallengeBinding.inflate(layoutInflater, binding.layoutChallenges, false)

            itemBinding.tvChallengeIcon.text = challenge.icon
            itemBinding.tvChallengeTitle.text = challenge.title
            itemBinding.tvChallengeDesc.text = challenge.description
            itemBinding.tvRewardPoints.text = "+${challenge.rewardPoints} pts"

            if (challenge.isCompleted) {
                itemBinding.tvCompletedCheck.visibility = View.VISIBLE
                itemBinding.tvRewardPoints.text = "¡Completado!"
            } else {
                itemBinding.tvCompletedCheck.visibility = View.GONE
            }

            // Progress bar
            val progressPercent = challenge.progressPercent
            itemBinding.vProgressFill.post {
                val parentWidth = (itemBinding.vProgressFill.parent as View).width
                val fillWidth = (parentWidth * progressPercent / 100)
                itemBinding.vProgressFill.layoutParams = itemBinding.vProgressFill.layoutParams.also {
                    it.width = fillWidth.coerceAtLeast(if (progressPercent > 0) 16 else 0)
                }
                itemBinding.vProgressFill.requestLayout()
            }

            val progressLabel = when (challenge.type.name) {
                "FIRST_PURCHASE" -> "${challenge.currentProgress} / ${challenge.target} compra"
                "TOTAL_ECO_POINTS" -> "${challenge.currentProgress} / ${challenge.target} pts"
                "PURCHASE_COUNT" -> "${challenge.currentProgress} / ${challenge.target} compras"
                "CATEGORY_DIVERSITY" -> "${challenge.currentProgress} / ${challenge.target} categorías"
                else -> "${challenge.currentProgress} / ${challenge.target}"
            }
            itemBinding.tvProgressText.text = progressLabel

            binding.layoutChallenges.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
