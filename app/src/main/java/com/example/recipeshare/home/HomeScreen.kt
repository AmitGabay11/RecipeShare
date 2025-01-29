package com.example.recipeshare.home

import android.widget.ImageView
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.recipeshare.R


@Composable
fun HomeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val recipes = remember { mutableStateListOf<RecipeEntity>() }
    val coroutineScope = rememberCoroutineScope()

    // Load local recipes first
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val localRecipes = recipeDao.getAllRecipes()
            withContext(Dispatchers.Main) {
                recipes.clear()
                recipes.addAll(localRecipes)
            }

            // Fetch remote recipes from Firestore
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

                    coroutineScope.launch(Dispatchers.IO) {
                        recipeDao.deleteAllRecipes()
                        recipeDao.insertRecipes(remoteRecipes)

                        withContext(Dispatchers.Main) {
                            recipes.clear()
                            recipes.addAll(remoteRecipes)
                        }
                    }
                }
        }
    }

    // UI Layout with Floating Action Button
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe)
            }
        }

        // Floating Action Button for Adding New Recipe
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
                // Picasso for image loading and caching
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Picasso.get()
                                .load(it) // Load the image URL
                                .placeholder(R.drawable.placeholder) // Show while loading
                                .error(R.drawable.error) // Show if the image fails to load
                                .fit() // Scale the image to fit the dimensions
                                .centerCrop() // Crop to fit aspect ratio
                                .into(this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }
        }
    }
}

