package com.example.recipeshare.trending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recipeshare.api.SpoonacularApi
import com.example.recipeshare.api.Recipe
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember


class TrendingRecipesViewModel : ViewModel() {
    private val api = SpoonacularApi.create()

    var recipes = mutableStateOf<List<Recipe>>(emptyList())
    var isLoading = mutableStateOf(false)
    var errorMessage = mutableStateOf<String?>(null)

    init {
        fetchTrendingRecipes()
    }

    fun fetchTrendingRecipes() {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            try {
                val response = api.getTrendingRecipes()
                recipes.value = response.recipes
            } catch (e: Exception) {
                errorMessage.value = "Failed to load recipes."
            } finally {
                isLoading.value = false
            }
        }
    }
}
