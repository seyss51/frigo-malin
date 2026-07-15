package com.julien.frigomalin.suggestion

import com.julien.frigomalin.data.Ingredient
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.data.RecetteAvecIngredients

data class SuggestionRecette(
    val recette: Recette,
    val pourcentageDisponible: Int, // 0-100
    val ingredientsManquants: List<String>,
    val ingredientsPerimantBientot: List<String>,
    val score: Double
)

object RecetteSuggestionEngine {

    private const val MILLIS_PAR_JOUR = 24 * 60 * 60 * 1000L
    private const val SEUIL_PEREMPTION_JOURS = 4

    /**
     * Classe les recettes selon leur pertinence : % d'ingrédients disponibles
     * en stock + bonus pour celles utilisant des ingrédients bientôt périmés.
     */
    fun suggerer(
        stock: List<Ingredient>,
        recettes: List<RecetteAvecIngredients>,
        maintenant: Long = System.currentTimeMillis()
    ): List<SuggestionRecette> {
        val stockParNom = stock.groupBy { normaliser(it.nom) }
        val seuilPeremption = maintenant + SEUIL_PEREMPTION_JOURS * MILLIS_PAR_JOUR

        return recettes
            .map { evaluerRecette(it, stockParNom, seuilPeremption) }
            .sortedByDescending { it.score }
    }

    private fun evaluerRecette(
        recetteAvecIngredients: RecetteAvecIngredients,
        stockParNom: Map<String, List<Ingredient>>,
        seuilPeremption: Long
    ): SuggestionRecette {
        val ingredientsNecessaires = recetteAvecIngredients.ingredients
        val recette = recetteAvecIngredients.recette

        if (ingredientsNecessaires.isEmpty()) {
            return SuggestionRecette(recette, 0, emptyList(), emptyList(), 0.0)
        }

        val manquants = mutableListOf<String>()
        val perimantBientot = mutableListOf<String>()
        var disponibles = 0

        for (besoin in ingredientsNecessaires) {
            val correspondances = stockParNom[normaliser(besoin.nomIngredient)]
            val ingredientDispo = correspondances?.firstOrNull {
                it.quantite >= besoin.quantiteNecessaire
            }

            if (ingredientDispo != null) {
                disponibles++
                val datePeremption = ingredientDispo.datePeremption
                if (datePeremption != null && datePeremption <= seuilPeremption) {
                    perimantBientot.add(besoin.nomIngredient)
                }
            } else {
                manquants.add(besoin.nomIngredient)
            }
        }

        val pourcentage = (disponibles * 100) / ingredientsNecessaires.size
        // Priorité forte à la complétude, bonus pour utiliser les ingrédients qui périment bientôt
        val score = pourcentage.toDouble() + (perimantBientot.size * 15.0)

        return SuggestionRecette(
            recette = recette,
            pourcentageDisponible = pourcentage,
            ingredientsManquants = manquants,
            ingredientsPerimantBientot = perimantBientot,
            score = score
        )
    }

    private fun normaliser(nom: String): String = nom.trim().lowercase()
}