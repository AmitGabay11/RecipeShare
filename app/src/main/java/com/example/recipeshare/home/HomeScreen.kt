package com.example.recipeshare.home

import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.recipeshare.R
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val recipes = remember { mutableStateListOf<RecipeEntity>() }
    val coroutineScope = rememberCoroutineScope()

    // Load recipes from Room and sync with Firestore
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                // Fetch local recipes from Room
                val localRecipes = recipeDao.getAllRecipes()
                withContext(Dispatchers.Main) {
                    recipes.clear()
                    recipes.addAll(localRecipes)
                }

                // Fetch remote recipes from Firestore
                val snapshot = db.collection("recipes").get().await()
                val remoteRecipes = snapshot.documents.mapNotNull { document ->
                    try {
                        RecipeEntity(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            createdAt = (document["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                            userId = document.getString("userId") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // Update local database
                recipeDao.insertRecipes(remoteRecipes)

                // Update UI
                withContext(Dispatchers.Main) {
                    recipes.clear()
                    recipes.addAll(remoteRecipes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Share") },
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        prefs.edit().putBoolean("stay_logged_in", false).apply()
                        navController.navigate("auth") { popUpTo("home") { inclusive = true } }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createRecipe") },
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Recipe")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(recipes) { recipe ->
                RecipeCard(recipe, recipeDao, coroutineScope, recipes, navController)
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeEntity,
    recipeDao: RecipeDao,
    coroutineScope: CoroutineScope,
    recipes: MutableList<RecipeEntity>,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(recipe.title, style = MaterialTheme.typography.h6)
            Text(recipe.description, style = MaterialTheme.typography.body2)

            if (recipe.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Picasso.get()
                                .load(recipe.imageUrl)
                                .placeholder(R.drawable.placeholder)
                                .error(R.drawable.error)
                                .fit()
                                .centerCrop()
                                .into(this)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                FirebaseFirestore.getInstance().collection("recipes")
                                    .document(recipe.id).delete().await()
                                recipeDao.deleteRecipeById(recipe.id)

                                withContext(Dispatchers.Main) {
                                    recipes.removeAll { it.id == recipe.id }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }

                Button(onClick = {
                    navController.navigate("editRecipe/${recipe.id}")
                }) {
                    Text("Edit")
                }
            }
        }
    }
}
