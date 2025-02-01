package recipe

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

suspend fun uploadImageToFirebase(storage: FirebaseStorage, imageUri: Uri): String {
    return try {
        val storageRef = storage.reference.child("recipe_images/${imageUri.lastPathSegment}")
        storageRef.putFile(imageUri).await()
        storageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

