package com.example.recipeshare.home

import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso

@Composable
fun HomeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val recipes = remember { mutableStateListOf<RecipeEntity>() }
    val coroutineScope = rememberCoroutineScope()
    var listener: ListenerRegistration? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        listener?.remove()
        listener = db.collection("recipes").addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            snapshot?.let { querySnapshot ->
                val updatedRecipes = querySnapshot.documents.mapNotNull { document ->
                    try {
                        RecipeEntity(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            description = document.getString("description") ?: "",
                            imageUrl = document.getString("imageUrl") ?: "",
                            createdAt = document.getLong("createdAt") ?: System.currentTimeMillis(),
                            userId = document.getString("userId") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                recipes.clear()
                recipes.addAll(updatedRecipes)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Recipes") },
                actions = {
                    IconButton(onClick = { navController.navigate("myRecipes") }) {
                        Text("My Recipes")
                    }
                    IconButton(onClick = {
                        auth.signOut()
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(recipes) { recipe ->
                ReadOnlyRecipeCard(recipe)
            }
        }
    }
}

@Composable
fun ReadOnlyRecipeCard(recipe: RecipeEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.h6)
            Text(recipe.description)

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
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }
        }
    }
}
