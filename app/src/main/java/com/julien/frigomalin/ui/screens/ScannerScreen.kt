package com.julien.frigomalin.ui.screens

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.julien.frigomalin.util.BarcodeAnalyzer
import com.julien.frigomalin.util.OpenFoodFactsApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onRetour: () -> Unit,
    onResultat: (nom: String, quantite: String, unite: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var codeDetecte by remember { mutableStateOf<String?>(null) }
    var recherchEnCours by remember { mutableStateOf(false) }
    var messageIntrouvable by remember { mutableStateOf(false) }

    fun traiterCode(code: String) {
        if (codeDetecte != null) return
        codeDetecte = code
        recherchEnCours = true
        scope.launch {
            val produit = OpenFoodFactsApi.rechercherProduit(code)
            recherchEnCours = false
            if (produit != null) {
                onResultat(produit.nom, produit.quantite, produit.unite)
            } else {
                messageIntrouvable = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scanner un produit") },
                navigationIcon = {
                    IconButton(onClick = onRetour) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val analyse = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(
                                    ContextCompat.getMainExecutor(ctx),
                                    BarcodeAnalyzer { code -> traiterCode(code) }
                                )
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                analyse
                            )
                        } catch (e: Exception) {
                            // caméra indisponible sur cet appareil
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )

            if (recherchEnCours) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Recherche du produit...", color = Color.White)
                    }
                }
            } else if (messageIntrouvable) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            "Produit non trouvé dans la base Open Food Facts.",
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onResultat("", "1", "unité") }) {
                            Text("Compléter manuellement")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            codeDetecte = null
                            messageIntrouvable = false
                        }) {
                            Text("Réessayer un scan", color = Color.White)
                        }
                    }
                }
            } else {
                Text(
                    "Vise le code-barres du produit",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(24.dp)
                )
            }
        }
    }
}