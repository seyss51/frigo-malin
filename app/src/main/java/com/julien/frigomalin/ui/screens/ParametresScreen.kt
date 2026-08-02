package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ParametresScreen(
    onExporter: () -> Unit,
    onImporter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Sauvegarde", style = MaterialTheme.typography.titleLarge)
        Text(
            "Exporte ton stock, tes recettes et les photos dans un fichier, ou restaure une sauvegarde précédente.",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onExporter, modifier = Modifier.fillMaxWidth()) {
            Text("Exporter une sauvegarde")
        }
        OutlinedButton(onClick = onImporter, modifier = Modifier.fillMaxWidth()) {
            Text("Importer une sauvegarde")
        }
        Text(
            "L'import remplace toutes les données actuelles par celles du fichier choisi.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}