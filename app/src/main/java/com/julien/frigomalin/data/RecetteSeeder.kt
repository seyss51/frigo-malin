package com.julien.frigomalin.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

suspend fun peuplerRecettesSiVide(db: FirebaseFirestore) {
    val collection = db.collection("recettes")
    val existant = collection.limit(1).get().await()
    if (!existant.isEmpty) return

    val repo = RecetteRepository(db)
    val recettes = listOf(
        Recette(nom = "Pâtes à la tomate", instructions = "Faire cuire les pâtes. Faire revenir l'ail dans l'huile, ajouter la sauce tomate, laisser mijoter 10 min. Mélanger avec les pâtes, ajouter le parmesan.", tempsPreparationMinutes = 20, portions = 2) to listOf(
            RecetteIngredient(nomIngredient = "pâtes", quantiteNecessaire = 200.0, unite = "g"),
            RecetteIngredient(nomIngredient = "sauce tomate", quantiteNecessaire = 300.0, unite = "g"),
            RecetteIngredient(nomIngredient = "ail", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(nomIngredient = "parmesan", quantiteNecessaire = 30.0, unite = "g")
        ),
        Recette(nom = "Omelette au fromage", instructions = "Battre les œufs avec sel et poivre. Cuire à la poêle avec un peu de beurre. Ajouter le fromage râpé, plier en deux.", tempsPreparationMinutes = 10, portions = 1) to listOf(
            RecetteIngredient(nomIngredient = "œufs", quantiteNecessaire = 3.0, unite = "unité"),
            RecetteIngredient(nomIngredient = "fromage râpé", quantiteNecessaire = 50.0, unite = "g"),
            RecetteIngredient(nomIngredient = "beurre", quantiteNecessaire = 10.0, unite = "g")
        ),
        Recette(nom = "Salade de riz", instructions = "Cuire le riz. Le laisser refroidir. Mélanger avec le thon, les tomates coupées, le maïs et une vinaigrette.", tempsPreparationMinutes = 25, portions = 3) to listOf(
            RecetteIngredient(nomIngredient = "riz", quantiteNecessaire = 200.0, unite = "g"),
            RecetteIngredient(nomIngredient = "thon", quantiteNecessaire = 150.0, unite = "g"),
            RecetteIngredient(nomIngredient = "tomates", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(nomIngredient = "maïs", quantiteNecessaire = 100.0, unite = "g")
        ),
        Recette(nom = "Poulet rôti aux légumes", instructions = "Assaisonner le poulet. Couper les légumes en morceaux. Enfourner le tout 45 min à 200°C.", tempsPreparationMinutes = 60, portions = 4) to listOf(
            RecetteIngredient(nomIngredient = "poulet", quantiteNecessaire = 1.0, unite = "unité"),
            RecetteIngredient(nomIngredient = "pommes de terre", quantiteNecessaire = 500.0, unite = "g"),
            RecetteIngredient(nomIngredient = "carottes", quantiteNecessaire = 300.0, unite = "g")
        ),
        Recette(nom = "Soupe de légumes", instructions = "Éplucher et couper les légumes. Cuire 30 min dans l'eau. Mixer.", tempsPreparationMinutes = 40, portions = 4) to listOf(
            RecetteIngredient(nomIngredient = "carottes", quantiteNecessaire = 300.0, unite = "g"),
            RecetteIngredient(nomIngredient = "pommes de terre", quantiteNecessaire = 300.0, unite = "g"),
            RecetteIngredient(nomIngredient = "oignon", quantiteNecessaire = 1.0, unite = "unité")
        ),
        Recette(nom = "Sandwich au thon", instructions = "Égoutter le thon, mélanger avec la mayonnaise. Tartiner le pain, ajouter salade et tomate.", tempsPreparationMinutes = 10, portions = 1) to listOf(
            RecetteIngredient(nomIngredient = "thon", quantiteNecessaire = 100.0, unite = "g"),
            RecetteIngredient(nomIngredient = "pain", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(nomIngredient = "tomates", quantiteNecessaire = 1.0, unite = "unité")
        ),
        Recette(nom = "Riz cantonais", instructions = "Cuire le riz, le laisser refroidir. Faire revenir œufs, jambon et petits pois. Ajouter le riz, sauce soja.", tempsPreparationMinutes = 25, portions = 3) to listOf(
            RecetteIngredient(nomIngredient = "riz", quantiteNecessaire = 250.0, unite = "g"),
            RecetteIngredient(nomIngredient = "œufs", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(nomIngredient = "jambon", quantiteNecessaire = 100.0, unite = "g"),
            RecetteIngredient(nomIngredient = "petits pois", quantiteNecessaire = 100.0, unite = "g")
        ),
        Recette(nom = "Crêpes", instructions = "Mélanger farine, œufs, lait. Laisser reposer 30 min. Cuire à la poêle.", tempsPreparationMinutes = 45, portions = 6) to listOf(
            RecetteIngredient(nomIngredient = "farine", quantiteNecessaire = 250.0, unite = "g"),
            RecetteIngredient(nomIngredient = "œufs", quantiteNecessaire = 3.0, unite = "unité"),
            RecetteIngredient(nomIngredient = "lait", quantiteNecessaire = 500.0, unite = "ml")
        )
    )

    recettes.forEach { (recette, ingredients) ->
        repo.insertRecetteComplete(recette, ingredients)
    }
}