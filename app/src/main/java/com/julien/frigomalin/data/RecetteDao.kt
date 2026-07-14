package com.julien.frigomalin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class RecetteAvecIngredients(
    @Embedded val recette: Recette,
    @Relation(
        parentColumn = "id",
        entityColumn = "recetteId"
    )
    val ingredients: List<RecetteIngredient>
)

@Dao
interface RecetteDao {
    @Transaction
    @Query("SELECT * FROM recettes ORDER BY nom ASC")
    fun getAllAvecIngredients(): Flow<List<RecetteAvecIngredients>>

    @Transaction
    @Query("SELECT * FROM recettes WHERE id = :id")
    suspend fun getByIdAvecIngredients(id: Long): RecetteAvecIngredients?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecette(recette: Recette): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredientsRecette(ingredients: List<RecetteIngredient>)

    @Update
    suspend fun updateRecette(recette: Recette)

    @Delete
    suspend fun deleteRecette(recette: Recette)

    @Query("DELETE FROM recette_ingredients WHERE recetteId = :recetteId")
    suspend fun deleteIngredientsDeRecette(recetteId: Long)
}