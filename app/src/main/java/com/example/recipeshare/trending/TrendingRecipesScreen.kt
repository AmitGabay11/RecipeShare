package com.example.recipeshare.trending

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter

@Composable
fun TrendingRecipesScreen(navController: NavController) {
    val viewModel: TrendingRecipesViewModel = viewModel()
    val recipes by viewModel.recipes
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trending Recipes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = Color.Red)
            } else {
                LazyColumn {
                    items(recipes) { recipe ->
                        TrendingRecipeCard(recipe) { uriHandler.openUri(recipe.sourceUrl ?: "") }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingRecipeCard(recipe: com.example.recipeshare.api.Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = rememberImagePainter(recipe.image),
                contentDescription = recipe.title,
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(recipe.title, style = MaterialTheme.typography.h6)
            Text("Tap to View Recipe", color = MaterialTheme.colors.primary)
        }
    }
}
