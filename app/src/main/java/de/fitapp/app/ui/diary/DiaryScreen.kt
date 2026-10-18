package de.fitapp.app.ui.diary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fitapp.app.data.entity.DiaryEntryEntity
import de.fitapp.app.data.entity.FoodSource
import de.fitapp.app.data.entity.MealType
import de.fitapp.app.ui.AppViewModel

@Composable
fun DiaryScreen(viewModel: AppViewModel) {
    val profile by viewModel.profile.collectAsState()
    val entries by viewModel.todayEntries.collectAsState()
    var showAddDialog by remember { mutableStateOf<MealType?>(null) }
    var startWithScanner by remember { mutableStateOf(false) }

    val totals = remember(entries) {
        Totals(
            kcal = entries.sumOf { it.kcal.toDouble() }.toFloat(),
            protein = entries.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = entries.sumOf { it.carbs.toDouble() }.toFloat(),
            sugar = entries.sumOf { it.sugar.toDouble() }.toFloat(),
            fat = entries.sumOf { it.fat.toDouble() }.toFloat(),
            fiber = entries.sumOf { it.fiber.toDouble() }.toFloat()
        )
    }

    Scaffold(topBar = { TopAppBar(title = {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text("Ernährung", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Dein Tag im Überblick", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item { DailyBalanceCard(totals, profile?.dailyCalorieGoal ?: 2200, profile?.dailyProteinGoalG ?: 0, profile?.dailyCarbsGoalG ?: 0, profile?.dailySugarLimitG ?: 50, profile?.dailyFatGoalG ?: 0, profile?.dailyFiberGoalG ?: 30) }

            item {
                FilledTonalButton(
                    onClick = {
                        startWithScanner = true
                        showAddDialog = MealType.SNACK
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Barcode scannen", style = MaterialTheme.typography.titleSmall)
                }
            }

            items(MealType.values().toList()) { meal ->
                MealSection(
                    meal = meal,
                    entries = entries.filter { it.mealType == meal },
                    onAdd = { showAddDialog = meal },
                    onDelete = { viewModel.deleteDiaryEntry(it) }
                )
            }
        }
    }

    showAddDialog?.let { meal ->
        AddFoodDialog(
            viewModel = viewModel,
            mealType = meal,
            startInScanner = startWithScanner,
            onDismiss = { showAddDialog = null; startWithScanner = false }
        )
    }
}

private data class Totals(
    val kcal: Float, val protein: Float, val carbs: Float,
    val sugar: Float, val fat: Float, val fiber: Float
)

@Composable
private fun DailyBalanceCard(
    totals: Totals, kcalGoal: Int, proteinGoal: Int, carbsGoal: Int,
    sugarLimit: Int, fatGoal: Int, fiberGoal: Int
) {
    ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("HEUTE IM BLICK", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                Text("${totals.kcal.toInt()}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("von $kcalGoal kcal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            val calorieProgress = if (kcalGoal > 0) (totals.kcal / kcalGoal).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(progress = { calorieProgress }, modifier = Modifier.fillMaxWidth().height(6.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surface.copy(alpha = .55f))
            Text("${(kcalGoal - totals.kcal.toInt()).coerceAtLeast(0)} kcal übrig", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .18f))
            NutrientRow("Eiweiß", totals.protein, proteinGoal.toFloat(), "g")
            NutrientRow("Kohlenhydrate", totals.carbs, carbsGoal.toFloat(), "g")
            NutrientRow("Fett", totals.fat, fatGoal.toFloat(), "g")
            NutrientRow("Zucker inkl. natürlichem", totals.sugar, sugarLimit.toFloat(), "g", isLimit = true)
            NutrientRow("Ballaststoffe", totals.fiber, fiberGoal.toFloat(), "g")
        }
    }
}

@Composable
private fun NutrientRow(label: String, value: Float, goal: Float, unit: String, isLimit: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, maxLines = 1, softWrap = false)
            Text("${value.toInt()} / ${goal.toInt()} $unit", style = MaterialTheme.typography.bodySmall, maxLines = 1, softWrap = false)
        }
        val progress = if (goal > 0) (value / goal).coerceIn(0f, 1f) else 0f
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = if (isLimit && value > goal) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

private fun MealType.label() = when (this) {
    MealType.FRUEHSTUECK -> "Frühstück"
    MealType.MITTAGESSEN -> "Mittagessen"
    MealType.ABENDESSEN -> "Abendessen"
    MealType.SNACK -> "Snacks"
}

@Composable
private fun MealSection(
    meal: MealType,
    entries: List<DiaryEntryEntity>,
    onAdd: () -> Unit,
    onDelete: (DiaryEntryEntity) -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(meal.label(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = "Lebensmittel zu ${meal.label()} hinzufügen") }
            }
            if (entries.isEmpty()) {
                Text("Noch nichts eingetragen.", style = MaterialTheme.typography.bodySmall)
            } else {
                entries.forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${entry.foodName} (${entry.gramAmount.toInt()} g)")
                            Text(
                                "${entry.kcal.toInt()} kcal · ${qualityLabel(entry.valueQuality)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDelete(entry) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eintrag löschen")
                        }
                    }
                }
            }
        }
    }
}

private fun qualityLabel(source: FoodSource): String = when (source) {
    FoodSource.LOCAL_DB -> "aus Datenbank"
    FoodSource.BARCODE_ONLINE -> "aus Online-Produktdatenbank"
    FoodSource.MANUAL -> "manuell eingegeben"
    FoodSource.CUSTOM -> "eigener Eintrag"
}
