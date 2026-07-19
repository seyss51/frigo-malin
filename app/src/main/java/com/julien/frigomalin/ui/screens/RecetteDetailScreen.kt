package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.data.RecetteAvecIngredients
import com.julien.frigomalin.suggestion.ArticleCourse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetteDetailScreen(
    recette: RecetteAvecIngredients?,
    listeCourses: List<ArticleCourse>,
    onRetour: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recette?.recette?.nom ?: "Recette") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        if (recette == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Recette introuvable", modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${recette.recette.tempsPreparationMinutes} min · ${recette.recette.portions} portions",
                style = MaterialTheme.typography.bodyMedium
            )

            Text("Ingrédients", style = MaterialTheme.typography.titleMedium)
            recette.ingredients.forEach { ing ->
                Text("• ${ing.quantiteNecessaire} ${ing.unite} ${ing.nomIngredient}")
            }

            Text("Instructions", style = MaterialTheme.typography.titleMedium)
            Text(recette.recette.instructions)

            if (listeCourses.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Liste de courses (ce qui manque)", style = MaterialTheme.typography.titleMedium)
                listeCourses.forEach { article ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(
                            "${article.nom} — ${article.quantiteManquante} ${article.unite}",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            } else {
                Text(
                    "Tu as tout ce qu'il faut en stock \uD83C\uDF89",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}