package com.example.recipeshare.ui.recipe

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.recipeshare.local.RecipeDao
import com.example.recipeshare.local.RecipeEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import com.example.recipeshare.R


@Composable
fun CreateRecipeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title input
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) Text("Enter title")
                innerTextField()
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Description input
        BasicTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (description.isEmpty()) Text("Enter description")
                innerTextField()
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Image picker preview
        if (imageUri != null) {
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        Picasso.get()
                            .load(imageUri.toString())
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
        } else {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Select Image")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Upload & Save Recipe
        Button(
            onClick = {
                if (title.isNotEmpty() && description.isNotEmpty()) {
                    isUploading = true
                    coroutineScope.launch {
                        try {
                            val imageUrl = uploadImageToFirebase(storage, imageUri)
                            val newRecipe = RecipeEntity(
                                title = title,
                                description = description,
                                imageUrl = imageUrl,
                                createdAt = System.currentTimeMillis()
                            )

                            // Save to local database (Room)
                            withContext(Dispatchers.IO) {
                                recipeDao.insertRecipes(listOf(newRecipe))
                            }

                            // Save to Firestore
                            saveRecipeToFirestore(db, newRecipe)

                            // Navigate back to home screen
                            withContext(Dispatchers.Main) {
                                isUploading = false
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            withContext(Dispatchers.Main) {
                                isUploading = false
                            }
                        }
                    }
                }
            },
            enabled = !isUploading
        ) {
            Text(if (isUploading) "Uploading..." else "Save Recipe")
        }
    }
}

// Upload image to Firebase Storage and return the image URL
suspend fun uploadImageToFirebase(storage: FirebaseStorage, uri: Uri?): String {
    if (uri == null) throw IllegalArgumentException("Image URI cannot be null")
    val fileName = UUID.randomUUID().toString() // Generate a unique file name
    val storageRef = storage.reference.child("recipes/$fileName")
    storageRef.putFile(uri).await() // Upload file to Firebase Storage
    return storageRef.downloadUrl.await().toString() // Get the download URL
}

// Save recipe data to Firestore
suspend fun saveRecipeToFirestore(
    db: FirebaseFirestore,
    recipe: RecipeEntity
) {
    val recipeMap = mapOf(
        "title" to recipe.title,
        "description" to recipe.description,
        "imageUrl" to recipe.imageUrl,
        "createdAt" to recipe.createdAt
    )
    db.collection("recipes").add(recipeMap).await()
}


