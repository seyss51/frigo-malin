package com.julien.frigomalin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.julien.frigomalin.data.BackupManager
import com.julien.frigomalin.data.Ingredient
import com.julien.frigomalin.data.TypeRepas
import com.julien.frigomalin.ui.screens.AjouterIngredientScreen
import com.julien.frigomalin.ui.screens.AjouterRecetteScreen
import com.julien.frigomalin.ui.screens.CreneauPickerDialog
import com.julien.frigomalin.ui.screens.ParametresScreen
import com.julien.frigomalin.ui.screens.PlanningScreen
import com.julien.frigomalin.ui.screens.RecetteDetailScreen
import com.julien.frigomalin.ui.screens.RecettePickerScreen
import com.julien.frigomalin.ui.screens.RecetteWebViewScreen
import com.julien.frigomalin.ui.screens.RechercheEnLigneScreen
import com.julien.frigomalin.ui.screens.StockScreen
import com.julien.frigomalin.ui.screens.SuggestionsScreen
import com.julien.frigomalin.ui.theme.FrigoMalinTheme
import com.julien.frigomalin.util.RecetteExtraite
import com.julien.frigomalin.viewmodel.FrigoViewModel
import kotlinx.coroutines.launch

private enum class Ecran {
    STOCK, SUGGESTIONS, PLANNING, RECHERCHE_EN_LIGNE, PARAMETRES,
    AJOUT_INGREDIENT, MODIF_INGREDIENT,
    AJOUT_RECETTE, MODIF_RECETTE, DETAIL_RECETTE,
    WEBVIEW_RECETTE, CHOIX_RECETTE_PLANNING
}

private data class CreneauCible(val date: Long, val typeRepas: TypeRepas)

class MainActivity : ComponentActivity() {

    private val viewModel: FrigoViewModel by viewModels {
        val app = application as FrigoMalinApplication
        FrigoViewModel.Factory(app.authRepository, app.ingredientRepository, app.recetteRepository, app.planningRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrigoMalinTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var erreurFatale by remember { mutableStateOf<String?>(null) }

                    if (erreurFatale != null) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Erreur au démarrage", style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(erreurFatale ?: "")
                        }
                    } else {
                        Box {
                            runCatching {
                                FrigoMalinApp(viewModel, onRedemarrer = { recreate() })
                            }.onFailure { e ->
                                erreurFatale = "${e::class.simpleName}: ${e.message}"
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FrigoMalinApp(viewModel: FrigoViewModel, onRedemarrer: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ecranActif by remember { mutableStateOf(Ecran.STOCK) }
    var ingredientEnEdition by remember { mutableStateOf<Ingredient?>(null) }
    var urlWebViewActuelle by remember { mutableStateOf("") }
    var extractionEnAttente by remember { mutableStateOf<RecetteExtraite?>(null) }
    var creneauCible by remember { mutableStateOf<CreneauCible?>(null) }
    var dialogAjoutPlanningOuvert by remember { mutableStateOf(false) }

    val stock by viewModel.stock.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()
    val recetteSelectionnee by viewModel.recetteSelectionnee.collectAsStateWithLifecycle()
    val portionsActuelles by viewModel.portionsActuelles.collectAsStateWithLifecycle()
    val listeCourses by viewModel.listeCourses.collectAsStateWithLifecycle()
    val toutesLesRecettes by viewModel.toutesLesRecettes.collectAsStateWithLifecycle()
    val quinzaine by viewModel.quinzaineActuelle.collectAsStateWithLifecycle()
val estConnecte by viewModel.estConnecte.collectAsStateWithLifecycle()
    var erreurConnexion by remember { mutableStateOf<String?>(null) }

    if (!estConnecte) {
        com.julien.frigomalin.ui.screens.LoginScreen(
            onConnexion = { email, motDePasse ->
                erreurConnexion = null
                viewModel.seConnecter(email, motDePasse) { erreurConnexion = it }
            },
            messageErreur = erreurConnexion
        )
        return
    }
    val selecteurImportZip = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val succes = BackupManager.importerSauvegarde(context, uri)
                if (succes) onRedemarrer()
            }
        }
    }

    fun exporterEtPartager() {
        scope.launch {
            val fichier = BackupManager.exporterSauvegarde(context)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fichier)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Exporter la sauvegarde"))
        }
    }

