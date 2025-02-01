package com.example.recipeshare.ui.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import recipe.uploadImageToFirebase

@Composable
fun MyProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var name by remember { mutableStateOf(auth.currentUser?.displayName ?: "") }
    var profileImageUrl by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    // ✅ Load Profile Data from Firestore
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                profileImageUrl = document.getString("profileImageUrl") ?: ""
                name = document.getString("name") ?: auth.currentUser?.displayName ?: ""
            } else {
                // First-time user -> create an empty profile
                db.collection("users").document(userId).set(
                    mapOf("name" to name, "profileImageUrl" to "")
                ).await()
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load profile."
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colors.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (profileImageUrl.isNotEmpty() || selectedImageUri != null) {
                Image(
                    painter = rememberImagePainter(selectedImageUri ?: profileImageUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Text("Change Profile Picture")
                }
            } else {
                Text(name, style = MaterialTheme.typography.h5)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                Button(
                    onClick = {
                        isSaving = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val userId = auth.currentUser?.uid ?: return@launch
                                val imageUrl = selectedImageUri?.let { uploadImageToFirebase(storage, it) } ?: profileImageUrl

                                val userUpdates = mapOf(
                                    "name" to name,
                                    "profileImageUrl" to imageUrl
                                )

                                db.collection("users").document(userId).set(userUpdates).await()

                                withContext(Dispatchers.Main) {
                                    isEditing = false
                                    isSaving = false
                                    profileImageUrl = imageUrl

                                    // ✅ Show Success Toast
                                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                isSaving = false
                                withContext(Dispatchers.Main) {
                                    errorMessage = "Failed to save profile."
                                }
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    Text(if (isSaving) "Saving..." else "Save Changes")
                }
            } else {
                Button(onClick = { isEditing = true }) {
                    Text("Edit Profile")
                }
            }
        }
    }
}
