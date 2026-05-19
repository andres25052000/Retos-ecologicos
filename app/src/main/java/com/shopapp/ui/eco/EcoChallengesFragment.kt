package com.shopapp.ui.eco

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.shopapp.AppSession
import com.shopapp.R
import com.shopapp.data.model.ActivityChallenge
import com.shopapp.data.model.ChallengePeriod
import com.shopapp.data.model.EcoChallenge
import com.shopapp.data.repository.EcoRepository
import com.shopapp.data.repository.FirestoreActivityChallengeRepository
import com.shopapp.data.repository.UserRepository
import com.shopapp.databinding.FragmentEcoChallengesBinding
import com.shopapp.databinding.ItemActivityChallengeBinding
import com.shopapp.databinding.ItemEcoChallengeBinding
import com.shopapp.ui.home.MainActivity

class EcoChallengesFragment : Fragment() {

    private var _binding: FragmentEcoChallengesBinding? = null
    private val binding get() = _binding!!

    private val uid get() = AppSession.userId

    /** Currently selected period filter (null = all) */
    private var selectedPeriod: ChallengePeriod? = null

    /** Latest loaded activity challenges (all periods) */
    private var activityChallenges: List<ActivityChallenge> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEcoChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = (requireActivity() as MainActivity).shopViewModel

        // ── Level card ──────────────────────────────────────────────────────
        viewModel.ecoPoints.observe(viewLifecycleOwner) { points ->
            updateLevelCard(points)
        }

        // ── Purchase challenges ─────────────────────────────────────────────
        viewModel.ecoChallenges.observe(viewLifecycleOwner) { challenges ->
            renderPurchaseChallenges(challenges)
        }

        viewModel.refreshEco()

