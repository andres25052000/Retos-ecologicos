package com.shopapp.data.repository

import com.shopapp.data.model.ActivityChallenge
import com.shopapp.data.model.ChallengePeriod
import java.text.SimpleDateFormat
import java.util.*

object ActivityChallengeRepository {

    /** All challenge templates (progress reset each period) */
    val templates: List<ActivityChallenge> = listOf(

        // ── DAILY ─────────────────────────────────────────────────────────────
        ActivityChallenge(
            id = "daily_no_plastic",
            title = "Sin bolsas plásticas",
            description = "Usa bolsa reutilizable al hacer compras hoy",
            icon = "🛍️",
            period = ChallengePeriod.DAILY,
            targetCount = 1,
            unit = "vez",
            ecoPoints = 10
        ),
        ActivityChallenge(
            id = "daily_bike_walk",
            title = "Bici o caminata",
            description = "Desplázate en bicicleta o caminando en vez de coche",
            icon = "🚴",
            period = ChallengePeriod.DAILY,
            targetCount = 1,
            unit = "vez",
            ecoPoints = 15
        ),
        ActivityChallenge(
            id = "daily_own_bottle",
            title = "Tu propio vaso/botella",
            description = "Lleva tu botella reutilizable en vez de comprar plástico",
            icon = "🧴",
            period = ChallengePeriod.DAILY,
            targetCount = 1,
            unit = "vez",
            ecoPoints = 10
        ),

        // ── WEEKLY ────────────────────────────────────────────────────────────
        ActivityChallenge(
            id = "weekly_recycle_bottles",
            title = "Reciclar botellas",
            description = "Lleva botellas plásticas al punto de reciclaje esta semana",
            icon = "♻️",
            period = ChallengePeriod.WEEKLY,
            targetCount = 5,
            unit = "botellas",
            ecoPoints = 30
        ),
        ActivityChallenge(
            id = "weekly_public_transport",
            title = "Transporte público",
            description = "Usa bus/metro en lugar de coche particular esta semana",
            icon = "🚌",
            period = ChallengePeriod.WEEKLY,
            targetCount = 3,
            unit = "veces",
            ecoPoints = 25
        ),
        ActivityChallenge(
            id = "weekly_local_market",
            title = "Mercado local",
            description = "Compra en el mercado o tienda de barrio (sin supermercado)",
            icon = "🥦",
            period = ChallengePeriod.WEEKLY,
            targetCount = 2,
            unit = "veces",
            ecoPoints = 20
        ),

        // ── MONTHLY ───────────────────────────────────────────────────────────
        ActivityChallenge(
            id = "monthly_plant_tree",
            title = "Plantar una planta",
            description = "Planta un árbol, arbusto o planta en casa o comunidad",
            icon = "🌳",
            period = ChallengePeriod.MONTHLY,
            targetCount = 1,
            unit = "planta",
            ecoPoints = 100
        ),
        ActivityChallenge(
            id = "monthly_reduce_waste",
            title = "Reducir residuos",
            description = "Clasifica y separa residuos correctamente en el hogar",
            icon = "🗑️",
            period = ChallengePeriod.MONTHLY,
            targetCount = 4,
            unit = "semanas",
            ecoPoints = 50
        ),
        ActivityChallenge(
            id = "monthly_compost",
            title = "Compostaje en casa",
            description = "Haz compost con residuos orgánicos del hogar",
            icon = "🌱",
            period = ChallengePeriod.MONTHLY,
            targetCount = 2,
            unit = "veces",
            ecoPoints = 60
        )
    )

    // ── Period key helpers ─────────────────────────────────────────────────────

    fun dailyKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun weeklyKey(): String {
        val cal = Calendar.getInstance()
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        val year = cal.get(Calendar.YEAR)
        return "%d-W%02d".format(year, week)
    }

    fun monthlyKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    fun periodKey(period: ChallengePeriod): String = when (period) {
        ChallengePeriod.DAILY   -> dailyKey()
        ChallengePeriod.WEEKLY  -> weeklyKey()
        ChallengePeriod.MONTHLY -> monthlyKey()
    }

    /** Firestore document id for a challenge in its current period */
    fun docId(challenge: ActivityChallenge): String = "${periodKey(challenge.period)}_${challenge.id}"

    fun dailyChallenges() = templates.filter { it.period == ChallengePeriod.DAILY }
    fun weeklyChallenges() = templates.filter { it.period == ChallengePeriod.WEEKLY }
    fun monthlyChallenges() = templates.filter { it.period == ChallengePeriod.MONTHLY }
}
