package com.example.recipeshare.ui.recipe

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import recipe.uploadImageToFirebase


@Composable
fun EditRecipeScreen(navController: NavController, recipeId: String, recipeDao: RecipeDao) {
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Fetch existing recipe data
    LaunchedEffect(recipeId) {
        val recipe = recipeDao.getRecipeById(recipeId)
        recipe?.let {
            title = it.title
            description = it.description
            imageUrl = it.imageUrl
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Recipe") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("myRecipes") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to My Recipes")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (imageUri != null || imageUrl.isNotEmpty()) {
                Image(
                    painter = rememberImagePainter(imageUri ?: imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Change Image")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        isSaving = true

                        val newImageUrl = imageUri?.let {
                            uploadImageToFirebase(storage, it)
                        } ?: imageUrl

                        val updatedRecipe = RecipeEntity(
                            id = recipeId,
                            title = title,
                            description = description,
                            imageUrl = newImageUrl,
                            createdAt = System.currentTimeMillis(),
                            userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        )

                        try {
                            // Update Firestore
                            db.collection("recipes").document(recipeId)
                                .set(updatedRecipe)
                                .await()

                            // Update Room Database
                            recipeDao.updateRecipe(updatedRecipe)

                            withContext(Dispatchers.Main) {
                                isSaving = false
                                navController.navigate("myRecipes") // Navigate back after saving
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Saving..." else "Save Changes")
            }
        }
    }
}





