package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.julien.frigomalin.data.RecetteAvecIngredients
import com.julien.frigomalin.suggestion.ArticleCourse
import com.julien.frigomalin.util.PhotoStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetteDetailScreen(
    recette: RecetteAvecIngredients?,
    portions: Int,
    listeCourses: List<ArticleCourse>,
    onPortionsChange: (Int) -> Unit,
    onModifier: () -> Unit,
    onRetour: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recette?.recette?.nom ?: "Recette") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (recette != null) {
                        IconButton(onClick = onModifier) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
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

        val base = recette.recette.portions.coerceAtLeast(1)
        val facteur = portions.toDouble() / base
        val fichierPhoto = PhotoStorage.fichierPhoto(context, recette.recette.photoPath)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (fichierPhoto != null) {
                AsyncImage(
                    model = fichierPhoto,
                    contentDescription = "Photo du plat",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Text(
                "${recette.recette.tempsPreparationMinutes} min",
                style = MaterialTheme.typography.bodyMedium
            )

            Card {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nombre de personnes", style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onPortionsChange(portions - 1) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Moins")
                        }
                        Text(
                            "$portions",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { onPortionsChange(portions + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "Plus")
                        }
                    }
                }
            }

            Text("Ingrédients", style = MaterialTheme.typography.titleMedium)
            recette.ingredients.forEach { ing ->
                val quantiteAjustee = arrondir(ing.quantiteNecessaire * facteur)
                Text("• $quantiteAjustee ${ing.unite} ${ing.nomIngredient}")
            }

            Text("Instructions", style = MaterialTheme.typography.titleMedium)
            Text(recette.recette.instructions)

            if (listeCourses.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Liste de courses (ce qui manque)", style = MaterialTheme.typography.titleMedium)
                listeCourses.forEach { article ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(
                            "${article.nom} — ${arrondir(article.quantiteManquante)} ${article.unite}",
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

private fun arrondir(valeur: Double): String {
    return if (valeur == valeur.toInt().toDouble()) {
        valeur.toInt().toString()
    } else {
        "%.1f".format(valeur)
    }
}