package it.polito.mad.g01_timebanking.repositories

import android.app.Application
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

class FirebaseRepository(val a: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getUserInfo() : UserInfo {
        val l: ListenerRegistration = db.collection("users").document("userInfo")
            .addSnapshotListener{ v, e ->
                if(e==null) {
                    return v!!.toUserInfo()
                }
            }
        return UserInfo()
    }
}

private fun DocumentSnapshot.toUserInfo(): UserInfo {
    return try {
        val fullName = get("fullname")
        var nickname = get("nickname")
        var email = get("email")
        var location = get("location")
        var biography = get("biography")
        var profilePicturePath = get("profilePicturePath")
        var skills = get("skills")

        UserInfo(). apply {
            fullName,nickname,email,location,biography,profilePicturePath,skills
        }
    }catch(ex: Exception) {

    }
}
