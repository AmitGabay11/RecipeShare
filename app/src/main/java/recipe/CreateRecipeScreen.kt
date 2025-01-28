package recipe

import android.widget.Toast
import androidx.compose.foundation.layout.*
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun CreateRecipeScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title Field
        TextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Recipe Title") },
            modifier = Modifier.fillMaxWidth()
        )

        // Description Field
        TextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        // Image URL Field
        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                isSubmitting = true

                // Prepare recipe data
                val recipe = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "author" to "Anonymous", // Add user info if needed
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                // Save to Firestore
                db.collection("recipes").add(recipe)
                    .addOnSuccessListener {
                        isSubmitting = false
                        Toast.makeText(context, "Recipe added!", Toast.LENGTH_SHORT).show()
                        navController.navigate("home")
                    }
                    .addOnFailureListener {
                        isSubmitting = false
                        Toast.makeText(context, "Failed to add recipe", Toast.LENGTH_SHORT).show()
                    }
            },
            enabled = !isSubmitting && title.isNotEmpty() && description.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = MaterialTheme.colors.onPrimary)
            } else {
                Text("Add Recipe")
            }
        }
    }
}
