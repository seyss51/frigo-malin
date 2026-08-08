package com.julien.frigomalin.util

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

data class RecetteExtraite(
    val titre: String,
    val url: String,
    val instructions: String? = null,
    val ingredientsBruts: List<String> = emptyList(),
    val tempsMinutes: Int? = null,
    val portions: Int? = null
)

data class IngredientParse(
    val nom: String,
    val quantite: String,
    val unite: String
)

/** Script injecté dans la page pour trouver les données structurées "Recipe" (schema.org). */
const val JS_EXTRACTION_RECETTE = """
(function() {
  try {
    var scripts = document.querySelectorAll('script[type="application/ld+json"]');
    for (var i = 0; i < scripts.length; i++) {
      var data;
      try { data = JSON.parse(scripts[i].textContent); } catch (e) { continue; }
      var items = Array.isArray(data) ? data : (data['@graph'] ? data['@graph'] : [data]);
      for (var j = 0; j < items.length; j++) {
        var item = items[j];
        var type = item['@type'];
        var isRecipe = type === 'Recipe' || (Array.isArray(type) && type.indexOf('Recipe') !== -1);
        if (isRecipe) {
          return JSON.stringify(item);
        }
      }
    }
    return null;
  } catch (e) {
    return null;
  }
})();
"""

/**
 * @param resultatBrut la valeur renvoyée par evaluateJavascript (JSON-encodée par le WebView)
 */
fun extraireRecetteDepuisJson(resultatBrut: String?, titrePage: String, url: String): RecetteExtraite {
    val titreParDefaut = titrePage.ifBlank { "Recette trouvée en ligne" }
    if (resultatBrut.isNullOrBlank() || resultatBrut == "null") {
        return RecetteExtraite(titre = titreParDefaut, url = url)
    }

    return try {
        val jsonDecode = JSONTokener(resultatBrut).nextValue() as? String
            ?: return RecetteExtraite(titre = titreParDefaut, url = url)

        val json = JSONObject(jsonDecode)

        val titre = json.optString("name", "").ifBlank { titreParDefaut }

        val ingredients = mutableListOf<String>()
        json.optJSONArray("recipeIngredient")?.let { tableau ->
            for (i in 0 until tableau.length()) ingredients.add(tableau.optString(i))
        }

        val instructions = extraireInstructions(json.opt("recipeInstructions"))
        val portions = extrairePortions(json.opt("recipeYield"))
        val temps = extraireDureeMinutes(
            json.optString("totalTime").ifBlank { null }
                ?: json.optString("cookTime").ifBlank { null }
                ?: json.optString("prepTime").ifBlank { null }
        )

        RecetteExtraite(
            titre = titre,
            url = url,
            instructions = instructions,
            ingredientsBruts = ingredients,
            tempsMinutes = temps,
            portions = portions
        )
    } catch (e: Exception) {
        RecetteExtraite(titre = titreParDefaut, url = url)
    }
}

private fun extraireInstructions(champ: Any?): String? {
    val texte = StringBuilder()
    when (champ) {
        is JSONArray -> {
            for (i in 0 until champ.length()) {
                val element = champ.get(i)
                val ligne = when (element) {
                    is JSONObject -> element.optString("text").ifBlank { element.optString("name") }
                    else -> element.toString()
                }
                if (ligne.isNotBlank()) texte.appendLine("${i + 1}. ${ligne.trim()}")
            }
        }
        is String -> texte.append(champ)
        else -> return null
    }
    return texte.toString().trim().ifBlank { null }
}

private fun extrairePortions(champ: Any?): Int? {
    val texte = when (champ) {
        is JSONArray -> if (champ.length() > 0) champ.optString(0) else null
        else -> champ?.toString()
    } ?: return null
    return Regex("""\d+""").find(texte)?.value?.toIntOrNull()
}

private fun extraireDureeMinutes(duree: String?): Int? {
    if (duree.isNullOrBlank()) return null
    val match = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?""").find(duree) ?: return null
    val heures = match.groupValues[1].toIntOrNull() ?: 0
    val minutes = match.groupValues[2].toIntOrNull() ?: 0
    val total = heures * 60 + minutes
    return if (total == 0) null else total
}

private val UNITES_CONNUES = listOf(
    "cuillères à soupe", "cuillère à soupe", "cuillères à café", "cuillère à café",
    "c. à soupe", "c. à café", "pincées", "pincée", "gousses", "gousse",
    "tranches", "tranche", "tasses", "tasse", "kg", "g", "ml", "cl", "l", "L", "unités", "unité"
)

/** Découpe une ligne texte brute (ex: "200 g de farine") en quantité / unité / nom. Best-effort. */
fun parserLigneIngredient(texteBrut: String): IngredientParse {
    val texte = texteBrut.trim()
    val match = Regex("""^([\d]+(?:[.,][\d]+)?(?:/[\d]+)?)\s*(.*)$""").find(texte)
        ?: return IngredientParse(nom = texte, quantite = "1", unite = "unité")

    val quantite = evaluerFraction(match.groupValues[1].replace(",", "."))
    var reste = match.groupValues[2].trim()

    var uniteTrouvee = "unité"
    for (unite in UNITES_CONNUES) {
        if (reste.startsWith(unite, ignoreCase = true)) {
            uniteTrouvee = if (unite.contains("soupe") || unite.contains("café")) "unité" else unite
            reste = reste.removePrefix(unite).trim()
            break
        }
    }
    reste = reste.removePrefix("de ").removePrefix("d'").trim()

    return IngredientParse(
        nom = reste.ifBlank { texte },
        quantite = quantite,
        unite = uniteTrouvee
    )
}

private fun evaluerFraction(valeur: String): String {
    if (!valeur.contains("/")) return valeur
    val parts = valeur.split("/")
    val num = parts.getOrNull(0)?.toDoubleOrNull()
    val den = parts.getOrNull(1)?.toDoubleOrNull()
    return if (num != null && den != null && den != 0.0) (num / den).toString() else "1"
}