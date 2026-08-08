package com.julien.frigomalin.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.julien.frigomalin.util.JS_EXTRACTION_RECETTE
import com.julien.frigomalin.util.RecetteExtraite
import com.julien.frigomalin.util.extraireRecetteDepuisJson

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RecetteWebViewScreen(
    urlDepart: String,
    onRetour: () -> Unit,
    onEnregistrerCommeRecette: (RecetteExtraite) -> Unit
) {
    var titrePage by remember { mutableStateOf("") }
    var urlActuelle by remember { mutableStateOf(urlDepart) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var extractionEnCours by remember { mutableStateOf(false) }

    fun retourOuFermer() {
        val webView = webViewRef
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            onRetour()
        }
    }

    BackHandler(onBack = { retourOuFermer() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titrePage.ifBlank { "Recherche de recette" }, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { retourOuFermer() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (extractionEnCours) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 12.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = {
                            val webView = webViewRef
                            if (webView == null) {
                                onEnregistrerCommeRecette(RecetteExtraite(titre = titrePage.ifBlank { "Recette trouvée en ligne" }, url = urlActuelle))
                                return@IconButton
                            }
                            extractionEnCours = true
                            webView.evaluateJavascript(JS_EXTRACTION_RECETTE) { resultat ->
                                extractionEnCours = false
                                val extraction = extraireRecetteDepuisJson(resultat, titrePage, urlActuelle)
                                onEnregistrerCommeRecette(extraction)
                            }
                        }) {
                            Icon(Icons.Default.Bookmark, contentDescription = "Enregistrer cette recette")
                        }
                    }
                }
            )
        }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            titrePage = view?.title ?: ""
                            urlActuelle = url ?: urlActuelle
                        }
                    }
                    loadUrl(urlDepart)
                    webViewRef = this
                }
            }
        )
    }
}