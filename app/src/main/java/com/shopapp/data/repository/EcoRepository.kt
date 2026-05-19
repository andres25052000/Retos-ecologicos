package com.shopapp.data.repository

import com.shopapp.data.model.ChallengeType
import com.shopapp.data.model.EcoChallenge
import com.shopapp.data.model.EcoLevel

object EcoRepository {

    val levels = listOf(
        EcoLevel("Semilla Verde",    "🌱", 0,    99,   "#95D5B2", "Estás comenzando tu camino ecológico"),
        EcoLevel("Brote Consciente", "🌿", 100,  299,  "#52B788", "Tu conciencia ambiental está creciendo"),
        EcoLevel("Árbol Guardián",   "🌳", 300,  599,  "#2D6A4F", "Eres un guardián del medio ambiente"),
        EcoLevel("Defensor del Planeta", "🌍", 600, 999, "#1B4332", "Tu impacto positivo es inspirador"),
        EcoLevel("Héroe Ecológico",  "⭐", 1000, Int.MAX_VALUE, "#10B981", "¡Leyenda del planeta! Tu compromiso es total")
    )

    private val challengeTemplates = listOf(
        EcoChallenge(
            id = "first_purchase",
            title = "Primera compra verde",
            description = "Realiza tu primera compra ecológica",
            icon = "🛒",
            type = ChallengeType.FIRST_PURCHASE,
            target = 1,
            rewardPoints = 50
        ),
        EcoChallenge(
            id = "eco_100",
            title = "Guardián en formación",
            description = "Acumula 100 puntos eco",
            icon = "🌿",
            type = ChallengeType.TOTAL_ECO_POINTS,
            target = 100,
            rewardPoints = 30
        ),
        EcoChallenge(
            id = "eco_300",
            title = "Árbol de impacto",
            description = "Acumula 300 puntos eco",
            icon = "🌳",
            type = ChallengeType.TOTAL_ECO_POINTS,
            target = 300,
            rewardPoints = 75
        ),
        EcoChallenge(
            id = "eco_600",
            title = "Defensor comprometido",
            description = "Acumula 600 puntos eco",
            icon = "🌍",
            type = ChallengeType.TOTAL_ECO_POINTS,
            target = 600,
            rewardPoints = 150
        ),
        EcoChallenge(
            id = "purchases_3",
            title = "Hábito sostenible",
            description = "Realiza 3 compras ecológicas",
            icon = "♻️",
            type = ChallengeType.PURCHASE_COUNT,
            target = 3,
            rewardPoints = 60
        ),
        EcoChallenge(
            id = "purchases_10",
            title = "Estilo de vida verde",
            description = "Realiza 10 compras ecológicas",
            icon = "💚",
            type = ChallengeType.PURCHASE_COUNT,
            target = 10,
            rewardPoints = 200
        ),
        EcoChallenge(
            id = "categories_3",
            title = "Explorador ecológico",
            description = "Compra productos de 3 categorías distintas",
            icon = "🔍",
            type = ChallengeType.CATEGORY_DIVERSITY,
            target = 3,
            rewardPoints = 80
        ),
        EcoChallenge(
            id = "categories_5",
            title = "Eco completo",
            description = "Compra productos de 5 categorías distintas",
            icon = "🏆",
            type = ChallengeType.CATEGORY_DIVERSITY,
            target = 5,
            rewardPoints = 120
        )
    )

    fun getChallenges(totalEcoPoints: Int, purchaseCount: Int, categoriesBought: Set<String>): List<EcoChallenge> {
        return challengeTemplates.map { template ->
            val progress = when (template.type) {
                ChallengeType.FIRST_PURCHASE      -> purchaseCount.coerceAtMost(1)
                ChallengeType.TOTAL_ECO_POINTS    -> totalEcoPoints
                ChallengeType.PURCHASE_COUNT      -> purchaseCount
                ChallengeType.CATEGORY_DIVERSITY  -> categoriesBought.size
            }
            template.copy(
                currentProgress = progress,
                isCompleted = progress >= template.target
            )
        }
    }

    fun getLevelForPoints(points: Int): EcoLevel =
        levels.lastOrNull { points >= it.minPoints } ?: levels.first()

    fun getNextLevel(points: Int): EcoLevel? {
        val current = getLevelForPoints(points)
        return levels.firstOrNull { it.minPoints > current.minPoints }
    }

    fun pointsToNextLevel(points: Int): Int {
        val next = getNextLevel(points) ?: return 0
        return next.minPoints - points
    }

    fun progressToNextLevel(points: Int): Int {
        val current = getLevelForPoints(points)
        val next = getNextLevel(points) ?: return 100
        val range = next.minPoints - current.minPoints
        val earned = points - current.minPoints
        return if (range == 0) 100 else ((earned * 100) / range).coerceIn(0, 100)
    }
}
