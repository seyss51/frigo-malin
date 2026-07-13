package com.julien.frigomalin.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TypeRepas { MIDI, SOIR }

@Entity(
    tableName = "planning_jours",
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
data class PlanningJour(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,          // epoch millis, tronqué au jour
    val typeRepas: TypeRepas,
    val recetteId: Long
)