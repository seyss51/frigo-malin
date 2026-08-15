package com.julien.frigomalin.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlanningRepository(private val db: FirebaseFirestore) {

    private val collection = db.collection("planning")

    fun getSemaine(debut: Long, fin: Long): Flow<List<PlanningJour>> = callbackFlow {
        val listener = collection
            .whereGreaterThanOrEqualTo("date", debut)
            .whereLessThanOrEqualTo("date", fin)
            .addSnapshotListener { snapshot, _ ->
                val liste = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PlanningJour::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(liste)
            }
        awaitClose { listener.remove() }
    }

    suspend fun insert(planningJour: PlanningJour) {
        collection.add(planningJour.copy(id = "")).await()
    }

    suspend fun delete(planningJour: PlanningJour) {
        collection.document(planningJour.id).delete().await()
    }
}