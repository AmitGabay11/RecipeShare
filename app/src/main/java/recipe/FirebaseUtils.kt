package recipe

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

suspend fun uploadImageToFirebase(storage: FirebaseStorage, uri: Uri): String {
    val storageRef = storage.reference.child("recipes/${uri.lastPathSegment}")
    storageRef.putFile(uri).await()
    return storageRef.downloadUrl.await().toString()
}
