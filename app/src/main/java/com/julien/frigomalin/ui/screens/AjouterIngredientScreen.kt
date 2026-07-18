package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.data.Ingredient

private const val MILLIS_PAR_JOUR = 24 * 60 * 60 * 1000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjouterIngredientScreen(
    onRetour: () -> Unit,
    onEnregistrer: (Ingredient) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var quantite by remember { mutableStateOf("") }
    var unite by remember { mutableStateOf("g") }
    var joursAvantPeremption by remember { mutableStateOf("") }
    var uniteMenuOuvert by remember { mutableStateOf(false) }

    val unitesDisponibles = listOf("g", "kg", "ml", "L", "unité")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajouter un ingrédient") },
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
                label = { Text("Nom de l'ingrédient") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = quantite,
                onValueChange = { quantite = it },
                label = { Text("Quantité") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = uniteMenuOuvert,
                onExpandedChange = { uniteMenuOuvert = it }
            ) {
                OutlinedTextField(
                    value = unite,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Unité") },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = uniteMenuOuvert,
                    onDismissRequest = { uniteMenuOuvert = false }
                ) {
                    unitesDisponibles.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                unite = option
                                uniteMenuOuvert = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = joursAvantPeremption,
                onValueChange = { joursAvantPeremption = it },
                label = { Text("Jours avant péremption (optionnel)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val quantiteValeur = quantite.toDoubleOrNull() ?: return@Button
                    if (nom.isBlank()) return@Button

                    val datePeremption = joursAvantPeremption.toIntOrNull()?.let { jours ->
                        System.currentTimeMillis() + jours * MILLIS_PAR_JOUR
                    }

                    onEnregistrer(
                        Ingredient(
                            nom = nom.trim(),
                            quantite = quantiteValeur,
                            unite = unite,
                            datePeremption = datePeremption
                        )
                    )
                    onRetour()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enregistrer")
            }
        }
    }
}