    // Dialogue "Ajouter au planning" depuis le détail d'une recette
    if (dialogAjoutPlanningOuvert && recetteSelectionnee != null) {
        CreneauPickerDialog(
            nomRecette = recetteSelectionnee!!.recette.nom,
            onDismiss = { dialogAjoutPlanningOuvert = false },
            onSelectionner = { date, type ->
                viewModel.assignerRepas(date, type, recetteSelectionnee!!.recette.id)
                dialogAjoutPlanningOuvert = false
            }
        )
    }

    // Écrans en plein écran (sans barre de navigation du bas)
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
                extraction = extractionEnAttente,
                onRetour = {
                    extractionEnAttente = null
                    ecranActif = Ecran.SUGGESTIONS
                },
                onEnregistrer = { recette, ingredients -> viewModel.enregistrerRecette(recette, ingredients) }
            )
            return
        }
        Ecran.MODIF_RECETTE -> {
            AjouterRecetteScreen(
                recetteExistante = recetteSelectionnee,
                onRetour = { ecranActif = Ecran.DETAIL_RECETTE },
                onEnregistrer = { recette, ingredients -> viewModel.enregistrerRecette(recette, ingredients) }
            )
            return
        }
        Ecran.DETAIL_RECETTE -> {
            RecetteDetailScreen(
                recette = recetteSelectionnee,
                portions = portionsActuelles,
                listeCourses = listeCourses,
                onPortionsChange = { viewModel.definirPortions(it) },
                onModifier = { ecranActif = Ecran.MODIF_RECETTE },
                onAjouterAuPlanning = { dialogAjoutPlanningOuvert = true },
                onVoirSource = { url ->
                    urlWebViewActuelle = url
                    ecranActif = Ecran.WEBVIEW_RECETTE
                },
                onRetour = { ecranActif = Ecran.SUGGESTIONS }
            )
            return
        }
        Ecran.WEBVIEW_RECETTE -> {
            RecetteWebViewScreen(
                urlDepart = urlWebViewActuelle,
                onRetour = { ecranActif = Ecran.RECHERCHE_EN_LIGNE },
                onEnregistrerCommeRecette = { extraction ->
                    extractionEnAttente = extraction
                    ecranActif = Ecran.AJOUT_RECETTE
                }
            )
            return
        }
        Ecran.CHOIX_RECETTE_PLANNING -> {
            val cible = creneauCible
            RecettePickerScreen(
                recettes = toutesLesRecettes,
                titre = "Choisir une recette",
                onRetour = { ecranActif = Ecran.PLANNING },
                onChoisir = { recette ->
                    if (cible != null) {
                        viewModel.assignerRepas(cible.date, cible.typeRepas, recette.id)
                    }
                    creneauCible = null
                    ecranActif = Ecran.PLANNING
                }
            )
            return
        }
        else -> Unit
    }

    // Écrans de navigation principale (avec barre du bas)
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
                NavigationBarItem(
                    selected = ecranActif == Ecran.RECHERCHE_EN_LIGNE,
                    onClick = { ecranActif = Ecran.RECHERCHE_EN_LIGNE },
                    icon = { Icon(Icons.Default.Public, contentDescription = "En ligne") },
                    label = { Text("En ligne") }
                )
                NavigationBarItem(
                    selected = ecranActif == Ecran.PARAMETRES,
                    onClick = { ecranActif = Ecran.PARAMETRES },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Réglages") },
                    label = { Text("Réglages") }
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
                planning = quinzaine,
                recettes = toutesLesRecettes,
                onChoisirRecette = { date, type ->
                    creneauCible = CreneauCible(date, type)
                    ecranActif = Ecran.CHOIX_RECETTE_PLANNING
                },
                onRetirer = { viewModel.retirerRepas(it) },
                modifier = Modifier.padding(padding)
            )
            Ecran.RECHERCHE_EN_LIGNE -> RechercheEnLigneScreen(
                stock = stock,
                onLancerRecherche = { url ->
                    urlWebViewActuelle = url
                    ecranActif = Ecran.WEBVIEW_RECETTE
                },
                modifier = Modifier.padding(padding)
            )
            Ecran.PARAMETRES -> ParametresScreen(
                onExporter = { exporterEtPartager() },
                onImporter = { selecteurImportZip.launch(arrayOf("application/zip", "*/*")) },
                modifier = Modifier.padding(padding)
            )
            else -> Unit
        }
    }
}