        // ── Activity challenges ─────────────────────────────────────────────
        setupFilterChips()
        loadActivityChallenges()
    }

    override fun onResume() {
        super.onResume()
        loadActivityChallenges()
    }

    // ── Filter chips ─────────────────────────────────────────────────────────

    private fun setupFilterChips() {
        val chips = listOf(
            binding.chipAll to null,
            binding.chipDaily to ChallengePeriod.DAILY,
            binding.chipWeekly to ChallengePeriod.WEEKLY,
            binding.chipMonthly to ChallengePeriod.MONTHLY
        )

        fun selectChip(period: ChallengePeriod?) {
            selectedPeriod = period
            chips.forEach { (chip, p) -> styleChip(chip, p == period) }
            renderActivityChallenges()
        }

        chips.forEach { (chip, period) ->
            chip.setOnClickListener { selectChip(period) }
        }

        // Default: show all
        selectChip(null)
    }

    private fun styleChip(chip: TextView, selected: Boolean) {
        val context = chip.context
        val bg = GradientDrawable().apply {
            cornerRadius = 50f * context.resources.displayMetrics.density
            if (selected) {
                setColor(ContextCompat.getColor(context, R.color.primary))
            } else {
                setColor(ContextCompat.getColor(context, R.color.surface))
                setStroke(
                    (1 * context.resources.displayMetrics.density).toInt(),
                    ContextCompat.getColor(context, R.color.divider)
                )
            }
        }
        chip.background = bg
        chip.setTextColor(
            if (selected) ContextCompat.getColor(context, R.color.white)
            else ContextCompat.getColor(context, R.color.text_secondary)
        )
    }

    // ── Activity challenges ──────────────────────────────────────────────────

    private fun loadActivityChallenges() {
        FirestoreActivityChallengeRepository.getAll(uid) { challenges ->
            activity?.runOnUiThread {
                activityChallenges = challenges
                renderActivityChallenges()
                updateActivityStats(challenges)
            }
        }
    }

    private fun renderActivityChallenges() {
        binding.layoutActivityChallenges.removeAllViews()
        val filtered = if (selectedPeriod == null) {
            activityChallenges
        } else {
            activityChallenges.filter { it.period == selectedPeriod }
        }

        if (filtered.isEmpty()) {
            val empty = TextView(requireContext()).apply {
                text = "No hay retos para este período"
                textSize = 13f
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                setPadding(0, 16, 0, 16)
            }
            binding.layoutActivityChallenges.addView(empty)
            return
        }

        filtered.forEach { challenge ->
            val itemBinding = ItemActivityChallengeBinding.inflate(
                layoutInflater, binding.layoutActivityChallenges, false
            )
            bindActivityChallenge(itemBinding, challenge)
            binding.layoutActivityChallenges.addView(itemBinding.root)
        }
    }

    private fun bindActivityChallenge(
        itemBinding: ItemActivityChallengeBinding,
        challenge: ActivityChallenge
    ) {
        itemBinding.tvActIcon.text = challenge.icon
        itemBinding.tvActTitle.text = challenge.title
        itemBinding.tvActDesc.text = challenge.description
        itemBinding.tvActPeriod.text = challenge.period.label
        itemBinding.tvActStatus.text = challenge.statusIcon
        itemBinding.tvActProgressText.text =
            "${challenge.currentProgress} / ${challenge.targetCount} ${challenge.unit}"

        if (challenge.isCompleted) {
            itemBinding.tvActPoints.text = "¡Completado!"
            itemBinding.btnActIncrement.visibility = View.GONE
        } else {
            itemBinding.tvActPoints.text = "+${challenge.ecoPoints} pts"
            itemBinding.btnActIncrement.visibility = View.VISIBLE
        }

        // Progress bar
        itemBinding.vActProgressFill.post {
            val parentWidth = (itemBinding.vActProgressFill.parent as View).width
            val fillWidth = (parentWidth * challenge.progressPercent / 100)
            itemBinding.vActProgressFill.layoutParams =
                itemBinding.vActProgressFill.layoutParams.also {
                    it.width = fillWidth.coerceAtLeast(
                        if (challenge.currentProgress > 0) 16 else 0
                    )
                }
            itemBinding.vActProgressFill.requestLayout()
        }

        // +1 button
        itemBinding.btnActIncrement.setOnClickListener {
            if (challenge.isCompleted) return@setOnClickListener
            itemBinding.btnActIncrement.isEnabled = false

            FirestoreActivityChallengeRepository.incrementProgress(uid, challenge) { updated ->
                activity?.runOnUiThread {
                    activityChallenges = activityChallenges.map {
                        if (it.id == updated.id) updated else it
                    }
                    renderActivityChallenges()

                    val viewModel = (requireActivity() as MainActivity).shopViewModel
                    viewModel.refreshEco()

                    if (updated.isCompleted && !challenge.isCompleted) {
                        Toast.makeText(
                            requireContext(),
                            "🎉 ¡Reto completado! +${updated.ecoPoints} pts eco",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun updateActivityStats(challenges: List<ActivityChallenge>) {
        val completedCount = challenges.count { it.isCompleted }
        binding.tvStatActivities.text = completedCount.toString()
    }

    // ── Level card ───────────────────────────────────────────────────────────

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

        // Points breakdown
        val purchasePts = UserRepository.getTotalEcoPoints()
        val activityPts = activityChallenges
            .filter { it.isCompleted }
            .sumOf { it.ecoPoints }
        binding.tvPurchasePts.text = "🛒 $purchasePts pts (compras)"
        binding.tvActivityPts.text = "🌿 $activityPts pts (actividad)"

        // Animate progress bar
        binding.vLevelProgress.post {
            val parentWidth = (binding.vLevelProgress.parent as View).width
            val fillWidth = (parentWidth * progressPercent / 100)
            binding.vLevelProgress.layoutParams =
                binding.vLevelProgress.layoutParams.also {
                    it.width = fillWidth.coerceAtLeast(16)
                }
            binding.vLevelProgress.requestLayout()
        }

        // Stats
        val purchaseCount = UserRepository.getPurchaseCount()
        binding.tvStatPurchases.text = purchaseCount.toString()
        binding.tvStatCo2.text = "${points / 10} kg"
    }

    // ── Purchase challenges ──────────────────────────────────────────────────

    private fun renderPurchaseChallenges(challenges: List<EcoChallenge>) {
        binding.layoutChallenges.removeAllViews()
        challenges.forEach { challenge ->
            val itemBinding = ItemEcoChallengeBinding.inflate(
                layoutInflater, binding.layoutChallenges, false
            )

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

            val progressPercent = challenge.progressPercent
            itemBinding.vProgressFill.post {
                val parentWidth = (itemBinding.vProgressFill.parent as View).width
                val fillWidth = (parentWidth * progressPercent / 100)
                itemBinding.vProgressFill.layoutParams =
                    itemBinding.vProgressFill.layoutParams.also {
                        it.width = fillWidth.coerceAtLeast(
                            if (progressPercent > 0) 16 else 0
                        )
                    }
                itemBinding.vProgressFill.requestLayout()
            }

            val progressLabel = when (challenge.type.name) {
                "FIRST_PURCHASE"     -> "${challenge.currentProgress} / ${challenge.target} compra"
                "TOTAL_ECO_POINTS"   -> "${challenge.currentProgress} / ${challenge.target} pts"
                "PURCHASE_COUNT"     -> "${challenge.currentProgress} / ${challenge.target} compras"
                "CATEGORY_DIVERSITY" -> "${challenge.currentProgress} / ${challenge.target} categorías"
                else -> "${challenge.currentProgress} / ${challenge.target}"
            }
            itemBinding.tvProgressText.text = progressLabel

            binding.layoutChallenges.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
