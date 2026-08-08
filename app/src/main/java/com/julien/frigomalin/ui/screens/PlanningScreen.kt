package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.data.PlanningJour
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.data.TypeRepas
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class CreneauSelection(val date: Long, val typeRepas: TypeRepas)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    planning: List<PlanningJour>,
    recettes: List<Recette>,
    onAssigner: (date: Long, typeRepas: TypeRepas, recetteId: Long) -> Unit,
    onRetirer: (PlanningJour) -> Unit,
    modifier: Modifier = Modifier
) {
    var creneauSelectionne by remember { mutableStateOf<CreneauSelection?>(null) }
    val recettesParId = remember(recettes) { recettes.associateBy { it.id } }
    val quinzaine = remember { joursDeLaQuinzaineCourante() }
    val formatJour = remember { SimpleDateFormat("EEEE d MMM", Locale.FRENCH) }
    val formatEnTete = remember { SimpleDateFormat("d MMM", Locale.FRENCH) }

    val semaine1 = quinzaine.take(7)
    val semaine2 = quinzaine.drop(7)

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        item {
            Text(
                "Semaine du ${formatEnTete.format(semaine1.first())}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        items(semaine1) { dateJour ->
            CarteJour(
                dateJour = dateJour,
                planning = planning,
                recettesParId = recettesParId,
                formatJour = formatJour,
                onChoisir = { type -> creneauSelectionne = CreneauSelection(dateJour, type) },
                onRetirer = onRetirer
            )
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text(
                "Semaine du ${formatEnTete.format(semaine2.first())}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        items(semaine2) { dateJour ->
            CarteJour(
                dateJour = dateJour,
                planning = planning,
                recettesParId = recettesParId,
                formatJour = formatJour,
                onChoisir = { type -> creneauSelectionne = CreneauSelection(dateJour, type) },
                onRetirer = onRetirer
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    creneauSelectionne?.let { creneau ->
        AlertDialog(
            onDismissRequest = { creneauSelectionne = null },
            title = { Text("Choisir une recette") },
            text = {
                if (recettes.isEmpty()) {
                    Text("Aucune recette disponible. Ajoutes-en une d'abord.")
                } else {
                    LazyColumn {
                        items(recettes, key = { it.id }) { recette ->
                            TextButton(
                                onClick = {
                                    onAssigner(creneau.date, creneau.typeRepas, recette.id)
                                    creneauSelectionne = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(recette.nom, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { creneauSelectionne = null }) {
                    Text("Fermer")
                }
            }
        )
    }
}

@Composable
private fun CarteJour(
    dateJour: Long,
    planning: List<PlanningJour>,
    recettesParId: Map<Long, Recette>,
    formatJour: SimpleDateFormat,
    onChoisir: (TypeRepas) -> Unit,
    onRetirer: (PlanningJour) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                formatJour.format(dateJour).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            listOf(TypeRepas.MIDI to "Midi", TypeRepas.SOIR to "Soir").forEach { (type, label) ->
                val repasAssigne = planning.firstOrNull { it.date == dateJour && it.typeRepas == type }
                val nomRecette = repasAssigne?.let { recettesParId[it.recetteId]?.nom }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$label : ${nomRecette ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                    Row {
                        TextButton(onClick = { onChoisir(type) }) {
                            Text(if (nomRecette == null) "Choisir" else "Changer")
                        }
                        if (repasAssigne != null) {
                            TextButton(onClick = { onRetirer(repasAssigne) }) {
                                Text("Retirer")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun joursDeLaQuinzaineCourante(): List<Long> {
    val cal = Calendar.getInstance()
    cal.firstDayOfWeek = Calendar.MONDAY
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    return (0 until 14).map {
        val jour = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        jour
    }
}