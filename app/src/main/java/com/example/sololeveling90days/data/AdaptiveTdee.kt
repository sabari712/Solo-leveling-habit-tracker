package com.example.sololeveling90days.data

import kotlinx.serialization.Serializable

/**
 * Adaptive TDEE Estimator — a 2-state linear Kalman filter.
 *
 * Starts from a population-level TDEE formula (Mifflin-St Jeor) and corrects it
 * day-by-day using logged calorie intake and morning scale weight.
 *
 * State vector: x = [weight_kg, tdee_kcal]
 * Process model: weight(t+1) = weight(t) + (intake(t) - tdee(t)) / KCAL_PER_KG
 *                tdee(t+1)   = tdee(t)
 * Observation:   scale(t)    = weight(t) + noise
 */

/** Approximate energy density of body-mass change. */
const val KCAL_PER_KG = 7700.0

// ---------------------------------------------------------------------------
// Mifflin-St Jeor helpers
// ---------------------------------------------------------------------------

fun mifflinStJeorBmr(weightKg: Double, heightCm: Double, ageYears: Double, sex: String): Double {
    return when (sex.trim().lowercase()) {
        "male" -> 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears + 5.0
        "female" -> 10.0 * weightKg + 6.25 * heightCm - 5.0 * ageYears - 161.0
        else -> throw IllegalArgumentException("sex must be 'male' or 'female', got '$sex'")
    }
}

val ACTIVITY_FACTORS = mapOf(
    "sedentary" to 1.200,
    "light" to 1.375,
    "moderate" to 1.550,
    "active" to 1.725,
    "very_active" to 1.900
)

fun mifflinStJeorTdee(
    weightKg: Double, heightCm: Double, ageYears: Double,
    sex: String, activityLevel: String
): Double {
    val level = activityLevel.trim().lowercase()
    val factor = ACTIVITY_FACTORS[level]
        ?: throw IllegalArgumentException("Unknown activity_level: '$activityLevel'")
    return mifflinStJeorBmr(weightKg, heightCm, ageYears, sex) * factor
}

// ---------------------------------------------------------------------------
// FilterConfig
// ---------------------------------------------------------------------------

data class TdeeFilterConfig(
    val weightProcessStdKg: Double = 0.06,
    val tdeeProcessStdKcal: Double = 2.0,
    val weightObsStdKg: Double = 0.4,
    val initialTdeeStdKcal: Double = 300.0,
    val initialWeightStdKg: Double = 0.3,
    val minTdeeKcal: Double = 800.0,
    val maxTdeeKcal: Double = 6000.0
)

// ---------------------------------------------------------------------------
// DailyEstimate snapshot
// ---------------------------------------------------------------------------

@Serializable
data class TdeeDailyEstimate(
    val day: Int,
    val weightKg: Double,
    val weightStdKg: Double,
    val tdeeKcal: Double,
    val tdeeStdKcal: Double,
    val usedObservation: Boolean
)

// ---------------------------------------------------------------------------
// AdaptiveTDEEEstimator — the Kalman filter
// ---------------------------------------------------------------------------

class AdaptiveTdeeEstimator(
    initialWeightKg: Double,
    formulaTdeeKcal: Double,
    private val config: TdeeFilterConfig = TdeeFilterConfig()
) {
    // State vector: [weight_kg, tdee_kcal]
    private var x = doubleArrayOf(initialWeightKg, formulaTdeeKcal)

    // 2x2 covariance matrix stored as [p00, p01, p10, p11]
    private var p = doubleArrayOf(
        config.initialWeightStdKg * config.initialWeightStdKg, 0.0,
        0.0, config.initialTdeeStdKcal * config.initialTdeeStdKcal
    )

    var day: Int = 0
        private set

    val history = mutableListOf<TdeeDailyEstimate>()

    init {
        history.add(snapshot(usedObs = false))
    }

    // Current estimates
    val currentTdee: Double get() = x[1]
    val currentWeight: Double get() = x[0]
    val currentTdeeConfidenceInterval: Pair<Double, Double>
        get() {
            val std = kotlin.math.sqrt(p[3])
            return Pair(x[1] - 1.96 * std, x[1] + 1.96 * std)
        }

    /**
     * Advance the filter by one day.
     *
     * @param intakeKcal logged calorie intake for this day
     * @param observedWeightKg scale reading, or null to skip measurement update
     */
    fun step(intakeKcal: Double, observedWeightKg: Double? = null): TdeeDailyEstimate {
        val invKcal = 1.0 / KCAL_PER_KG
        val qW = config.weightProcessStdKg * config.weightProcessStdKg
        val qT = config.tdeeProcessStdKcal * config.tdeeProcessStdKcal
        val r = config.weightObsStdKg * config.weightObsStdKg

        // --- PREDICT ---
        // A = [[1, -invKcal], [0, 1]]
        // x_pred = A * x + B * intake
        val xPredW = x[0] + (-invKcal) * x[1] + invKcal * intakeKcal
        val xPredT = x[1]

        // P_pred = A * P * A^T + Q
        // A*P: row0 = [p00 - invKcal*p10, p01 - invKcal*p11]
        //      row1 = [p10, p11]
        val ap00 = p[0] + (-invKcal) * p[2]
        val ap01 = p[1] + (-invKcal) * p[3]
        val ap10 = p[2]
        val ap11 = p[3]

        // (A*P) * A^T:
        // [ap00*1 + ap01*0, ap00*(-invKcal) + ap01*1]
        // [ap10*1 + ap11*0, ap10*(-invKcal) + ap11*1]
        val pp00 = ap00 + qW
        val pp01 = ap00 * (-invKcal) + ap01
        val pp10 = ap10
        val pp11 = ap10 * (-invKcal) + ap11 + qT

        // --- UPDATE ---
        val usedObs = observedWeightKg != null
        if (usedObs) {
            val z = observedWeightKg!!
            // H = [1, 0]
            // y = z - H * x_pred = z - xPredW
            val innovation = z - xPredW
            // S = H * P_pred * H^T + R = pp00 + r
            val s = pp00 + r
            val sInv = 1.0 / s
            // K = P_pred * H^T * S^-1 = [pp00/s, pp10/s]
            val k0 = pp00 * sInv
            val k1 = pp10 * sInv

            x[0] = xPredW + k0 * innovation
            x[1] = xPredT + k1 * innovation

            // P = (I - K*H) * P_pred
            // I - K*H = [[1-k0, 0], [-k1, 1]]
            p[0] = (1.0 - k0) * pp00
            p[1] = (1.0 - k0) * pp01
            p[2] = -k1 * pp00 + pp10
            p[3] = -k1 * pp01 + pp11
        } else {
            x[0] = xPredW
            x[1] = xPredT
            p[0] = pp00; p[1] = pp01; p[2] = pp10; p[3] = pp11
        }

        // --- CLAMP ---
        x[1] = x[1].coerceIn(config.minTdeeKcal, config.maxTdeeKcal)

        // --- RECORD ---
        day++
        val est = snapshot(usedObs)
        history.add(est)
        return est
    }

    private fun snapshot(usedObs: Boolean) = TdeeDailyEstimate(
        day = day,
        weightKg = x[0],
        weightStdKg = kotlin.math.sqrt(p[0]),
        tdeeKcal = x[1],
        tdeeStdKcal = kotlin.math.sqrt(p[3]),
        usedObservation = usedObs
    )
}
