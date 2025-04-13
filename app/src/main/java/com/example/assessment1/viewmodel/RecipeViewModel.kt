package com.example.assessment1.viewmodel

import androidx.lifecycle.ViewModel
import com.example.assessment1.model.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecipeViewModel : ViewModel() {
    val categories = listOf("Pasta", "Soup", "Dessert", "Salad", "Pizza", "Drinks")

    private val _recipes = MutableStateFlow(emptyList<Recipe>())
    val recipes = _recipes.asStateFlow()

    private val _currentCategory = MutableStateFlow("Pasta")
    val currentCategory = _currentCategory.asStateFlow()

    private val _filter = MutableStateFlow("All")
    val filter = _filter.asStateFlow()

    private val _lastViewedRecipe = MutableStateFlow<Pair<Recipe, Long>?>(null)
    val lastViewedRecipe = _lastViewedRecipe.asStateFlow()

    fun addRecipe(recipe: Recipe) {
        _recipes.value += recipe
    }

    fun setCategory(category: String) {
        _currentCategory.value = category
    }

    fun setFilter(newFilter: String) {
        _filter.value = newFilter
    }

    fun getRecipesByCategory(category: String) =
        _recipes.value.filter { it.category == category }

    fun getRecipeById(id: String): Recipe? {
        return _recipes.value.find { it.id == id }
    }

    fun setLastViewedRecipe(recipe: Recipe) {
        _lastViewedRecipe.value = Pair(recipe, System.currentTimeMillis())
    }
}
