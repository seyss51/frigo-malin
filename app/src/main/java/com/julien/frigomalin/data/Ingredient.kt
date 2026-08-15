package com.julien.frigomalin.data

data class Ingredient(
    val id: String = "",
    val nom: String = "",
    val quantite: Double = 0.0,
    val unite: String = "",
    val datePeremption: Long? = null,
    val dateAjout: Long = System.currentTimeMillis()
)