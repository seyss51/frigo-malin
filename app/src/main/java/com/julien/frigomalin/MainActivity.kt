package com.julien.frigomalin


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.julien.frigomalin.ui.screens.AjouterIngredientScreen
import com.julien.frigomalin.ui.screens.AjouterRecetteScreen
import com.julien.frigomalin.ui.screens.StockScreen
import com.julien.frigomalin.ui.screens.SuggestionsScreen
import com.julien.frigomalin.viewmodel.FrigoViewModel
import import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class Ecran { STOCK, SUGGESTIONS, AJOUT_INGREDIENT, AJOUT_RECETTE }

class MainActivity : ComponentActivity() {

    private val viewModel: FrigoViewModel by viewModels {
        val app = application as FrigoMalinApplication
        FrigoViewModel.Factory(app.ingredientRepository, app.recetteRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FrigoMalinApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun FrigoMalinApp(viewModel: FrigoViewModel) {
    var ecranActif by remember { mutableStateOf(Ecran.STOCK) }
    val stock by viewModel.stock.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()

    when (ecranActif) {
        Ecran.AJOUT_INGREDIENT -> {
            AjouterIngredientScreen(
                onRetour = { ecranActif = Ecran.STOCK },
                onEnregistrer = { viewModel.ajouterIngredient(it) }
            )
            return
        }
        Ecran.AJOUT_RECETTE -> {
            AjouterRecetteScreen(
                onRetour = { ecranActif = Ecran.SUGGESTIONS },
                onEnregistrer = { recette, ingredients -> viewModel.ajouterRecette(recette, ingredients) }
            )
            return
        }
        else -> Unit
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = ecranActif == Ecran.STOCK,
                    onClick = { ecranActif = Ecran.STOCK },
                    icon = { Icon(Icons.Default.Kitchen, contentDescription = "Stock") },
                    label = { Text("Stock") }
                )
                NavigationBarItem(
                    selected = ecranActif == Ecran.SUGGESTIONS,
                    onClick = { ecranActif = Ecran.SUGGESTIONS },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Suggestions") },
                    label = { Text("Suggestions") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    ecranActif = if (ecranActif == Ecran.STOCK) Ecran.AJOUT_INGREDIENT else Ecran.AJOUT_RECETTE
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter")
            }
        }
    ) { padding ->
        when (ecranActif) {
            Ecran.STOCK -> StockScreen(
                stock = stock,
                onSupprimer = { viewModel.supprimerIngredient(it) },
                modifier = Modifier.padding(padding)
            )
            Ecran.SUGGESTIONS -> SuggestionsScreen(
                suggestions = suggestions,
                modifier = Modifier.padding(padding)
            )
            else -> Unit
        }
    }
}