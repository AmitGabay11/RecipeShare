package com.example.recipeshare.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val recipes = remember { mutableStateListOf<RecipeEntity>() }

    // Load local recipes first
    LaunchedEffect(Unit) {
        recipes.clear()
        recipes.addAll(recipeDao.getAllRecipes())

        // Fetch from Firestore
        db.collection("recipes").get()
            .addOnSuccessListener { result ->
                val remoteRecipes = result.map {
                    RecipeEntity(
                        title = it.getString("title") ?: "",
                        description = it.getString("description") ?: "",
                        imageUrl = it.getString("imageUrl") ?: "",
                        createdAt = it.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    )
                }

                // Update database in coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    recipeDao.deleteAllRecipes()
                    recipeDao.insertRecipes(remoteRecipes)

                    withContext(Dispatchers.Main) {
                        recipes.clear()
                        recipes.addAll(remoteRecipes)
                    }
                }
            }
    }

    // UI Layout with FAB
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe)
            }
        }

        // Floating Action Button (FAB) to Add Recipe
        FloatingActionButton(
            onClick = { navController.navigate("createRecipe") },
            modifier = Modifier
                .align(Alignment.BottomEnd) // ✅ FIXED: Correct alignment
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Recipe")
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(recipe.title, style = MaterialTheme.typography.h6)
            Text(recipe.description, style = MaterialTheme.typography.body2)

            recipe.imageUrl?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}


