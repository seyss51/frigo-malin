package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.julien.frigomalin.data.PlanningJour
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.data.TypeRepas
import com.julien.frigomalin.util.PhotoStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PlanningScreen(
    planning: List<PlanningJour>,
    recettes: List<Recette>,
    onChoisirRecette: (date: Long, typeRepas: TypeRepas) -> Unit,
    onRetirer: (PlanningJour) -> Unit,
    modifier: Modifier = Modifier
) {
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
                onChoisir = { type -> onChoisirRecette(dateJour, type) },
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
                onChoisir = { type -> onChoisirRecette(dateJour, type) },
                onRetirer = onRetirer
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
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
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                formatJour.format(dateJour).replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            listOf(TypeRepas.MIDI to "Midi", TypeRepas.SOIR to "Soir").forEach { (type, label) ->
                val repasAssigne = planning.firstOrNull { it.date == dateJour && it.typeRepas == type }
                val recette = repasAssigne?.let { recettesParId[it.recetteId] }
                val fichierPhoto = recette?.let { PhotoStorage.fichierPhoto(context, it.photoPath) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (recette != null) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (fichierPhoto != null) {
                                    AsyncImage(
                                        model = fichierPhoto,
                                        contentDescription = recette.nom,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("$label : ${recette?.nom ?: "—"}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row {
                        TextButton(onClick = { onChoisir(type) }) {
                            Text(if (recette == null) "Choisir" else "Changer")
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