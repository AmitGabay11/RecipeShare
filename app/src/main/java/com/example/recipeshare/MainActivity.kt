package com.example.recipeshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipeshare.home.HomeScreen
import com.example.recipeshare.local.RecipeDatabase
import com.example.recipeshare.ui.recipe.CreateRecipeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and DAO
        val database = RecipeDatabase.getInstance(applicationContext)
        val recipeDao = database.recipeDao()

        setContent {
            val navController: NavHostController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable("home") {
                    HomeScreen(navController = navController, recipeDao = recipeDao)
                }
                composable("createRecipe") {
                    CreateRecipeScreen(navController = navController, recipeDao = recipeDao)
                }
            }
        }
    }
}
