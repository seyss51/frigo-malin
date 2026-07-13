package com.julien.frigomalin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanningDao {
    @Query("SELECT * FROM planning_jours WHERE date BETWEEN :debut AND :fin ORDER BY date ASC")
    fun getSemaine(debut: Long, fin: Long): Flow<List<PlanningJour>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(planningJour: PlanningJour): Long

    @Delete
    suspend fun delete(planningJour: PlanningJour)
}