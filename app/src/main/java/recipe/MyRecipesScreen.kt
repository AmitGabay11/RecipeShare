package recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun MyRecipesScreen(navController: NavController, recipeDao: RecipeDao) {
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val recipes = remember { mutableStateListOf<RecipeEntity>() } // ✅ Mutable list

    LaunchedEffect(Unit) {
        db.collection("recipes").whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.let {
                    recipes.clear()
                    recipes.addAll(it.documents.mapNotNull { doc ->
                        try {
                            RecipeEntity(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                description = doc.getString("description") ?: "",
                                imageUrl = doc.getString("imageUrl") ?: "",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                userId = doc.getString("userId") ?: ""
                            )
                        } catch (e: Exception) {
                            null
                        }
                    })
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Recipes") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to Home")
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
            modifier = Modifier.padding(paddingValues).padding(16.dp)
        ) {
            items(recipes) { recipe ->
                MyRecipeCard(recipe, recipeDao, recipes, navController, db) // ✅ Pass db
            }
        }
    }
}

@Composable
fun MyRecipeCard(
    recipe: RecipeEntity,
    recipeDao: RecipeDao,
    recipes: MutableList<RecipeEntity>, // ✅ MutableList allows removal
    navController: NavController,
    db: FirebaseFirestore
) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipe.title, style = MaterialTheme.typography.h6)
            Text(recipe.description)

            if (recipe.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberImagePainter(recipe.imageUrl),
                    contentDescription = "Recipe Image",
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                db.collection("recipes").document(recipe.id).delete().await()
                                recipeDao.deleteRecipeById(recipe.id)
                                withContext(Dispatchers.Main) {
                                    recipes.removeAll { it.id == recipe.id } // ✅ Fix remove error
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    Text("Delete")
                }

                Button(onClick = { navController.navigate("editRecipe/${recipe.id}") }) {
                    Text("Edit")
                }
            }
        }
    }
}




