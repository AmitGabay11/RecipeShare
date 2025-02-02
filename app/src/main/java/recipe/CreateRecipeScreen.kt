package recipe

import android.net.Uri
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun CreateRecipeScreen(navController: NavController, recipeDao: RecipeDao) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var showUrlInput by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart // 🔥 Moves title slightly left
                    ) {
                        Text(
                            text = "Create Recipe",
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 20.dp) // ✅ Small left shift
                        )
                    }
                },
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
                .padding(paddingValues)
                .padding(16.dp),
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

            if (imageUri != null || imageUrl.isNotEmpty()) {
                AndroidView(
                    factory = { context ->
                        ImageView(context).apply {
                            Picasso.get()
                                .load(imageUri?.toString() ?: imageUrl)
                                .fit()
                                .centerCrop()
                                .into(this)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Upload Image from Device")
                }
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { showUrlInput = !showUrlInput }) {
                    Text(if (showUrlInput) "Hide URL Input" else "Enter Image URL")
                }

                if (showUrlInput) {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val userId = auth.currentUser?.uid ?: return@Button
                    isUploading = true

                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            val finalImageUrl = imageUri?.let { uploadImageToFirebase(storage, it) } ?: imageUrl
                            val recipeId = db.collection("recipes").document().id

                            val newRecipe = RecipeEntity(
                                id = recipeId,
                                title = title,
                                description = description,
                                imageUrl = finalImageUrl,
                                createdAt = System.currentTimeMillis(),
                                userId = userId
                            )

                            recipeDao.insertRecipes(listOf(newRecipe))
                            db.collection("recipes").document(recipeId).set(newRecipe).await()

                            withContext(Dispatchers.Main) {
                                isUploading = false
                                navController.popBackStack()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            isUploading = false
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



