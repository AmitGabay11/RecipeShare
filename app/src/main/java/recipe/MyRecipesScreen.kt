package com.example.recipeshare.ui.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun MyRecipesScreen(navController: NavController, recipeDao: RecipeDao) {
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    var recipes by remember { mutableStateOf(emptyList<RecipeEntity>()) }

    LaunchedEffect(Unit) {
        recipes = recipeDao.getAllRecipes().filter { it.userId == userId }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("My Recipes") }) }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            items(recipes) { recipe ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(recipe.title, style = MaterialTheme.typography.h6)
                        Text(recipe.description)
                        Button(onClick = { navController.navigate("editRecipe/${recipe.id}") }) { Text("Edit") }
                    }
                }
            }
        }
    }
}

