package de.fitapp.app.ui.profile

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

@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    val profile by viewModel.profile.collectAsState()
    val current = profile ?: return

    var age by remember(current) { mutableStateOf(current.age.toString()) }
    var heightCm by remember(current) { mutableStateOf(current.heightCm.toString()) }
    var weightKg by remember(current) { mutableStateOf(current.weightKg.toString()) }
    var targetWeightKg by remember(current) { mutableStateOf(current.targetWeightKg?.toString() ?: "") }
    var goal by remember(current) { mutableStateOf(current.goal) }
    var gender by remember(current) { mutableStateOf(current.gender) }
    var activity by remember(current) { mutableStateOf(current.activityLevel) }
    var calorieGoal by remember(current) { mutableStateOf(current.dailyCalorieGoal.toString()) }
    var proteinGoal by remember(current) { mutableStateOf(current.dailyProteinGoalG.toString()) }
    var carbsGoal by remember(current) { mutableStateOf(current.dailyCarbsGoalG.toString()) }
    var sugarLimit by remember(current) { mutableStateOf(current.dailySugarLimitG.toString()) }
    var fatGoal by remember(current) { mutableStateOf(current.dailyFatGoalG.toString()) }
    var fiberGoal by remember(current) { mutableStateOf(current.dailyFiberGoalG.toString()) }

    var newWeight by remember { mutableStateOf("") }

    fun save() {
        val savedAge = age.toIntOrNull() ?: current.age
        val savedHeight = heightCm.toIntOrNull() ?: current.heightCm
        val savedWeight = weightKg.toFloatOrNull() ?: current.weightKg
        val savedCalories = calorieGoal.toIntOrNull() ?: current.dailyCalorieGoal
        val updatedMacros = CalorieCalculator.macrosForCalories(savedCalories, savedWeight, savedHeight, goal, savedAge)
        val bodyOrGoalChanged = savedAge != current.age || savedHeight != current.heightCm || savedWeight != current.weightKg || goal != current.goal
        viewModel.saveProfile(
            current.copy(
                age = savedAge,
                heightCm = savedHeight,
                weightKg = savedWeight,
                targetWeightKg = targetWeightKg.toFloatOrNull(),
                goal = goal,
                gender = gender,
                activityLevel = activity,
                dailyCalorieGoal = savedCalories,
                dailyProteinGoalG = if (bodyOrGoalChanged) updatedMacros.proteinG else proteinGoal.toIntOrNull() ?: updatedMacros.proteinG,
                dailyCarbsGoalG = if (bodyOrGoalChanged) updatedMacros.carbsG else carbsGoal.toIntOrNull() ?: updatedMacros.carbsG,
                dailySugarLimitG = if (bodyOrGoalChanged) (savedCalories * 0.10 / 4).toInt() else sugarLimit.toIntOrNull() ?: current.dailySugarLimitG,
                dailyFatGoalG = if (bodyOrGoalChanged) updatedMacros.fatG else fatGoal.toIntOrNull() ?: updatedMacros.fatG,
                dailyFiberGoalG = if (bodyOrGoalChanged) (savedCalories * 14.6 / 1000).toInt().coerceAtLeast(30) else fiberGoal.toIntOrNull() ?: (savedCalories * 14.6 / 1000).toInt().coerceAtLeast(30)
            )
        )
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Profil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ziel", style = MaterialTheme.typography.titleMedium)
            SegmentedGoal(goal) { goal = it }

            Text("Körperdaten", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(age, { age = it }, label = { Text("Alter") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(heightCm, { heightCm = it }, label = { Text("Größe (cm)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(weightKg, { weightKg = it }, label = { Text("Aktuelles Gewicht (kg)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(targetWeightKg, { targetWeightKg = it }, label = { Text("Zielgewicht (kg, optional)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

            Text("Neues Gewicht protokollieren", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(newWeight, { newWeight = it }, label = { Text("kg") }, modifier = Modifier.weight(1f), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Button(onClick = {
                    newWeight.toFloatOrNull()?.let {
                        viewModel.addWeightEntry(it)
                        weightKg = it.toString()
                        newWeight = ""
                    }
                }) { Text("Eintragen") }
            }

            Text("Aktivitätslevel", style = MaterialTheme.typography.titleMedium)
            ActivityLevel.values().forEach { level ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = activity == level, onClick = { activity = level })
                    Text(activityLabel(level))
                }
            }

            Text("Tagesziele", style = MaterialTheme.typography.titleMedium)
            Text(
                "Wenn du die Kalorien änderst, werden Eiweiß/Kohlenhydrate/Fett automatisch " +
                    "passend neu berechnet. Der Zuckerwert ist ein Orientierungswert; das Tagebuch zählt auch natürlichen Zucker.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = calorieGoal,
                onValueChange = { text ->
                    calorieGoal = text
                    text.toIntOrNull()?.let { newGoal ->
                        val w = weightKg.toFloatOrNull() ?: current.weightKg
                        val macros = CalorieCalculator.macrosForCalories(newGoal, w, heightCm.toIntOrNull() ?: current.heightCm, goal, age.toIntOrNull() ?: current.age)
                        proteinGoal = macros.proteinG.toString()
                        carbsGoal = macros.carbsG.toString()
                        fatGoal = macros.fatG.toString()
                        fiberGoal = (newGoal * 14.6 / 1000).toInt().coerceAtLeast(30).toString()
                        sugarLimit = (newGoal * 0.10 / 4).toInt().toString()
                    }
                },
                label = { Text("Kalorien (kcal)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(proteinGoal, { proteinGoal = it }, label = { Text("Eiweiß (g)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(carbsGoal, { carbsGoal = it }, label = { Text("Kohlenhydrate (g)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(sugarLimit, { sugarLimit = it }, label = { Text("Zucker-Orientierungswert (g)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(fatGoal, { fatGoal = it }, label = { Text("Fett (g)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(fiberGoal, { fiberGoal = it }, label = { Text("Ballaststoffe (g)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

            Button(onClick = { save() }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Text("Änderungen speichern")
            }
        }
    }
}

@Composable
private fun SegmentedGoal(current: Goal, onChange: (Goal) -> Unit) {
    Column {
        Goal.values().forEach { g ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                RadioButton(selected = current == g, onClick = { onChange(g) })
                Text(goalLabel(g))
            }
        }
    }
}

private fun goalLabel(g: Goal) = when (g) {
    Goal.LOSE_WEIGHT -> "Abnehmen"
    Goal.STAY -> "Gewicht halten"
    Goal.GAIN_MUSCLE -> "Muskeln aufbauen"
    Goal.GENERAL_FITNESS -> "Allgemein fitter werden"
}

private fun activityLabel(a: ActivityLevel) = when (a) {
    ActivityLevel.SEDENTARY -> "Überwiegend sitzend"
    ActivityLevel.LIGHT -> "Leicht aktiv"
    ActivityLevel.MODERATE -> "Moderat aktiv"
    ActivityLevel.ACTIVE -> "Sehr aktiv"
    ActivityLevel.VERY_ACTIVE -> "Extrem aktiv"
}
