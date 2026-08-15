package com.julien.frigomalin.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ProduitScanne(
    val nom: String,
    val quantite: String,
    val unite: String
)

object OpenFoodFactsApi {

    suspend fun rechercherProduit(codeBarre: String): ProduitScanne? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://world.openfoodfacts.org/api/v2/product/$codeBarre.json?fields=product_name,quantity")
            val connexion = url.openConnection() as HttpURLConnection
            connexion.requestMethod = "GET"
            connexion.connectTimeout = 8000
            connexion.readTimeout = 8000
            connexion.setRequestProperty("User-Agent", "FrigoMalin-Android-App-Perso")

            val reponse = connexion.inputStream.bufferedReader().use { it.readText() }
            connexion.disconnect()

            val json = JSONObject(reponse)
            if (json.optInt("status") != 1) return@withContext null

            val produit = json.optJSONObject("product") ?: return@withContext null
            val nom = produit.optString("product_name").takeIf { it.isNotBlank() } ?: return@withContext null
            val (quantite, unite) = parserQuantiteTexte(produit.optString("quantity"))

            ProduitScanne(nom = nom, quantite = quantite, unite = unite)
        } catch (e: Exception) {
            null
        }
    }

    private fun parserQuantiteTexte(texte: String?): Pair<String, String> {
        if (texte.isNullOrBlank()) return "1" to "unité"
        val nettoye = texte.trim().lowercase().replace(",", ".")
        val match = Regex("""([\d.]+)\s*(kg|g|ml|cl|l)\b""").find(nettoye)
            ?: return "1" to "unité"
        val nombre = match.groupValues[1]
        val unite = if (match.groupValues[2] == "l") "L" else match.groupValues[2]
        return nombre to unite
    }
}