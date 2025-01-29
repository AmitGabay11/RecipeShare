package com.example.recipeshare.ui.recipe

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import com.example.recipeshare.R
import kotlinx.coroutines.withContext

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
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Recipe") },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = MaterialTheme.colors.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
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
                        .height(200.dp)
                )
            } else {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Select Image")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val imageUrl = uploadImageToFirebase(storage, imageUri)
                            val newRecipe = RecipeEntity(
                                title = title,
                                description = description,
                                imageUrl = imageUrl,
                                createdAt = System.currentTimeMillis()
                            )
                            recipeDao.insertRecipes(listOf(newRecipe))
                            saveRecipeToFirestore(db, newRecipe)

                            withContext(Dispatchers.Main) { navController.popBackStack() }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                enabled = !isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isUploading) "Uploading..." else "Save Recipe")
            }
        }
    }
}

suspend fun uploadImageToFirebase(storage: FirebaseStorage, uri: Uri?): String {
    if (uri == null) throw IllegalArgumentException("Image URI cannot be null")
    val fileName = UUID.randomUUID().toString()
    val storageRef = storage.reference.child("recipes/$fileName")
    storageRef.putFile(uri).await()
    return storageRef.downloadUrl.await().toString()
}

suspend fun saveRecipeToFirestore(db: FirebaseFirestore, recipe: RecipeEntity) {
    val recipeMap = mapOf(
        "title" to recipe.title,
        "description" to recipe.description,
        "imageUrl" to recipe.imageUrl,
        "createdAt" to recipe.createdAt
    )
    db.collection("recipes").add(recipeMap).await()
}



