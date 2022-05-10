package it.polito.mad.g01_timebanking.repositories

import android.app.Application
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.ui.profile.ProfileViewModel
import java.io.File

class FirebaseRepository(val a: Application, val profilevm: ProfileViewModel) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var l : ListenerRegistration
    private val auth = Firebase.auth
    private val storageRef = Firebase.storage.reference
    private val imagesRef = storageRef.child("userImages")

    fun getUserInfo() {
        l = db.collection("users").document(auth.currentUser!!.uid)
            .addSnapshotListener{ v, e ->
                if (v != null) {
                    if(e==null && v.exists()) {
                        Log.d("TESTING","Setting user info")
                        profilevm.setUserInfo(v.toUserInfo())
                    } else {
                        Log.d("TESTING","Setting new user info")
                        val newUser = UserInfo().apply {
                            email = auth.currentUser!!.email.toString()
                            //profilePicturePath = auth.currentUser!!.photoUrl.toString()
                            fullName = auth.currentUser!!.displayName.toString()
                        }

                        insertOrUpdateUserInfo(newUser)
                        profilevm.setUserInfo(newUser)
                    }
                }
            }
    }

    fun insertOrUpdateUserInfo(toBeSaved: UserInfo) {
        db.collection("users").document(auth.currentUser!!.uid)
            .set(mapOf(
                "fullName" to toBeSaved.fullName,
                "nickname" to toBeSaved.nickname,
                "email" to toBeSaved.email,
                "biography" to toBeSaved.biography,
                "profilePicturePath" to toBeSaved.profilePicturePath,
                "location" to toBeSaved.location,
                "skills" to toBeSaved.skills.toList()
            ))
            .addOnSuccessListener { it ->
                Log.d("Firebase","Success ${it.toString()}")
            }
            .addOnFailureListener{
                Log.d("Firebase", "Exception: ${it.message}")
            }
    }

    fun uploadPhoto(profilePicturePath: String) {
        val file = Uri.fromFile(File(profilePicturePath))
        val riversRef = storageRef.child("images/${auth.currentUser!!.uid}.jpg")
        val uploadTask = riversRef.putFile(file)

        uploadTask
            .addOnSuccessListener {
                Log.d("PICTURE_UPLOAD","Successfully updated picture")
            }
            .addOnFailureListener{
                Log.d("PICTURE_UPLOAD","Failed to upload picture")
            }
    }

    fun downloadPhoto() {
        val riversRef = storageRef.child("images/${auth.currentUser!!.uid}.jpg")
        val localFile = File.createTempFile("images","jpg")

        riversRef.getFile(localFile).addOnSuccessListener {
            profilevm.setProfilePicturePath(localFile.absolutePath)
        }.addOnFailureListener{
            Log.d("DOWNLOAD_PICTURE", "Failed downloading picture")
        }
    }

    fun clear() {
        l.remove()
    }
}

private fun DocumentSnapshot.toUserInfo(): UserInfo {
    return try {
        val fullName = get("fullName").toString()
        val nickname = get("nickname").toString()
        val email = get("email").toString()
        val location = get("location").toString()
        val biography = get("biography").toString()
        val profilePicturePath = get("profilePicturePath").toString()
        val skills : MutableSet<String> = (get("skills") as List<*>).map{it -> it.toString()}.toMutableSet()

        Log.d("TESTING","Inside to user, email is $email")
        UserInfo().apply {
            this.fullName = fullName
            this.nickname = nickname
            this.email = email
            this.location = location
            this.biography = biography
            this.profilePicturePath = profilePicturePath
            this.skills = skills
        }
    }catch(ex: Exception) {
        Log.d("TESTING","Exception while toUser: ${ex.message}")
        return UserInfo()
    }
}
