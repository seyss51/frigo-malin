package com.julien.frigomalin

import android.app.Application
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.julien.frigomalin.data.AuthRepository
import com.julien.frigomalin.data.IngredientRepository
import com.julien.frigomalin.data.PlanningRepository
import com.julien.frigomalin.data.RecetteRepository

class FrigoMalinApplication : Application() {

    val authRepository: AuthRepository by lazy { AuthRepository(Firebase.auth) }
    val ingredientRepository: IngredientRepository by lazy { IngredientRepository(Firebase.firestore) }
    val recetteRepository: RecetteRepository by lazy { RecetteRepository(Firebase.firestore) }
    val planningRepository: PlanningRepository by lazy { PlanningRepository(Firebase.firestore) }
}