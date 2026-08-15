package com.julien.frigomalin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.julien.frigomalin.data.AuthRepository
import com.julien.frigomalin.data.Ingredient
import com.julien.frigomalin.data.IngredientRepository
import com.julien.frigomalin.data.PlanningJour
import com.julien.frigomalin.data.PlanningRepository
import com.julien.frigomalin.data.Recette
import com.julien.frigomalin.data.RecetteAvecIngredients
import com.julien.frigomalin.data.RecetteIngredient
import com.julien.frigomalin.data.RecetteRepository
import com.julien.frigomalin.data.TypeRepas
import com.julien.frigomalin.data.peuplerRecettesSiVide
import com.julien.frigomalin.suggestion.ArticleCourse
import com.julien.frigomalin.suggestion.RecetteSuggestionEngine
import com.julien.frigomalin.suggestion.ShoppingListGenerator
import com.julien.frigomalin.suggestion.SuggestionRecette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FrigoViewModel(
    val authRepository: AuthRepository,
    private val ingredientRepository: IngredientRepository,
    private val recetteRepository: RecetteRepository,
    private val planningRepository: PlanningRepository
) : ViewModel() {

    val estConnecte: StateFlow<Boolean> = authRepository.utilisateurConnecte()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        viewModelScope.launch {
            estConnecte.collect { connecte ->
                if (connecte) {
                    runCatching { peuplerRecettesSiVide(FirebaseFirestore.getInstance()) }
                }
            }
        }
    }

    fun seConnecter(email: String, motDePasse: String, onErreur: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.seConnecter(email, motDePasse)
                .onFailure { onErreur(it.message ?: "Connexion impossible") }
        }
    }

    fun seDeconnecter() {
        authRepository.seDeconnecter()
    }

    // Toutes les collections ci-dessous n'écoutent Firestore QUE si l'utilisateur est connecté,
    // pour éviter les erreurs PERMISSION_DENIED (les règles Firestore exigent une session active).

    val stock: StateFlow<List<Ingredient>> = estConnecte.flatMapLatest { connecte ->
        if (connecte) ingredientRepository.getAll() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recettes: StateFlow<List<RecetteAvecIngredients>> = estConnecte.flatMapLatest { connecte ->
        if (connecte) recetteRepository.getAllAvecIngredients() else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toutesLesRecettes: StateFlow<List<Recette>> = run {
        val out = MutableStateFlow<List<Recette>>(emptyList())
        viewModelScope.launch {
            recettes.collect { liste -> out.value = liste.map { it.recette } }
        }
        out
    }

    val suggestions: StateFlow<List<SuggestionRecette>> = combine(stock, recettes) { s, r ->
        RecetteSuggestionEngine.suggerer(s, r)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recetteSelectionneeId = MutableStateFlow<String?>(null)
    private val portionsSelectionnees = MutableStateFlow(1)

    val recetteSelectionnee: StateFlow<RecetteAvecIngredients?> =
        combine(recettes, recetteSelectionneeId) { liste, id ->
            liste.firstOrNull { it.recette.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val portionsActuelles: StateFlow<Int> = portionsSelectionnees

    val listeCourses: StateFlow<List<ArticleCourse>> =
        combine(recetteSelectionnee, stock, portionsSelectionnees) { recette, stockActuel, portions ->
            recette?.let {
                val base = it.recette.portions.coerceAtLeast(1)
                val facteur = portions.toDouble() / base
                ShoppingListGenerator.genererPour(it.ingredients, stockActuel, facteur)
            } ?: emptyList()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectionnerRecette(id: String, portionsParDefaut: Int) {
        recetteSelectionneeId.value = id
        portionsSelectionnees.value = portionsParDefaut.coerceAtLeast(1)
    }

    fun definirPortions(portions: Int) {
        portionsSelectionnees.value = portions.coerceAtLeast(1)
    }

    fun ajouterIngredient(ingredient: Ingredient) {
        viewModelScope.launch { ingredientRepository.insert(ingredient) }
    }

    fun modifierIngredient(ingredient: Ingredient) {
        viewModelScope.launch { ingredientRepository.update(ingredient) }
    }

    fun supprimerIngredient(ingredient: Ingredient) {
        viewModelScope.launch { ingredientRepository.delete(ingredient) }
    }

    fun enregistrerRecette(recette: Recette, ingredients: List<RecetteIngredient>) {
        viewModelScope.launch {
            if (recette.id.isBlank()) {
                recetteRepository.insertRecetteComplete(recette, ingredients)
            } else {
                recetteRepository.updateRecetteComplete(recette, ingredients)
            }
        }
    }

    val quinzaineActuelle: StateFlow<List<PlanningJour>> = estConnecte.flatMapLatest { connecte ->
        if (connecte) {
            val (debut, fin) = bornesQuinzaineCourante()
            planningRepository.getSemaine(debut, fin)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun assignerRepas(date: Long, typeRepas: TypeRepas, recetteId: String) {
        viewModelScope.launch {
            planningRepository.insert(PlanningJour(date = date, typeRepas = typeRepas, recetteId = recetteId))
        }
    }

    fun retirerRepas(planningJour: PlanningJour) {
        viewModelScope.launch { planningRepository.delete(planningJour) }
    }

    private fun bornesQuinzaineCourante(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val debut = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 14)
        val fin = cal.timeInMillis - 1
        return debut to fin
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val ingredientRepository: IngredientRepository,
        private val recetteRepository: RecetteRepository,
        private val planningRepository: PlanningRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return FrigoViewModel(authRepository, ingredientRepository, recetteRepository, planningRepository) as T
        }
    }
}