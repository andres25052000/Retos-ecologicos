package com.shopapp.data.model

data class EcoChallenge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val type: ChallengeType,
    val target: Int,
    val rewardPoints: Int,
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false
) {
    val progressPercent: Int
        get() = if (target == 0) 100 else minOf((currentProgress * 100) / target, 100)
}

enum class ChallengeType {
    FIRST_PURCHASE,
    TOTAL_ECO_POINTS,
    PURCHASE_COUNT,
    CATEGORY_DIVERSITY
}

data class EcoLevel(
    val name: String,
    val emoji: String,
    val minPoints: Int,
    val maxPoints: Int,
    val hexColor: String,
    val description: String
)
