package com.julien.frigomalin.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recettes")
data class Recette(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nom: String,
    val instructions: String,
    val tempsPreparationMinutes: Int,
    val portions: Int,
    val estPersonnalisee: Boolean = false   // false = pré-remplie, true = ajoutée par l'utilisateur
)