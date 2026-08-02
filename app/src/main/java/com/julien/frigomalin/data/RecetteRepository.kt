package com.julien.frigomalin.data

import kotlinx.coroutines.flow.Flow

class RecetteRepository(private val recetteDao: RecetteDao) {

    fun getAllAvecIngredients(): Flow<List<RecetteAvecIngredients>> =
        recetteDao.getAllAvecIngredients()

    suspend fun insertRecetteComplete(recette: Recette, ingredients: List<RecetteIngredient>): Long {
        val recetteId = recetteDao.insertRecette(recette)
        recetteDao.insertIngredientsRecette(ingredients.map { it.copy(recetteId = recetteId) })
        return recetteId
    }

    suspend fun updateRecetteComplete(recette: Recette, ingredients: List<RecetteIngredient>) {
        recetteDao.updateRecette(recette)
        recetteDao.deleteIngredientsDeRecette(recette.id)
        recetteDao.insertIngredientsRecette(ingredients.map { it.copy(recetteId = recette.id) })
    }
}