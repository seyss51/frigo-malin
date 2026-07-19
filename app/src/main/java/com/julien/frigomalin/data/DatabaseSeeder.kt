package com.julien.frigomalin.data

suspend fun peuplerRecettesInitiales(recetteDao: RecetteDao) {
    val recettes = listOf(
        Recette(
            nom = "Pâtes à la tomate",
            instructions = "Faire cuire les pâtes. Faire revenir l'ail dans l'huile, ajouter la sauce tomate, laisser mijoter 10 min. Mélanger avec les pâtes, ajouter le parmesan.",
            tempsPreparationMinutes = 20,
            portions = 2
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "pâtes", quantiteNecessaire = 200.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "sauce tomate", quantiteNecessaire = 300.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "ail", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(recetteId = 0, nomIngredient = "parmesan", quantiteNecessaire = 30.0, unite = "g")
        ),
        Recette(
            nom = "Omelette au fromage",
            instructions = "Battre les œufs avec sel et poivre. Cuire à la poêle avec un peu de beurre. Ajouter le fromage râpé, plier en deux.",
            tempsPreparationMinutes = 10,
            portions = 1
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "œufs", quantiteNecessaire = 3.0, unite = "unité"),
            RecetteIngredient(recetteId = 0, nomIngredient = "fromage râpé", quantiteNecessaire = 50.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "beurre", quantiteNecessaire = 10.0, unite = "g")
        ),
        Recette(
            nom = "Salade de riz",
            instructions = "Cuire le riz. Le laisser refroidir. Mélanger avec le thon, les tomates coupées, le maïs et une vinaigrette.",
            tempsPreparationMinutes = 25,
            portions = 3
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "riz", quantiteNecessaire = 200.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "thon", quantiteNecessaire = 150.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "tomates", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(recetteId = 0, nomIngredient = "maïs", quantiteNecessaire = 100.0, unite = "g")
        ),
        Recette(
            nom = "Poulet rôti aux légumes",
            instructions = "Assaisonner le poulet. Couper les légumes en morceaux. Enfourner le tout 45 min à 200°C.",
            tempsPreparationMinutes = 60,
            portions = 4
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "poulet", quantiteNecessaire = 1.0, unite = "unité"),
            RecetteIngredient(recetteId = 0, nomIngredient = "pommes de terre", quantiteNecessaire = 500.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "carottes", quantiteNecessaire = 300.0, unite = "g")
        ),
        Recette(
            nom = "Soupe de légumes",
            instructions = "Éplucher et couper les légumes. Cuire 30 min dans l'eau. Mixer.",
            tempsPreparationMinutes = 40,
            portions = 4
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "carottes", quantiteNecessaire = 300.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "pommes de terre", quantiteNecessaire = 300.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "oignon", quantiteNecessaire = 1.0, unite = "unité")
        ),
        Recette(
            nom = "Sandwich au thon",
            instructions = "Égoutter le thon, mélanger avec la mayonnaise. Tartiner le pain, ajouter salade et tomate.",
            tempsPreparationMinutes = 10,
            portions = 1
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "thon", quantiteNecessaire = 100.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "pain", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(recetteId = 0, nomIngredient = "tomates", quantiteNecessaire = 1.0, unite = "unité")
        ),
        Recette(
            nom = "Riz cantonais",
            instructions = "Cuire le riz, le laisser refroidir. Faire revenir œufs, jambon et petits pois. Ajouter le riz, sauce soja.",
            tempsPreparationMinutes = 25,
            portions = 3
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "riz", quantiteNecessaire = 250.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "œufs", quantiteNecessaire = 2.0, unite = "unité"),
            RecetteIngredient(recetteId = 0, nomIngredient = "jambon", quantiteNecessaire = 100.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "petits pois", quantiteNecessaire = 100.0, unite = "g")
        ),
        Recette(
            nom = "Crêpes",
            instructions = "Mélanger farine, œufs, lait. Laisser reposer 30 min. Cuire à la poêle.",
            tempsPreparationMinutes = 45,
            portions = 6
        ) to listOf(
            RecetteIngredient(recetteId = 0, nomIngredient = "farine", quantiteNecessaire = 250.0, unite = "g"),
            RecetteIngredient(recetteId = 0, nomIngredient = "œufs", quantiteNecessaire = 3.0, unite = "unité"),
            RecetteIngredient(recetteId = 0, nomIngredient = "lait", quantiteNecessaire = 500.0, unite = "ml")
        )
    )

    recettes.forEach { (recette, ingredients) ->
        val id = recetteDao.insertRecette(recette)
        recetteDao.insertIngredientsRecette(ingredients.map { it.copy(recetteId = id) })
    }
}