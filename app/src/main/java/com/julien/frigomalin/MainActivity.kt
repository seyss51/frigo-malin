package com.julien.frigomalin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julien.frigomalin.data.Ingredient
import com.julien.frigomalin.ui.screens.AjouterIngredientScreen
import com.julien.frigomalin.ui.screens.AjouterRecetteScreen
import com.julien.frigomalin.ui.screens.PlanningScreen
import com.julien.frigomalin.ui.screens.RecetteDetailScreen
import com.julien.frigomalin.ui.screens.StockScreen
import com.julien.frigomalin.ui.screens.SuggestionsScreen
import com.julien.frigomalin.ui.theme.FrigoMalinTheme
import com.julien.frigomalin.viewmodel.FrigoViewModel

private enum class Ecran { STOCK, SUGGESTIONS, PLANNING, AJOUT_INGREDIENT, MODIF_INGREDIENT, AJOUT_RECETTE, DETAIL_RECETTE }

class MainActivity : ComponentActivity() {

    private val viewModel: FrigoViewModel by viewModels {
        val app = application as FrigoMalinApplication
        FrigoViewModel.Factory(app.ingredientRepository, app.recetteRepository, app.planningRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrigoMalinTheme {
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
    var ingredientEnEdition by remember { mutableStateOf<Ingredient?>(null) }

    val stock by viewModel.stock.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val recetteSelectionnee by viewModel.recetteSelectionnee.collectAsStateWithLifecycle()
    val portionsActuelles by viewModel.portionsActuelles.collectAsStateWithLifecycle()
    val listeCourses by viewModel.listeCourses.collectAsStateWithLifecycle()
    val toutesLesRecettes by viewModel.toutesLesRecettes.collectAsStateWithLifecycle()
    val semaine by viewModel.semaineActuelle.collectAsStateWithLifecycle()

    when (ecranActif) {
        Ecran.AJOUT_INGREDIENT -> {
            AjouterIngredientScreen(
                onRetour = { ecranActif = Ecran.STOCK },
                onEnregistrer = { viewModel.ajouterIngredient(it) }
            )
            return
        }
        Ecran.MODIF_INGREDIENT -> {
            AjouterIngredientScreen(
                ingredientExistant = ingredientEnEdition,
                onRetour = { ecranActif = Ecran.STOCK },
                onEnregistrer = { viewModel.modifierIngredient(it) }
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
        Ecran.DETAIL_RECETTE -> {
            RecetteDetailScreen(
                recette = recetteSelectionnee,
                portions = portionsActuelles,
                listeCourses = listeCourses,
                onPortionsChange = { viewModel.definirPortions(it) },
                onRetour = { ecranActif = Ecran.SUGGESTIONS }
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
                NavigationBarItem(
                    selected = ecranActif == Ecran.PLANNING,
                    onClick = { ecranActif = Ecran.PLANNING },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Planning") },
                    label = { Text("Planning") }
                )
            }
        },
        floatingActionButton = {
            if (ecranActif == Ecran.STOCK || ecranActif == Ecran.SUGGESTIONS) {
                FloatingActionButton(
                    onClick = {
                        ecranActif = if (ecranActif == Ecran.STOCK) Ecran.AJOUT_INGREDIENT else Ecran.AJOUT_RECETTE
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter")
                }
            }
        }
    ) { padding ->
        when (ecranActif) {
            Ecran.STOCK -> StockScreen(
                stock = stock,
                onSupprimer = { viewModel.supprimerIngredient(it) },
                onModifier = { ingredient ->
                    ingredientEnEdition = ingredient
                    ecranActif = Ecran.MODIF_INGREDIENT
                },
                modifier = Modifier.padding(padding)
            )
            Ecran.SUGGESTIONS -> SuggestionsScreen(
                suggestions = suggestions,
                onSelectionner = { id, portionsParDefaut ->
                    viewModel.selectionnerRecette(id, portionsParDefaut)
                    ecranActif = Ecran.DETAIL_RECETTE
                },
                modifier = Modifier.padding(padding)
            )
            Ecran.PLANNING -> PlanningScreen(
                planning = semaine,
                recettes = toutesLesRecettes,
                onAssigner = { date, type, recetteId -> viewModel.assignerRepas(date, type, recetteId) },
                onRetirer = { viewModel.retirerRepas(it) },
                modifier = Modifier.padding(padding)
            )
            else -> Unit
        }
    }
}