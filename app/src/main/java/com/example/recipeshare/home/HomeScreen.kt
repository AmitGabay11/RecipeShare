package com.example.recipeshare.home

import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.recipeshare.R
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val recipes = remember { mutableStateListOf<RecipeEntity>() }
    val coroutineScope = rememberCoroutineScope()
    var listener: ListenerRegistration? by remember { mutableStateOf(null) }

    var userName by remember { mutableStateOf(auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Guest") }

    LaunchedEffect(Unit) {
        auth.currentUser?.uid?.let { userId ->
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    userName = document.getString("name") ?: userName
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("All Recipes")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("auth") { popUpTo("home") { inclusive = true } }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                },
                actions = {
                    // ✅ "Star" Button (Left of "My Recipes")
                    IconButton(onClick = { navController.navigate("trendingRecipes") }) {
                        Icon(Icons.Default.Star, contentDescription = "Trending Recipes")
                    }


                    // ✅ "My Recipes" Button (Right of "Star")
                    IconButton(onClick = { navController.navigate("myRecipes") }) {
                        Icon(Icons.Default.Book, contentDescription = "My Recipes")
                    }

                    // ✅ Profile Button (On the Right)
                    IconButton(onClick = { navController.navigate("myProfile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ Enhanced Greeting UI
            Text(
                text = "Hello, $userName!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Welcome to Recipe Share",
                fontSize = 16.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(recipes) { recipe ->
                    ReadOnlyRecipeCard(recipe)
                }
            }
        }
    }
}

// ✅ Unchanged - Displays Recipe Cards
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



