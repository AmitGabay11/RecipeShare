package com.example.recipeshare.api

data class RecipeResponse(
    val recipes: List<Recipe>
)

data class Recipe(
    val id: Int,
    val title: String,
    val image: String,
    val sourceUrl: String? = null
)
