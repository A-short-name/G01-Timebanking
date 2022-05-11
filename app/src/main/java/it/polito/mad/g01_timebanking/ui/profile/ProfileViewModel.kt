package it.polito.mad.g01_timebanking.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.repositories.PreferencesRepository


import java.io.File

class ProfileViewModel(a: Application): AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private lateinit var l : ListenerRegistration

    private val storageRef = Firebase.storage.reference

    // Initialization placeholder variable
    private var _user = UserInfo()

    // This variable contains user info synchronized with the database
    private val pvtUser = MutableLiveData<UserInfo>().also {
        // Initial values, then database query
        it.value = _user
        getUserInfo()
    }
    val user : LiveData<UserInfo> = pvtUser

    /* Ephemeral variables used from the Edit fragment to handle temporary save */

    private val pvtFullName = MutableLiveData<String>().also {
        it.value = _user.fullName
    }
    val fullName : LiveData<String> = pvtFullName

    private val pvtNickname = MutableLiveData<String>().also {
        it.value = _user.nickname
    }
    val nickname : LiveData<String> = pvtNickname

    private val pvtEmail = MutableLiveData<String>().also {
        it.value = _user.email
    }
    val email : LiveData<String> = pvtEmail

    private val pvtBiography = MutableLiveData<String>().also {
        it.value = _user.biography
    }
    val biography : LiveData<String> = pvtBiography

    private val pvtLocation = MutableLiveData<String>().also {
        it.value = _user.location
    }
    val location : LiveData<String> = pvtLocation

    private val pvtProfilePicturePath = MutableLiveData<String>().also {
        it.value = _user.profilePicturePath
    }
    val profilePicturePath : LiveData<String> = pvtProfilePicturePath

    private var tmpSkills : MutableSet<String> = _user.skills.toMutableSet()

    private val pvtSkills = MutableLiveData<MutableSet<String>>().also {
        it.value = tmpSkills
    }
    val skills :LiveData<MutableSet<String>> = pvtSkills


    fun setFullname(fullname: String) {
        pvtFullName.value = fullname
    }

    fun setNickname(nickname: String) {
        pvtNickname.value = nickname
    }

    fun setEmail(email: String) {
        pvtEmail.value = email
    }

    fun setLocation(location: String) {
        pvtLocation.value = location
    }

    fun setBiography(biography: String) {
        pvtBiography.value = biography
    }

    fun setProfilePicturePath(profilePicturePath: String) {
        pvtProfilePicturePath.value = profilePicturePath
    }

    fun removeSkill(skillText: String) {
        tmpSkills.remove(skillText)
        pvtSkills.value = tmpSkills
    }

    fun tryToAddSkill(skillText: String): Boolean {
        return if(tmpSkills.add(skillText)){ pvtSkills.value = tmpSkills; true } else false
    }

    fun setUserInfo(userInfo: UserInfo) {
        _user = userInfo
        pvtUser.value = userInfo
        pvtFullName.value = userInfo.fullName
        pvtNickname.value = userInfo.nickname
        pvtEmail.value = userInfo.email
        pvtLocation.value = userInfo.location
        pvtBiography.value = userInfo.biography
        pvtProfilePicturePath.value = userInfo.profilePicturePath
        pvtSkills.value = userInfo.skills.toMutableSet()
    }

    fun addOrUpdateData(user: UserInfo) {
        insertOrUpdateUserInfo(user)
        pvtUser.value = user
        _user = user
    }

    fun updatePhoto(newProfilePicturePath: String) {
        uploadPhoto(newProfilePicturePath)
        val u = UserInfo (
            fullName = _user.fullName,
            nickname = _user.nickname,
            email = _user.email,
            location = _user.location,
            biography = _user.biography,
            profilePicturePath = newProfilePicturePath,
            skills = _user.skills
        )
        addOrUpdateData(u)
    }

    private fun getUserInfo() {
        l = db.collection("users").document(auth.currentUser!!.uid)
            .addSnapshotListener{ v, e ->
                if (v != null) {
                    if(e==null && v.exists()) {
                        Log.d("TESTING","Setting user info")
                        setUserInfo(v.toUserInfo())
                    } else {
                        Log.d("TESTING","Setting new user info")
                        val newUser = UserInfo().apply {
                            email = auth.currentUser!!.email.toString()
                            //profilePicturePath = auth.currentUser!!.photoUrl.toString()
                            fullName = auth.currentUser!!.displayName.toString()
                        }

                        insertOrUpdateUserInfo(newUser)
                        setUserInfo(newUser)
                    }
                }
            }
    }

    private fun insertOrUpdateUserInfo(toBeSaved: UserInfo) {
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

    private fun uploadPhoto(profilePicturePath: String) {
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
            setProfilePicturePath(localFile.absolutePath)
        }.addOnFailureListener{
            Log.d("DOWNLOAD_PICTURE", "Failed downloading picture")
        }
    }

    fun clear() {
        l.remove()
    }
}

private fun DocumentSnapshot.toUserInfo(): UserInfo {
    return this.toObject(UserInfo::class.java) ?: UserInfo()
}