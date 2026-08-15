package com.julien.frigomalin.data

enum class TypeRepas { MIDI, SOIR }

data class PlanningJour(
    val id: String = "",
    val date: Long = 0,
    val typeRepas: TypeRepas = TypeRepas.MIDI,
    val recetteId: String = ""
)