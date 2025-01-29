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
import com.example.recipeshare.R
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val recipes = remember { mutableStateListOf<RecipeEntity>() }
    val coroutineScope = rememberCoroutineScope()

    // Load recipes from Room and sync with Firestore
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            val localRecipes = recipeDao.getAllRecipes()
            withContext(Dispatchers.Main) {
                recipes.clear()
                recipes.addAll(localRecipes)
            }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipe Share") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
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
                RecipeCard(recipe)
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: RecipeEntity) {
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

            recipe.imageUrl?.let {
                Spacer(modifier = Modifier.height(8.dp))
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Picasso.get()
                                .load(it)
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
        }
    }
}

