package com.shopapp.data.model

data class ActivityChallenge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val period: ChallengePeriod,
    val targetCount: Int,
    val unit: String,          // "veces", "botellas", etc.
    val ecoPoints: Int,        // reward on completion
    var currentProgress: Int = 0,
    var isCompleted: Boolean = false,
    var pointsAwarded: Boolean = false
) {
    val progressPercent: Int
        get() = if (targetCount == 0) 100 else minOf((currentProgress * 100) / targetCount, 100)

    val statusIcon: String
        get() = when {
            isCompleted -> "✅"
            currentProgress > 0 -> "🔄"
            else -> "⭕"
        }
}

enum class ChallengePeriod(val label: String) {
    DAILY("Diario"),
    WEEKLY("Semanal"),
    MONTHLY("Mensual")
}
