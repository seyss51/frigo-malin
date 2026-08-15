package com.julien.frigomalin.data

data class Recette(
    val id: String = "",
    val nom: String = "",
    val instructions: String = "",
    val tempsPreparationMinutes: Int = 0,
    val portions: Int = 4,
    val estPersonnalisee: Boolean = false,
    val photoPath: String? = null,
    val sourceUrl: String? = null
)

data class RecetteIngredient(
    val recetteId: String = "",
    val nomIngredient: String = "",
    val quantiteNecessaire: Double = 0.0,
    val unite: String = ""
)

data class RecetteAvecIngredients(
    val recette: Recette,
    val ingredients: List<RecetteIngredient>
)