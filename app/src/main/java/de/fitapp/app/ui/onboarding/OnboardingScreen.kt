package de.fitapp.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.fitapp.app.data.entity.*
import de.fitapp.app.ui.AppViewModel
import de.fitapp.app.util.CalorieCalculator

private val stepTitles = listOf(
    "Willkommen", "Ziel", "Körperdaten", "Aktivität", "Ernährungsziele", "Trainingsstand", "Startwerte"
)

private data class BaselineDraft(val exerciseName: String = "", val weightKg: String = "")

private val baselineGroups = listOf(
    MuscleGroup.BRUST, MuscleGroup.RUECKEN, MuscleGroup.BEINE,
    MuscleGroup.SCHULTERN, MuscleGroup.ARME, MuscleGroup.BAUCH
)

private fun MuscleGroup.displayName() = when (this) {
    MuscleGroup.BRUST -> "Brust"
    MuscleGroup.RUECKEN -> "Rücken"
    MuscleGroup.BEINE -> "Beine"
    MuscleGroup.SCHULTERN -> "Schultern"
    MuscleGroup.ARME -> "Arme"
    MuscleGroup.BAUCH -> "Bauch"
}

private fun MuscleGroup.exerciseExamples() = when (this) {
    MuscleGroup.BRUST -> listOf("Bankdrücken", "Brustpresse")
    MuscleGroup.RUECKEN -> listOf("Rudern", "Latzug")
    MuscleGroup.BEINE -> listOf("Kniebeuge", "Beinpresse")
    MuscleGroup.SCHULTERN -> listOf("Schulterdrücken", "Seitheben")
    MuscleGroup.ARME -> listOf("Bizepscurls", "Trizepsdrücken")
    MuscleGroup.BAUCH -> listOf("Crunches", "Bauchmaschine")
}

