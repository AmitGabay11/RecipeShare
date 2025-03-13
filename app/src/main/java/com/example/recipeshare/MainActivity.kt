package com.example.recipeshare

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.recipeshare.home.HomeScreen
import com.example.recipeshare.local.RecipeDatabase
import com.example.recipeshare.trending.TrendingRecipesScreen
import com.example.recipeshare.ui.auth.AuthScreen
import com.example.recipeshare.ui.auth.SignUpScreen
import recipe.MyProfileScreen
import recipe.CreateRecipeScreen
import recipe.EditRecipeScreen
import recipe.MyRecipesScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = RecipeDatabase.getInstance(applicationContext)
        val recipeDao = database.recipeDao()
        val auth = FirebaseAuth.getInstance()
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        setContent {
            val navController: NavHostController = rememberNavController()
            val stayLoggedIn = prefs.getBoolean("stay_logged_in", false)
            val startDestination = if (auth.currentUser != null || stayLoggedIn) "home" else "auth"

            NavHost(navController = navController, startDestination = startDestination) {
                composable("auth") { AuthScreen(navController = navController) }
                composable("signUp") { SignUpScreen(navController) }
                composable("myProfile") { MyProfileScreen(navController) }
                composable("home") { HomeScreen(navController = navController, recipeDao = recipeDao) }
                composable("createRecipe") { CreateRecipeScreen(navController = navController, recipeDao = recipeDao) }
                composable("myRecipes") { MyRecipesScreen(navController = navController, recipeDao = recipeDao) }
                composable("trendingRecipes") { TrendingRecipesScreen(navController) }

                composable("editRecipe/{recipeId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId") ?: ""
                    EditRecipeScreen(navController = navController, recipeId = recipeId, recipeDao = recipeDao)
                }
            }
        }
    }
}




