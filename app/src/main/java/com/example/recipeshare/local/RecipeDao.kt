package com.example.recipeshare.local

import androidx.room.*

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes WHERE id = :recipeId LIMIT 1")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?

    @Query("SELECT * FROM recipes")
    suspend fun getAllRecipes(): List<RecipeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)
}