@Composable
fun OnboardingScreen(viewModel: AppViewModel) {
    var step by remember { mutableIntStateOf(0) }

    var goal by remember { mutableStateOf(Goal.STAY) }
    var age by remember { mutableStateOf("30") }
    var heightCm by remember { mutableStateOf("175") }
    var weightKg by remember { mutableStateOf("75") }
    var targetWeightKg by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.DIVERS) }
    var activity by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var experience by remember { mutableStateOf(TrainingExperience.BEGINNER) }

    var calorieGoal by remember { mutableStateOf(2200) }
    var proteinGoal by remember { mutableStateOf(120) }
    var carbsGoal by remember { mutableStateOf(250) }
    var fatGoal by remember { mutableStateOf(70) }
    var fiberGoal by remember { mutableStateOf(30) }
    var sugarGoal by remember { mutableStateOf(50) }

    val baselineDrafts = remember {
        mutableStateMapOf<String, BaselineDraft>().apply {
            baselineGroups.forEach { group -> repeat(2) { slot -> put("${group.name}-$slot", BaselineDraft()) } }
        }
    }

    fun applyMacrosForCalories(newCalorieGoal: Int) {
        calorieGoal = newCalorieGoal
        val w = weightKg.toFloatOrNull() ?: 75f
        val h = heightCm.toIntOrNull() ?: 175
        val macros = CalorieCalculator.macrosForCalories(newCalorieGoal, w, h, goal, age.toIntOrNull() ?: 30)
        proteinGoal = macros.proteinG
        carbsGoal = macros.carbsG
        fatGoal = macros.fatG
        fiberGoal = (newCalorieGoal * 14.6 / 1000).toInt().coerceAtLeast(30)
        sugarGoal = (newCalorieGoal * 0.10 / 4).toInt()
    }

    fun applyEstimateIfPossible() {
        val a = age.toIntOrNull() ?: return
        val h = heightCm.toIntOrNull() ?: return
        val w = weightKg.toFloatOrNull() ?: return
        val estimate = viewModel.estimateCalories(a, h, w, gender, activity, goal)
        calorieGoal = estimate.goalKcal
        proteinGoal = estimate.proteinG
        carbsGoal = estimate.carbsG
        fatGoal = estimate.fatG
        fiberGoal = estimate.fiberG
        sugarGoal = estimate.sugarLimitG
    }

    fun finish() {
        val profile = UserProfile(
            goal = goal,
            age = age.toIntOrNull() ?: 30,
            heightCm = heightCm.toIntOrNull() ?: 175,
            weightKg = weightKg.toFloatOrNull() ?: 75f,
            targetWeightKg = targetWeightKg.toFloatOrNull(),
            gender = gender,
            activityLevel = activity,
            trainingExperience = experience,
            dailyCalorieGoal = calorieGoal,
            dailyProteinGoalG = proteinGoal,
            dailyCarbsGoalG = carbsGoal,
            dailyFatGoalG = fatGoal,
            dailyFiberGoalG = fiberGoal,
            dailySugarLimitG = sugarGoal,
            onboardingCompleted = true
        )
        viewModel.saveProfile(profile)
        val baselines = baselineGroups.flatMap { group ->
            (0..1).mapNotNull { slot ->
                val draft = baselineDrafts.getValue("${group.name}-$slot")
                val name = draft.exerciseName.trim()
                val weight = draft.weightKg.replace(',', '.').toFloatOrNull()
                if (name.isNotBlank() && weight != null && weight > 0f) {
                    ExerciseBaselineEntity(exerciseName = name, muscleGroup = group, maxWeightKg = weight)
                } else null
            }
        }
        viewModel.saveExerciseBaselines(baselines)

    }

    Scaffold(
        topBar = { LargeTopAppBar(title = { Text("Einrichtung – ${stepTitles[step]}", fontWeight = FontWeight.Bold) }) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { finish() }) {
                    Text(if (step == stepTitles.lastIndex) "Fertig" else "Überspringen")
                }
                Button(onClick = {
                    if (step == 1) applyEstimateIfPossible()
                    if (step < stepTitles.lastIndex) step++ else finish()
                }) {
                    Text(if (step == stepTitles.lastIndex) "Los geht's" else "Weiter")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                0 -> {
                    Text(
                        "Willkommen bei Fit & Ziele!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        "In wenigen Schritten richten wir deine App ein. Wir fragen nach deinem " +
                            "Ziel, Körperdaten und deine persönlichen Kraftwerte. Damit können wir " +
                            "passende Kalorien- und Trainingsvorschläge berechnen. Du kannst jeden " +
                        "Schritt überspringen und später im Profil ändern."
                    )
                }
                1 -> {
                    Text("Was ist dein Hauptziel?", style = MaterialTheme.typography.titleMedium)
                    GoalOption("Abnehmen", goal == Goal.LOSE_WEIGHT) { goal = Goal.LOSE_WEIGHT }
                    GoalOption("Gewicht halten", goal == Goal.STAY) { goal = Goal.STAY }
                    GoalOption("Muskeln aufbauen", goal == Goal.GAIN_MUSCLE) { goal = Goal.GAIN_MUSCLE }
                    GoalOption("Allgemein fitter werden", goal == Goal.GENERAL_FITNESS) { goal = Goal.GENERAL_FITNESS }
                }
                2 -> {
                    Text("Deine Körperdaten", style = MaterialTheme.typography.titleMedium)
                    Text("Diese Angaben helfen uns, Kalorien- und Nährstoffziele passend zu berechnen.")
                    OutlinedTextField(age, { age = it }, label = { Text("Alter (Jahre)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(heightCm, { heightCm = it }, label = { Text("Größe (cm)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(weightKg, { weightKg = it }, label = { Text("Aktuelles Gewicht (kg)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    if (goal == Goal.LOSE_WEIGHT || goal == Goal.GAIN_MUSCLE) {
                        OutlinedTextField(targetWeightKg, { targetWeightKg = it }, label = { Text("Zielgewicht (kg, optional)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    }
                    Text("Geschlecht (für die Berechnung des Grundumsatzes):")
                    GoalOption("Männlich", gender == Gender.MALE) { gender = Gender.MALE }
                    GoalOption("Weiblich", gender == Gender.FEMALE) { gender = Gender.FEMALE }
                    GoalOption("Divers", gender == Gender.DIVERS) { gender = Gender.DIVERS }
                }
                3 -> {
                    Text("Wie aktiv bist du im Alltag?", style = MaterialTheme.typography.titleMedium)
                    GoalOption("Überwiegend sitzend", activity == ActivityLevel.SEDENTARY) { activity = ActivityLevel.SEDENTARY }
                    GoalOption("Leicht aktiv (wenig Bewegung)", activity == ActivityLevel.LIGHT) { activity = ActivityLevel.LIGHT }
                    GoalOption("Moderat aktiv", activity == ActivityLevel.MODERATE) { activity = ActivityLevel.MODERATE }
                    GoalOption("Sehr aktiv", activity == ActivityLevel.ACTIVE) { activity = ActivityLevel.ACTIVE }
                    GoalOption("Extrem aktiv (körperliche Arbeit/Leistungssport)", activity == ActivityLevel.VERY_ACTIVE) { activity = ActivityLevel.VERY_ACTIVE }
                }
                4 -> {
                    LaunchedEffect(Unit) { applyEstimateIfPossible() }
                    Text("Deine Tagesziele", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Basierend auf deinen Angaben schlagen wir folgende Werte vor – ein " +
                            "Näherungswert, den du jederzeit anpassen kannst. Wenn du die Kalorien " +
                            "änderst, passen sich die Nährstoffziele automatisch an. Der Zuckerwert " +
                            "ist eine Orientierung; erfasst wird auch natürlicher Zucker."
                    )
                    NumberField("Kalorien (kcal)", calorieGoal) { applyMacrosForCalories(it) }
                    NumberField("Eiweiß (g)", proteinGoal) { proteinGoal = it }
                    NumberField("Kohlenhydrate (g)", carbsGoal) { carbsGoal = it }
                    NumberField("Fett (g)", fatGoal) { fatGoal = it }
                    NumberField("Ballaststoffe (g)", fiberGoal) { fiberGoal = it }
                    NumberField("Zucker-Orientierungswert (g)", sugarGoal) { sugarGoal = it }
                }
                5 -> {
                    Text("Trainingserfahrung", style = MaterialTheme.typography.titleMedium)
                    GoalOption("Anfänger:in", experience == TrainingExperience.BEGINNER) { experience = TrainingExperience.BEGINNER }
                    GoalOption("Fortgeschritten", experience == TrainingExperience.INTERMEDIATE) { experience = TrainingExperience.INTERMEDIATE }
                    GoalOption("Erfahren", experience == TrainingExperience.ADVANCED) { experience = TrainingExperience.ADVANCED }
                }
                6 -> {
                    Text("Deine Übungen und Maximalgewichte", style = MaterialTheme.typography.titleMedium)
                    Text("Trag für jede große Muskelgruppe bis zu zwei Übungen und das bewegte Gewicht ein. Die Bewertung vergleicht alle Werte einheitlich auf Basis von 12 Wiederholungen – Wiederholungszahlen musst du nicht angeben. Unbekannte oder nicht passende Übungen kannst du einzeln überspringen.")
                    baselineGroups.forEach { group ->
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(group.displayName(), style = MaterialTheme.typography.titleSmall)
                                repeat(2) { slot ->
                                    val key = "${group.name}-$slot"
                                    val draft = baselineDrafts.getValue(key)
                                    OutlinedTextField(
                                        value = draft.exerciseName,
                                        onValueChange = { baselineDrafts[key] = draft.copy(exerciseName = it) },
                                        label = { Text("Übung ${slot + 1}") },
                                        placeholder = { Text("z. B. ${group.exerciseExamples()[slot]}") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        OutlinedTextField(
                                            value = draft.weightKg,
                                            onValueChange = { baselineDrafts[key] = draft.copy(weightKg = it) },
                                            label = { Text("Gewicht (kg)") },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        TextButton(onClick = { baselineDrafts[key] = BaselineDraft() }) { Text("Überspringen") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun NumberField(label: String, value: Int, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { it.toIntOrNull()?.let(onChange) },
        label = { Text(label) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}
