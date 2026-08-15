package com.julien.frigomalin.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private data class RecetteDocument(
    val nom: String = "",
    val instructions: String = "",
    val tempsPreparationMinutes: Int = 0,
    val portions: Int = 4,
    val estPersonnalisee: Boolean = false,
    val photoPath: String? = null,
    val sourceUrl: String? = null,
    val ingredients: List<Map<String, Any>> = emptyList()
)

class RecetteRepository(private val db: FirebaseFirestore) {

    private val collection = db.collection("recettes")

    fun getAllAvecIngredients(): Flow<List<RecetteAvecIngredients>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, _ ->
            val liste = snapshot?.documents?.mapNotNull { doc -> mapDocument(doc.id, doc.data) }
                ?.sortedBy { it.recette.nom } ?: emptyList()
            trySend(liste)
        }
        awaitClose { listener.remove() }
    }

    suspend fun insertRecetteComplete(recette: Recette, ingredients: List<RecetteIngredient>) {
        collection.add(versDocument(recette, ingredients)).await()
    }

    suspend fun updateRecetteComplete(recette: Recette, ingredients: List<RecetteIngredient>) {
        collection.document(recette.id).set(versDocument(recette, ingredients)).await()
    }

    private fun versDocument(recette: Recette, ingredients: List<RecetteIngredient>): RecetteDocument {
        return RecetteDocument(
            nom = recette.nom,
            instructions = recette.instructions,
            tempsPreparationMinutes = recette.tempsPreparationMinutes,
            portions = recette.portions,
            estPersonnalisee = recette.estPersonnalisee,
            photoPath = recette.photoPath,
            sourceUrl = recette.sourceUrl,
            ingredients = ingredients.map {
                mapOf(
                    "nomIngredient" to it.nomIngredient,
                    "quantiteNecessaire" to it.quantiteNecessaire,
                    "unite" to it.unite
                )
            }
        )
    }

    private fun mapDocument(id: String, data: Map<String, Any>?): RecetteAvecIngredients? {
        if (data == null) return null
        val recette = Recette(
            id = id,
            nom = data["nom"] as? String ?: "",
            instructions = data["instructions"] as? String ?: "",
            tempsPreparationMinutes = (data["tempsPreparationMinutes"] as? Long)?.toInt() ?: 0,
            portions = (data["portions"] as? Long)?.toInt() ?: 4,
            estPersonnalisee = data["estPersonnalisee"] as? Boolean ?: false,
            photoPath = data["photoPath"] as? String,
            sourceUrl = data["sourceUrl"] as? String
        )
        @Suppress("UNCHECKED_CAST")
        val ingredientsBruts = data["ingredients"] as? List<Map<String, Any>> ?: emptyList()
        val ingredients = ingredientsBruts.map {
            RecetteIngredient(
                recetteId = id,
                nomIngredient = it["nomIngredient"] as? String ?: "",
                quantiteNecessaire = (it["quantiteNecessaire"] as? Number)?.toDouble() ?: 0.0,
                unite = it["unite"] as? String ?: ""
            )
        }
        return RecetteAvecIngredients(recette, ingredients)
    }
}