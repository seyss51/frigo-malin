package com.julien.frigomalin.suggestion

import com.julien.frigomalin.data.Ingredient
import com.julien.frigomalin.data.RecetteIngredient

data class ArticleCourse(
    val nom: String,
    val quantiteManquante: Double,
    val unite: String
)

object ShoppingListGenerator {

    /**
     * Génère la liste des ingrédients manquants pour une recette donnée,
     * en tenant compte des quantités déjà présentes en stock.
     */
    fun genererPour(
        ingredientsNecessaires: List<RecetteIngredient>,
        stock: List<Ingredient>
    ): List<ArticleCourse> {
        val stockParNom = stock.groupBy { normaliser(it.nom) }

        return ingredientsNecessaires.mapNotNull { besoin ->
            val disponible = stockParNom[normaliser(besoin.nomIngredient)]
                ?.sumOf { it.quantite } ?: 0.0

            val manquant = besoin.quantiteNecessaire - disponible
            if (manquant > 0) {
                ArticleCourse(besoin.nomIngredient, manquant, besoin.unite)
            } else {
                null
            }
        }
    }

    /**
     * Fusionne plusieurs listes de courses (plusieurs recettes) en additionnant
     * les quantités des ingrédients en commun.
     */
    fun fusionner(listes: List<List<ArticleCourse>>): List<ArticleCourse> {
        return listes.flatten()
            .groupBy { normaliser(it.nom) to it.unite }
            .map { (cle, articles) ->
                ArticleCourse(
                    nom = articles.first().nom,
                    quantiteManquante = articles.sumOf { it.quantiteManquante },
                    unite = cle.second
                )
            }
    }

    private fun normaliser(nom: String): String = nom.trim().lowercase()
}