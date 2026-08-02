package com.julien.frigomalin.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.data.RecetteAvecIngredients
import com.julien.frigomalin.data.RecetteIngredient
import com.julien.frigomalin.util.PhotoStorage

private data class LigneIngredient(
    val nom: String = "",
    val quantite: String = "",
    val unite: String = "g"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjouterRecetteScreen(
    recetteExistante: RecetteAvecIngredients? = null,
    onRetour: () -> Unit,
    onEnregistrer: (Recette, List<RecetteIngredient>) -> Unit
) {
    val context = LocalContext.current
    val estEdition = recetteExistante != null

    var nom by remember { mutableStateOf(recetteExistante?.recette?.nom ?: "") }
    var instructions by remember { mutableStateOf(recetteExistante?.recette?.instructions ?: "") }
    var temps by remember { mutableStateOf(recetteExistante?.recette?.tempsPreparationMinutes?.toString() ?: "") }
    var portions by remember { mutableStateOf(recetteExistante?.recette?.portions?.toString() ?: "4") }
    var photoNomFichier by remember { mutableStateOf(recetteExistante?.recette?.photoPath) }
    var lignesIngredients by remember {
        mutableStateOf(
            recetteExistante?.ingredients?.map {
                LigneIngredient(it.nomIngredient, it.quantiteNecessaire.toString(), it.unite)
            } ?: listOf(LigneIngredient())
        )
    }

    val selecteurPhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoNomFichier = PhotoStorage.copierDansStockageInterne(context, uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estEdition) "Modifier la recette" else "Ajouter une recette") },
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
            val fichierPhoto = PhotoStorage.fichierPhoto(context, photoNomFichier)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        selecteurPhoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (fichierPhoto != null) {
                    AsyncImage(
                        model = fichierPhoto,
                        contentDescription = "Photo du plat",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ajouter une photo du plat")
                    }
                }
            }

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
                    verticalAlignment = Alignment.CenterVertically,
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
                                recetteId = recetteExistante?.recette?.id ?: 0,
                                nomIngredient = ligne.nom.trim(),
                                quantiteNecessaire = quantiteValeur,
                                unite = ligne.unite
                            )
                        } else null
                    }

                    onEnregistrer(
                        Recette(
                            id = recetteExistante?.recette?.id ?: 0,
                            nom = nom.trim(),
                            instructions = instructions.trim(),
                            tempsPreparationMinutes = tempsValeur,
                            portions = portionsValeur,
                            estPersonnalisee = recetteExistante?.recette?.estPersonnalisee ?: true,
                            photoPath = photoNomFichier
                        ),
                        ingredientsValides
                    )
                    onRetour()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estEdition) "Mettre à jour" else "Enregistrer la recette")
            }
        }
    }
}