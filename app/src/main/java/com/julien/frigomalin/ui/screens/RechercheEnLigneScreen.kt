package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.data.Ingredient
import java.net.URLEncoder

@Composable
fun RechercheEnLigneScreen(
    stock: List<Ingredient>,
    onLancerRecherche: (url: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ingredientsSelectionnes = remember { mutableStateListOf<String>() }
    var requeteLibre by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Idées de recettes en ligne", style = MaterialTheme.typography.titleLarge)
        Text(
            "Sélectionne des ingrédients de ton stock, ou tape une recherche libre, puis choisis où chercher.",
            style = MaterialTheme.typography.bodyMedium
        )

        if (stock.isNotEmpty()) {
            Text("Depuis ton stock", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.heightIn(max = 220.dp)) {
                items(stock, key = { it.id }) { ingredient ->
                    val selectionne = ingredientsSelectionnes.contains(ingredient.nom)
                    FilterChip(
                        selected = selectionne,
                        onClick = {
                            if (selectionne) ingredientsSelectionnes.remove(ingredient.nom)
                            else ingredientsSelectionnes.add(ingredient.nom)
                        },
                        label = { Text(ingredient.nom) },
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        OutlinedTextField(
            value = requeteLibre,
            onValueChange = { requeteLibre = it },
            label = { Text("Ou recherche libre (ex: poulet curry)") },
            modifier = Modifier.fillMaxWidth()
        )

        val requeteFinale = (ingredientsSelectionnes + listOfNotNull(requeteLibre.trim().takeIf { it.isNotBlank() }))
            .joinToString(" ")

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                val q = URLEncoder.encode(requeteFinale, "UTF-8")
                onLancerRecherche("https://www.marmiton.org/recettes/recherche.aspx?aqt=$q")
            },
            enabled = requeteFinale.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chercher sur Marmiton")
        }

        OutlinedButton(
            onClick = {
                val q = URLEncoder.encode(
                    "$requeteFinale recette site:marmiton.org OR site:jow.fr OR site:cuisineaz.com",
                    "UTF-8"
                )
                onLancerRecherche("https://www.google.com/search?q=$q")
            },
            enabled = requeteFinale.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Chercher sur Marmiton, Jow et autres")
        }
    }
}