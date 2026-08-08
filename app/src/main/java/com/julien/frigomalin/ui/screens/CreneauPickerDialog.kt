package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.data.TypeRepas
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun CreneauPickerDialog(
    nomRecette: String,
    onDismiss: () -> Unit,
    onSelectionner: (date: Long, typeRepas: TypeRepas) -> Unit
) {
    val quinzaine = remember { joursQuinzaine() }
    val format = remember { SimpleDateFormat("EEEE d MMM", Locale.FRENCH) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter \"$nomRecette\" au planning") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(quinzaine) { jour ->
                    Text(
                        format.format(jour).replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        TextButton(onClick = { onSelectionner(jour, TypeRepas.MIDI) }) {
                            Text("Midi")
                        }
                        TextButton(onClick = { onSelectionner(jour, TypeRepas.SOIR) }) {
                            Text("Soir")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

private fun joursQuinzaine(): List<Long> {
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