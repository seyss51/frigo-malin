package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.util.PhotoStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecettePickerScreen(
    recettes: List<Recette>,
    titre: String,
    onRetour: () -> Unit,
    onChoisir: (Recette) -> Unit
) {
    val context = LocalContext.current
    var recherche by remember { mutableStateOf("") }

    val recettesFiltrees = remember(recettes, recherche) {
        if (recherche.isBlank()) recettes
        else recettes.filter { it.nom.contains(recherche, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titre) },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = recherche,
                onValueChange = { recherche = it },
                label = { Text("Rechercher une recette") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (recettesFiltrees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aucune recette trouvée.")
                }
                return@Scaffold
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                items(recettesFiltrees, key = { it.id }) { recette ->
                    val fichierPhoto = PhotoStorage.fichierPhoto(context, recette.photoPath)

                    Card(
                        onClick = { onChoisir(recette) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
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
                                        modifier = Modifier.size(26.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(recette.nom, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${recette.tempsPreparationMinutes} min · ${recette.portions} pers.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}