package de.fitapp.app.ui.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.fitapp.app.ui.AppViewModel
import de.fitapp.app.ui.common.StreakCalendarGrid
import de.fitapp.app.ui.common.StreakStat
import java.time.LocalDate

@Composable
fun ProgressScreen(viewModel: AppViewModel) {
    val streak by viewModel.streak.collectAsState()
    val weightEntries by viewModel.weightEntries.collectAsState()

    Scaffold(topBar = { LargeTopAppBar(title = { Text("Fortschritt", fontWeight = FontWeight.Bold) }) }) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Kalorienziel-Streak", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            StreakStat("Aktuell", streak.currentStreak)
                            StreakStat("Rekord", streak.longestStreak)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Letzte 28 Tage:", style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.height(4.dp))
                        StreakCalendarGrid(streak.achievedDays)
                        Text(
                            "Tage ohne Eintrag zählen nicht automatisch als erreicht.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Gewichtsverlauf", style = MaterialTheme.typography.titleMedium)
                        if (weightEntries.isEmpty()) {
                            Text("Noch keine Gewichtseinträge. Du kannst dein Gewicht im Profil aktualisieren.")
                        } else {
                            weightEntries.take(10).forEach { w ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(LocalDate.ofEpochDay(w.dateEpochDay).toString())
                                    Text("${w.weightKg} kg")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

