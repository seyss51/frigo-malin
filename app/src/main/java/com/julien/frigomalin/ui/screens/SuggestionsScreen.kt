package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.suggestion.SuggestionRecette

@Composable
fun SuggestionsScreen(
    suggestions: List<SuggestionRecette>,
    modifier: Modifier = Modifier
) {
    if (suggestions.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
            Text("Ajoute des recettes et du stock pour voir des suggestions.")
        }
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(suggestions, key = { it.recette.id }) { suggestion ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(suggestion.recette.nom, style = MaterialTheme.typography.titleMedium)
                        Text("${suggestion.pourcentageDisponible}%")
                    }
                    if (suggestion.ingredientsPerimantBientot.isNotEmpty()) {
                        Text(
                            "Utilise des ingrédients bientôt périmés : ${suggestion.ingredientsPerimantBientot.joinToString()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (suggestion.ingredientsManquants.isNotEmpty()) {
                        Text(
                            "Manque : ${suggestion.ingredientsManquants.joinToString()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}