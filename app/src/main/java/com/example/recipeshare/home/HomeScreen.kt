package com.example.recipeshare.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore

// Data class for a Recipe
data class Recipe(
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val author: String = ""
)

// HomeScreen Composable
@Composable
fun HomeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val recipes = remember { mutableStateListOf<Recipe>() }

    // Fetch recipes from Firestore
    LaunchedEffect(Unit) {
        db.collection("recipes").get()
            .addOnSuccessListener { result ->
                recipes.clear()
                for (document in result) {
                    val recipe = document.toObject(Recipe::class.java)
                    recipes.add(recipe)
                }
            }
    }

    // UI Layout
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // List of Recipes
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe = recipe)
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { navController.navigate("createRecipe") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Recipe")
        }
    }
}

// RecipeCard Composable
@Composable
fun RecipeCard(recipe: Recipe) {
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
            // Recipe Title
            Text(
                text = recipe.title,
                style = androidx.compose.material.MaterialTheme.typography.h6
            )
            // Recipe Description
            Text(
                text = recipe.description,
                style = androidx.compose.material.MaterialTheme.typography.body2
            )
            // Recipe Image
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
            Spacer(modifier = Modifier.height(4.dp))
            // Recipe Author
            Text(
                text = "Author: ${recipe.author}",
                style = androidx.compose.material.MaterialTheme.typography.caption
            )
        }
    }
}
