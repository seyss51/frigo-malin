package com.julien.frigomalin.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class IngredientRepository(private val db: FirebaseFirestore) {

    private val collection = db.collection("ingredients")

    fun getAll(): Flow<List<Ingredient>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, _ ->
            val liste = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Ingredient::class.java)?.copy(id = doc.id)
            }?.sortedBy { it.datePeremption ?: Long.MAX_VALUE } ?: emptyList()
            trySend(liste)
        }
        awaitClose { listener.remove() }
    }

    suspend fun insert(ingredient: Ingredient) {
        collection.add(ingredient.copy(id = "")).await()
    }

    suspend fun update(ingredient: Ingredient) {
        collection.document(ingredient.id).set(ingredient.copy(id = "")).await()
    }

    suspend fun delete(ingredient: Ingredient) {
        collection.document(ingredient.id).delete().await()
    }
}