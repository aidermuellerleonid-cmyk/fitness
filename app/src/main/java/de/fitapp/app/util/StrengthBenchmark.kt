package de.fitapp.app.util

import de.fitapp.app.data.entity.Gender
import de.fitapp.app.data.entity.MuscleGroup

/**
 * Grobe Schätzung eines "Altersdurchschnitts" für das 1-Wiederholungs-Maximum je Muskelgruppe.
 * Das ist bewusst eine einfache Näherung (keine wissenschaftliche Referenz), die aber
 * plausible, nachvollziehbare Vergleichswerte liefert: Körpergewicht, Alter und Geschlecht
 * fließen mit ein, damit z. B. ein 90-kg-Latzug realistisch gegen "Durchschnitt für diese
 * Person" bewertet wird statt gegen einen starren Fixwert.
 */
object StrengthBenchmark {

    // Referenzwert: geschätztes 1RM (kg) einer 75 kg schweren, 30-jährigen, durchschnittlich
    // trainierten Person je Muskelgruppe (typische Übung in Klammern).
    private val referenceOneRmAt75kgAge30: Map<MuscleGroup, Float> = mapOf(
        MuscleGroup.BRUST to 55f,      // Bankdrücken
        MuscleGroup.RUECKEN to 60f,    // Latzug / Rudern
        MuscleGroup.BEINE to 85f,      // Beinpresse / Kniebeuge
        MuscleGroup.SCHULTERN to 38f,  // Schulterdrücken
        MuscleGroup.ARME to 32f,       // Bizeps-/Trizepsübungen
        MuscleGroup.BAUCH to 28f       // Kabelzug-Crunch mit Zusatzgewicht
    )

    /**
     * Geschätztes durchschnittliches 1-Wiederholungs-Maximum (kg) für die gegebene Muskelgruppe,
     * angepasst an Körpergewicht, Alter und Geschlecht der Person.
     */
    fun averageOneRepMax(group: MuscleGroup, bodyWeightKg: Float, age: Int, gender: Gender): Float {
        val reference = referenceOneRmAt75kgAge30[group] ?: 45f

        return adjustedAverage(reference, bodyWeightKg, age, gender)
    }

    /** Altersgerechter grober 12-Wiederholungs-Vergleichswert für eine konkrete Übung. */
    fun averageTwelveRepWeight(exerciseName: String, group: MuscleGroup, bodyWeightKg: Float, age: Int, gender: Gender): Float {
        val name = exerciseName.lowercase().replace("ä", "ae").replace("ö", "oe").replace("ü", "ue")
        val referenceOneRm = when (group) {
            MuscleGroup.BRUST -> when {
                "flieg" in name || "fly" in name -> 22f
                "brustpresse" in name || "chest press" in name -> 65f
                "schraeg" in name || "incline" in name -> 48f
                else -> 55f
            }
            MuscleGroup.RUECKEN -> when {
                "kreuzheben" in name || "deadlift" in name -> 100f
                "latzug" in name || "pulldown" in name -> 55f
                else -> 60f
            }
            MuscleGroup.BEINE -> when {
                "beinpresse" in name || "leg press" in name -> 130f
                "beinstreck" in name || "extension" in name -> 50f
                "beinbeug" in name || "leg curl" in name -> 45f
                "kniebeug" in name || "squat" in name -> 85f
                "ausfall" in name || "lunge" in name -> 65f
                else -> 85f
            }
            MuscleGroup.SCHULTERN -> when {
                "seitheben" in name || "lateral raise" in name -> 15f
                "frontheben" in name || "front raise" in name -> 12f
                else -> 38f
            }
            MuscleGroup.ARME -> when {
                "trizeps" in name || "pushdown" in name || "push down" in name -> 35f
                "bizeps" in name || "curl" in name -> 22f
                else -> 28f
            }
            MuscleGroup.BAUCH -> when {
                "maschine" in name || "machine" in name -> 55f
                else -> 30f
            }
        }
        return adjustedAverage(referenceOneRm, bodyWeightKg, age, gender) / 1.4f
    }

    private fun adjustedAverage(reference: Float, bodyWeightKg: Float, age: Int, gender: Gender): Float {

        // Kraft skaliert grob (aber nicht 1:1) mit dem Körpergewicht.
        val weightFactor = (bodyWeightKg / 75f).coerceIn(0.55f, 1.8f)

        // Durchschnittliche Kraft steigt bis Mitte 20 leicht an und nimmt danach langsam ab.
        val ageFactor = when {
            age < 18 -> 0.75f
            age <= 26 -> 0.9f + (age - 18) * 0.0125f
            age <= 32 -> 1.0f
            else -> (1f - (age - 32) * 0.007f).coerceAtLeast(0.5f)
        }

        // Grobe Näherung für den durchschnittlichen Kraftunterschied zwischen den Geschlechtern.
        val genderFactor = when (gender) {
            Gender.FEMALE -> 0.68f
            Gender.MALE -> 1.0f
            Gender.DIVERS -> 0.85f
        }

        return (reference * weightFactor * ageFactor * genderFactor).coerceAtLeast(5f)
    }
}
