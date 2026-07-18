package com.julien.frigomalin.data

import kotlinx.coroutines.flow.Flow

class IngredientRepository(private val dao: IngredientDao) {
    fun getAll(): Flow<List<Ingredient>> = dao.getAll()
    fun getBientotPerimes(seuil: Long): Flow<List<Ingredient>> = dao.getBientotPerimes(seuil)
    suspend fun insert(ingredient: Ingredient): Long = dao.insert(ingredient)
    suspend fun update(ingredient: Ingredient) = dao.update(ingredient)
    suspend fun delete(ingredient: Ingredient) = dao.delete(ingredient)
}