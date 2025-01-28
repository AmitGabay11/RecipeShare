package com.example.recipeshare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipeshare.local.RecipeDatabase
import com.example.recipeshare.ui.auth.AuthScreen
import com.example.recipeshare.home.HomeScreen
import recipe.CreateRecipeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the database and DAO
        val database = RecipeDatabase.getInstance(applicationContext)
        val recipeDao = database.recipeDao()

        setContent {
            val navController: NavHostController = rememberNavController()
            NavHost(navController = navController, startDestination = "auth") {
                composable("auth") { AuthScreen(navController) }
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        recipeDao = recipeDao // Pass the DAO to HomeScreen
                    )
                }
                composable("createRecipe") { CreateRecipeScreen(navController) }
            }
        }
    }
}
