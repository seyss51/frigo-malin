package com.julien.frigomalin.data

import kotlinx.coroutines.flow.Flow

class PlanningRepository(private val dao: PlanningDao) {
    fun getSemaine(debut: Long, fin: Long): Flow<List<PlanningJour>> = dao.getSemaine(debut, fin)
    suspend fun insert(planningJour: PlanningJour): Long = dao.insert(planningJour)
    suspend fun delete(planningJour: PlanningJour) = dao.delete(planningJour)
}