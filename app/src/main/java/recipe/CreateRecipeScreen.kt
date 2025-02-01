package com.example.recipeshare.ui.recipe

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import recipe.uploadImageToFirebase

@Composable
fun CreateRecipeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Recipe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // ✅ Go Back to My Recipes
                        Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Recipe Title") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("Description") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (imageUri != null) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Picasso.get().load(imageUri.toString()).fit().centerCrop().into(this)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            } else {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Select Image")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val userId = auth.currentUser?.uid ?: return@Button
                    isUploading = true

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val imageUrl = imageUri?.let { uploadImageToFirebase(storage, it) }
                            val recipeId = db.collection("recipes").document().id

                            val newRecipe = RecipeEntity(
                                id = recipeId,
                                title = title,
                                description = description,
                                imageUrl = imageUrl ?: "",
                                createdAt = System.currentTimeMillis(),
                                userId = userId
                            )

                            recipeDao.insertRecipes(listOf(newRecipe))
                            db.collection("recipes").document(recipeId).set(newRecipe).await()

                            withContext(Dispatchers.Main) {
                                isUploading = false
                                navController.popBackStack() // ✅ Go Back to My Recipes After Saving
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isUploading = false
                        }
                    }
                },
                enabled = !isUploading
            ) {
                Text(if (isUploading) "Uploading..." else "Save Recipe")
            }
        }
    }
}




