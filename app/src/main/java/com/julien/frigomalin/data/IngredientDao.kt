package com.julien.frigomalin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY datePeremption ASC")
    fun getAll(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE datePeremption IS NOT NULL AND datePeremption <= :seuil ORDER BY datePeremption ASC")
    fun getBientotPerimes(seuil: Long): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getById(id: Long): Ingredient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: Ingredient): Long

    @Update
    suspend fun update(ingredient: Ingredient)

    @Delete
    suspend fun delete(ingredient: Ingredient)

    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteById(id: Long)
}