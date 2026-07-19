package com.julien.frigomalin.data

import kotlinx.coroutines.flow.Flow

/**
 * Logique combinant plusieurs appels DAO. Placée hors du DAO lui-même
 * pour éviter un bug connu de KSP avec les méthodes à corps d'implémentation
 * dans les interfaces annotées @Dao.
 */
class RecetteRepository(private val recetteDao: RecetteDao) {

    fun getAllAvecIngredients(): Flow<List<RecetteAvecIngredients>> =
        recetteDao.getAllAvecIngredients()

    suspend fun insertRecetteComplete(recette: Recette, ingredients: List<RecetteIngredient>): Long {
        val recetteId = recetteDao.insertRecette(recette)
        recetteDao.insertIngredientsRecette(ingredients.map { it.copy(recetteId = recetteId) })
        return recetteId
    }
}