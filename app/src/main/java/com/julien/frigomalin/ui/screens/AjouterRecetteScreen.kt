package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.data.RecetteIngredient

private data class LigneIngredient(
    val nom: String = "",
    val quantite: String = "",
    val unite: String = "g"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjouterRecetteScreen(
    onRetour: () -> Unit,
    onEnregistrer: (Recette, List<RecetteIngredient>) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var temps by remember { mutableStateOf("") }
    var portions by remember { mutableStateOf("4") }
    var lignesIngredients by remember { mutableStateOf(listOf(LigneIngredient())) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter une recette") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = nom,
                onValueChange = { nom = it },
                label = { Text("Nom de la recette") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = temps,
                    onValueChange = { temps = it },
                    label = { Text("Temps (min)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = portions,
                    onValueChange = { portions = it },
                    label = { Text("Portions") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = instructions,
                onValueChange = { instructions = it },
                label = { Text("Instructions") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Ingrédients nécessaires", style = MaterialTheme.typography.titleMedium)

            lignesIngredients.forEachIndexed { index, ligne ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = ligne.nom,
                        onValueChange = { nouveau ->
                            lignesIngredients = lignesIngredients.toMutableList().also {
                                it[index] = ligne.copy(nom = nouveau)
                            }
                        },
                        label = { Text("Ingrédient") },
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = ligne.quantite,
                        onValueChange = { nouveau ->
                            lignesIngredients = lignesIngredients.toMutableList().also {
                                it[index] = ligne.copy(quantite = nouveau)
                            }
                        },
                        label = { Text("Qté") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            lignesIngredients = lignesIngredients.toMutableList().also {
                                it.removeAt(index)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Retirer")
                    }
                }
            }

            OutlinedButton(
                onClick = { lignesIngredients = lignesIngredients + LigneIngredient() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Ajouter un ingrédient")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (nom.isBlank()) return@Button
                    val tempsValeur = temps.toIntOrNull() ?: 0
                    val portionsValeur = portions.toIntOrNull() ?: 4

                    val ingredientsValides = lignesIngredients.mapNotNull { ligne ->
                        val quantiteValeur = ligne.quantite.toDoubleOrNull()
                        if (ligne.nom.isNotBlank() && quantiteValeur != null) {
                            RecetteIngredient(
                                recetteId = 0, // remplacé par le repository lors de l'insertion
                                nomIngredient = ligne.nom.trim(),
                                quantiteNecessaire = quantiteValeur,
                                unite = ligne.unite
                            )
                        } else null
                    }

                    onEnregistrer(
                        Recette(
                            nom = nom.trim(),
                            instructions = instructions.trim(),
                            tempsPreparationMinutes = tempsValeur,
                            portions = portionsValeur,
                            estPersonnalisee = true
                        ),
                        ingredientsValides
                    )
                    onRetour()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer la recette")
            }
        }
    }
}