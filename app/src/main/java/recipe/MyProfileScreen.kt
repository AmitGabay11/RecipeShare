package recipe

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
    var imageUrlInput by remember { mutableStateOf("") }
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
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start // ✅ Shifted "My Profile" More Left
                    ) {
                        Spacer(modifier = Modifier.width(56.dp)) // To compensate for the back button
                        Text("My Profile", fontWeight = FontWeight.Bold)
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
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // ✅ Center everything properly
        ) {
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colors.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // ✅ Profile Image (Centered)
            Image(
                painter = rememberImagePainter(selectedImageUri ?: profileImageUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(130.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ✅ Name (Centered, Better Styling)
            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Text(
                    text = name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ "Edit Profile" Button (Always Visible, Centered)
            if (!isEditing) {
                Button(onClick = { isEditing = true }) {
                    Text("Edit Profile")
                }
            }

            // ✅ Upload Options (Only Visible in Edit Mode)
            if (isEditing) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Upload Image")
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        label = { Text("Enter Image URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (imageUrlInput.isNotEmpty()) {
                                profileImageUrl = imageUrlInput
                            }
                        }
                    ) {
                        Text("Use URL")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ✅ "Save Changes" Button (Only Visible in Edit Mode)
                Button(
                    onClick = {
                        isSaving = true
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val userId = auth.currentUser?.uid ?: return@launch
                                val finalImageUrl = selectedImageUri?.toString() ?: profileImageUrl

                                val userUpdates = mapOf(
                                    "name" to name,
                                    "profileImageUrl" to finalImageUrl
                                )

                                // ✅ Update Firestore
                                db.collection("users").document(userId).set(userUpdates).await()

                                // ✅ Update FirebaseAuth display name
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .setPhotoUri(Uri.parse(finalImageUrl))
                                    .build()
                                auth.currentUser?.updateProfile(profileUpdates)?.await()

                                withContext(Dispatchers.Main) {
                                    isEditing = false
                                    isSaving = false
                                    profileImageUrl = finalImageUrl

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
            }
        }
    }
}


