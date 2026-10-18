package de.fitapp.app.util

import de.fitapp.app.data.entity.ExerciseBaselineEntity
import de.fitapp.app.data.entity.ExerciseLogEntity
import de.fitapp.app.data.entity.Gender
import de.fitapp.app.data.entity.MuscleGroup
import de.fitapp.app.data.entity.UserProfile
import java.time.LocalDate
import kotlin.math.ln
import kotlin.math.min

const val TRAINING_WINDOW_DAYS = 90L
private const val RATING_LOG2_SCALE = 3f
private const val RATING_BASELINE = 5f
private const val TWELVE_REP_FACTOR = 1.4f

data class MuscleProgressResult(
    val group: MuscleGroup,
    val hasEnoughData: Boolean,
    val rating: Float,
    val totalSets: Int,
    val averageTwelveRepWeightKg: Float,
    val averageTwelveRepBenchmarkKg: Float
) {
    val ratingPercent: Int get() = ((rating / 10f) * 100f).toInt()
}

/** A single consistent scale: average = 5/10, twice average = 8/10, four times = 10/10. */
object MuscleProgressCalculator {

    fun calculate(
        logs: List<ExerciseLogEntity>,
        baselines: List<ExerciseBaselineEntity>,
        profile: UserProfile?,
        today: LocalDate = LocalDate.now()
    ): List<MuscleProgressResult> {
        val sinceEpochDay = today.minusDays(TRAINING_WINDOW_DAYS).toEpochDay()
        val age = profile?.age ?: 30
        val gender = profile?.gender ?: Gender.DIVERS
        val bodyWeightKg = profile?.weightKg ?: 75f

        return MuscleGroup.values().map { group ->
            val groupLogs = logs.filter { it.muscleGroup == group && it.dateEpochDay >= sinceEpochDay }
            val byExercise = linkedMapOf<String, Pair<String, Float>>()

            baselines.filter { it.muscleGroup == group && it.maxWeightKg > 0f }.forEach { baseline ->
                val key = baseline.exerciseName.trim().lowercase()
                val prior = byExercise[key]
                if (prior == null || baseline.maxWeightKg > prior.second) {
                    byExercise[key] = baseline.exerciseName to baseline.maxWeightKg
                }
            }
            groupLogs.forEach { log ->
                if (log.weightKg <= 0f) return@forEach
                val key = log.exerciseName.trim().lowercase()
                val estimatedTwelveRepWeight = log.weightKg * (1f + min(log.reps, 20).coerceAtLeast(0) / 30f) / TWELVE_REP_FACTOR
                val prior = byExercise[key]
                if (prior == null || estimatedTwelveRepWeight > prior.second) {
                    byExercise[key] = log.exerciseName to estimatedTwelveRepWeight
                }
            }

            if (byExercise.isEmpty()) {
                return@map MuscleProgressResult(group, false, 0f, groupLogs.sumOf { it.sets }, 0f, 0f)
            }

            val comparisons = byExercise.values.map { (exerciseName, weight12) ->
                val benchmark12 = StrengthBenchmark.averageTwelveRepWeight(exerciseName, group, bodyWeightKg, age, gender)
                val ratio = (weight12 / benchmark12).coerceAtLeast(0.05f)
                val rating = (RATING_BASELINE + RATING_LOG2_SCALE * (ln(ratio) / ln(2f))).coerceIn(0f, 10f)
                Triple(rating, weight12, benchmark12)
            }

            MuscleProgressResult(
                group = group,
                hasEnoughData = true,
                rating = comparisons.map { it.first }.average().toFloat(),
                totalSets = groupLogs.sumOf { it.sets },
                averageTwelveRepWeightKg = comparisons.map { it.second }.average().toFloat(),
                averageTwelveRepBenchmarkKg = comparisons.map { it.third }.average().toFloat()
            )
        }
    }
}
