package com.julien.frigomalin.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Ligne d'ingrédient nécessaire pour une recette.
 * Référence l'ingrédient par NOM (pas par id de stock) : une recette a besoin
 * de "farine" qu'elle soit ou non actuellement dans le stock de l'utilisateur.
 */
@Entity(
    tableName = "recette_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recette::class,
            parentColumns = ["id"],
            childColumns = ["recetteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recetteId")]
)
data class RecetteIngredient(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recetteId: Long,
    val nomIngredient: String,
    val quantiteNecessaire: Double,
    val unite: String
) 