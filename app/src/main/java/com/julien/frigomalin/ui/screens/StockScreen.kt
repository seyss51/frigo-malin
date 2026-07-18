package com.julien.frigomalin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.julien.frigomalin.data.Ingredient

@Composable
fun StockScreen(
    stock: List<Ingredient>,
    onSupprimer: (Ingredient) -> Unit,
    modifier: Modifier = Modifier
) {
    if (stock.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Companion.Center) {
            Text("Aucun ingrédient en stock pour le moment.")
        }
        return
    }

    LazyColumn(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(stock, key = { it.id }) { ingredient ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Companion.CenterVertically
                ) {
                    Column {
                        Text(ingredient.nom, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${ingredient.quantite} ${ingredient.unite}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    IconButton(onClick = { onSupprimer(ingredient) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer")
                    }
                }
            }
        }
    }
}