package com.shopapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.shopapp.data.model.ActivityChallenge
import com.shopapp.data.model.ChallengePeriod

/**
 * Stores per-period activity challenge progress in Firestore.
 *
 * Path: users/{uid}/activityProgress/{periodKey}_{challengeId}
 * Fields: challengeId, periodKey, progress, isCompleted, pointsAwarded
 */
object FirestoreActivityChallengeRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun col(uid: String) =
        db.collection("users").document(uid).collection("activityProgress")

    /**
     * Loads all activity challenges for the current period, merging Firestore
     * progress into the static templates. Calls [callback] on the main thread
     * with the resulting list.
     */
    fun getAll(uid: String, callback: (List<ActivityChallenge>) -> Unit) {
        if (uid.isBlank()) {
            callback(ActivityChallengeRepository.templates.map { it.copy() })
            return
        }

        col(uid).get()
            .addOnSuccessListener { snapshot ->
                // Build a map: docId -> progress data
                val progressMap = mutableMapOf<String, Map<String, Any>>()
                for (doc in snapshot.documents) {
                    doc.data?.let { progressMap[doc.id] = it }
                }

                val result = ActivityChallengeRepository.templates.map { template ->
                    val docId = ActivityChallengeRepository.docId(template)
                    val data = progressMap[docId]
                    if (data != null) {
                        val progress = (data["progress"] as? Long)?.toInt() ?: 0
                        val completed = data["isCompleted"] as? Boolean ?: false
                        val awarded = data["pointsAwarded"] as? Boolean ?: false
                        template.copy(
                            currentProgress = progress,
                            isCompleted = completed,
                            pointsAwarded = awarded
                        )
                    } else {
                        template.copy()
                    }
                }
                callback(result)
            }
            .addOnFailureListener {
                callback(ActivityChallengeRepository.templates.map { it.copy() })
            }
    }

    /**
     * Increments the progress of a challenge by [amount].
     * If the challenge becomes complete AND points haven't been awarded yet,
     * adds eco points to the user's total and sets pointsAwarded = true.
     * Calls [onResult] with the updated challenge.
     */
    fun incrementProgress(
        uid: String,
        challenge: ActivityChallenge,
        amount: Int = 1,
        onResult: (updated: ActivityChallenge) -> Unit
    ) {
        if (uid.isBlank()) {
            onResult(challenge)
            return
        }

        val docId = ActivityChallengeRepository.docId(challenge)
        val docRef = col(uid).document(docId)
        val periodKey = ActivityChallengeRepository.periodKey(challenge.period)

        db.runTransaction { tx ->
            val snap = tx.get(docRef)
            val currentProgress = if (snap.exists()) {
                (snap.getLong("progress") ?: 0L).toInt()
            } else 0
            val alreadyCompleted = snap.getBoolean("isCompleted") ?: false
            val pointsAwarded = snap.getBoolean("pointsAwarded") ?: false

            val newProgress = (currentProgress + amount).coerceAtMost(challenge.targetCount)
            val nowCompleted = newProgress >= challenge.targetCount

            val data = hashMapOf(
                "challengeId" to challenge.id,
                "periodKey" to periodKey,
                "progress" to newProgress,
                "isCompleted" to nowCompleted,
                "pointsAwarded" to (pointsAwarded || (nowCompleted && !alreadyCompleted))
            )
            tx.set(docRef, data)

            // Return whether we should award points
            Triple(newProgress, nowCompleted, nowCompleted && !pointsAwarded)
        }.addOnSuccessListener { (newProgress, nowCompleted, awardPoints) ->
            // Award eco points if challenge just completed
            if (awardPoints) {
                FirestoreUserRepository.addEcoPoints(uid, challenge.ecoPoints)
                UserRepository.addEcoPoints(challenge.ecoPoints)
            }
            onResult(
                challenge.copy(
                    currentProgress = newProgress,
                    isCompleted = nowCompleted,
                    pointsAwarded = nowCompleted
                )
            )
        }.addOnFailureListener {
            onResult(challenge)
        }
    }

    /**
     * Returns only the daily challenges with current progress loaded.
     */
    fun getDailyChallenges(uid: String, callback: (List<ActivityChallenge>) -> Unit) {
        getAll(uid) { all ->
            callback(all.filter { it.period == ChallengePeriod.DAILY })
        }
    }
}
