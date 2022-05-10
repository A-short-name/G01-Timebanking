package it.polito.mad.g01_timebanking.repositories

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.g01_timebanking.UserInfo

class FirebaseRepository(val a: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var l : ListenerRegistration

    fun getUserInfo() : UserInfo {
        var userInfo : UserInfo? = null
        l = db.collection("users").document("userInfo")
            .addSnapshotListener{ v, e ->
                if(e==null) {
                    userInfo = v!!.toUserInfo()
                }
            }
        Log.d("Firebase", "Called getUserInfo() method")
        return userInfo ?: UserInfo()
    }

    fun create() {
        db.collection("users").document().set(mapOf("key" to "value"))
            .addOnSuccessListener { it ->
                Log.d("Firebase","Success ${it.toString()}")
            }
            .addOnFailureListener{
                Log.d("Firebase", "Exception: ${it.message}")
            }
    }

    fun clear() {
        l.remove()
    }
}

private fun DocumentSnapshot.toUserInfo(): UserInfo {
    return try {
        val fullName = get("fullname").toString()
        val nickname = get("nickname").toString()
        val email = get("email").toString()
        val location = get("location").toString()
        val biography = get("biography").toString()
        val profilePicturePath = get("profilePicturePath").toString()
        val skills : MutableSet<String> = (get("skills") as List<*>).map{it -> it.toString()}.toMutableSet()

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
        return UserInfo()
    }
}
