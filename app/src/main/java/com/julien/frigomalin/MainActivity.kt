package com.julien.frigomalin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.julien.frigomalin.ui.screens.StockScreen
import com.julien.frigomalin.ui.screens.SuggestionsScreen
import com.julien.frigomalin.viewmodel.FrigoViewModel
import kotlinx.coroutines.flow.collectAsStateWithLifecycle

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
    var ongletSelectionne by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    val stock by viewModel.stock.collectAsStateWithLifecycle()
    val suggestions by viewModel.suggestions.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = ongletSelectionne == 0,
                    onClick = { ongletSelectionne = 0 },
                    icon = { Icon(Icons.Default.Kitchen, contentDescription = "Stock") },
                    label = { Text("Stock") }
                )
                NavigationBarItem(
                    selected = ongletSelectionne == 1,
                    onClick = { ongletSelectionne = 1 },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Suggestions") },
                    label = { Text("Suggestions") }
                )
            }
        }
    ) { padding ->
        when (ongletSelectionne) {
            0 -> StockScreen(
                stock = stock,
                onSupprimer = { viewModel.supprimerIngredient(it) },
                modifier = Modifier.padding(padding)
            )
            1 -> SuggestionsScreen(
                suggestions = suggestions,
                modifier = Modifier.padding(padding)
            )
        }
    }
}