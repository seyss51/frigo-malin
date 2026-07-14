package com.julien.frigomalin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val quantite: Double,
    val unite: String,              // ex: "g", "ml", "unité", "kg", "L"
    val datePeremption: Long?,      // epoch millis, nullable si pas de péremption
    val dateAjout: Long = System.currentTimeMillis()
)