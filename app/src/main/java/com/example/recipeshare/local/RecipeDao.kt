package com.example.recipeshare.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeDao {

    // Fetch all recipes sorted by creation time
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    suspend fun getAllRecipes(): List<RecipeEntity> // Room maps to a List of RecipeEntity

    // Insert multiple recipes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>): List<Long> // Return row IDs of inserted recipes

    // Delete all recipes
    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes(): Int // Return number of rows deleted
}
