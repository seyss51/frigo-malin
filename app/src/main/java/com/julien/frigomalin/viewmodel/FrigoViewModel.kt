package com.julien.frigomalin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.julien.frigomalin.data.Ingredient
import com.julien.frigomalin.data.IngredientRepository
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.data.RecetteAvecIngredients
import com.julien.frigomalin.data.RecetteIngredient
import com.julien.frigomalin.data.RecetteRepository
import com.julien.frigomalin.suggestion.RecetteSuggestionEngine
import com.julien.frigomalin.suggestion.SuggestionRecette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FrigoViewModel(
    private val ingredientRepository: IngredientRepository,
    private val recetteRepository: RecetteRepository
) : ViewModel() {

    val stock: StateFlow<List<Ingredient>> = ingredientRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recettes: StateFlow<List<RecetteAvecIngredients>> =
        recetteRepository.getAllAvecIngredients()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions: StateFlow<List<SuggestionRecette>> = combine(stock, recettes) { s, r ->
        RecetteSuggestionEngine.suggerer(s, r)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun ajouterIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientRepository.insert(ingredient)
        }
    }

    fun supprimerIngredient(ingredient: Ingredient) {
        viewModelScope.launch {
            ingredientRepository.delete(ingredient)
        }
    }

    fun ajouterRecette(recette: Recette, ingredients: List<RecetteIngredient>) {
        viewModelScope.launch {
            recetteRepository.insertRecetteComplete(recette, ingredients)
        }
    }

    class Factory(
        private val ingredientRepository: IngredientRepository,
        private val recetteRepository: RecetteRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FrigoViewModel(ingredientRepository, recetteRepository) as T
        }
    }
}