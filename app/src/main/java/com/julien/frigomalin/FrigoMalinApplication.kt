package com.julien.frigomalin

import android.app.Application
import com.julien.frigomalin.data.DatabaseProvider
import com.julien.frigomalin.data.IngredientRepository
import com.julien.frigomalin.data.PlanningRepository
import com.julien.frigomalin.data.RecetteRepository

class FrigoMalinApplication : Application() {

    val ingredientRepository: IngredientRepository by lazy {
        IngredientRepository(DatabaseProvider.getDatabase(this).ingredientDao())
    }

    val recetteRepository: RecetteRepository by lazy {
        RecetteRepository(DatabaseProvider.getDatabase(this).recetteDao())
    }

    val planningRepository: PlanningRepository by lazy {
        PlanningRepository(DatabaseProvider.getDatabase(this).planningDao())
    }
}