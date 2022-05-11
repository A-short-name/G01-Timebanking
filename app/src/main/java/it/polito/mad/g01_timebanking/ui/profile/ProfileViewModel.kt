package it.polito.mad.g01_timebanking.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
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
import it.polito.mad.g01_timebanking.UserKey
import java.io.ByteArrayOutputStream


import java.io.File

class ProfileViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageRef = Firebase.storage.reference
    private val auth = Firebase.auth

    private lateinit var userInfoListener: ListenerRegistration

    // Initialization placeholder variable
    private var _user = UserInfo()

    // This variable contains user info synchronized with the database
    private val pvtUser = MutableLiveData<UserInfo>().also {
        // Initial values, then database query will arise from activity
        it.value = _user
    }
    val user: LiveData<UserInfo> = pvtUser

    /* Ephemeral variables used from the Edit fragment to handle temporary save */

    private val pvtFullName = MutableLiveData<String>().also {
        it.value = _user.fullName
    }
    val fullName: LiveData<String> = pvtFullName

    private val pvtNickname = MutableLiveData<String>().also {
        it.value = _user.nickname
    }
    val nickname: LiveData<String> = pvtNickname

    private val pvtEmail = MutableLiveData<String>().also {
        it.value = _user.email
    }
    val email: LiveData<String> = pvtEmail

    private val pvtBiography = MutableLiveData<String>().also {
        it.value = _user.biography
    }
    val biography: LiveData<String> = pvtBiography

    private val pvtLocation = MutableLiveData<String>().also {
        it.value = _user.location
    }
    val location: LiveData<String> = pvtLocation

    private val pvtProfilePicturePath = MutableLiveData<String>().also {
        it.value = ""
    }
    val profilePicturePath: LiveData<String> = pvtProfilePicturePath

    private var tmpSkills: MutableSet<String> = _user.skills.toMutableSet()

    private val pvtSkills = MutableLiveData<MutableSet<String>>().also {
        it.value = tmpSkills
    }
    val skills: LiveData<MutableSet<String>> = pvtSkills


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
        return if (tmpSkills.add(skillText)) {
            pvtSkills.value = tmpSkills; true
        } else false
    }

    fun setUserInfo(userInfo: UserInfo) {
        _user = userInfo
        pvtUser.value = userInfo
        pvtFullName.value = userInfo.fullName
        pvtNickname.value = userInfo.nickname
        pvtEmail.value = userInfo.email
        pvtLocation.value = userInfo.location
        pvtBiography.value = userInfo.biography
        pvtSkills.value = userInfo.skills.toMutableSet()
    }

    fun updatePhoto(newProfilePicturePath: String, imageView: ImageView) {
        uploadPhoto(imageView)
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

    fun addOrUpdateData(toBeSaved: UserInfo) {
        db.collection("users").document(auth.currentUser!!.uid).set(toBeSaved)
            .addOnSuccessListener {
                Log.d("InsertOrUpdateUserInfo", "Success: $it")
                pvtUser.value = toBeSaved
                _user = toBeSaved
            }
            .addOnFailureListener {
                Log.d("InsertOrUpdateUserInfo", "Exception: ${it.message}")
                Toast.makeText(a.applicationContext,"Failed updating data. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getUserInfo() {
        userInfoListener = db.collection("users").document(auth.currentUser!!.uid)
            .addSnapshotListener { v, e ->
                if (e == null && v?.exists() == true) {
                    Log.d("UserInfo_Listener", "Data found on database. Updating!")
                    pvtUser.value = v.toUserInfo()
                    _user = v.toUserInfo()
                    downloadPhoto()
                } else if (e == null) {
                    Log.d("UserInfo_Listener", "Data not found on database. Setting new user info")
                    val newUser = UserInfo().apply {
                        email = auth.currentUser!!.email.toString()
                        fullName = auth.currentUser!!.displayName.toString()
                    }
                    addOrUpdateData(newUser)
                }
            }
    }

    fun uploadPhoto(imageView: ImageView) {
        // Get the data from an ImageView as bytes
        if(imageView.width == 0 || imageView.height == 0)
            return

        val bitmap = Bitmap.createBitmap(imageView.width, imageView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.draw(canvas)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val userPicRef = storageRef.child("images/${auth.currentUser!!.uid}.jpg")
        val uploadTask = userPicRef.putBytes(data)

        uploadTask
            .addOnSuccessListener {
                Log.d("PICTURE_UPLOAD", "Successfully updated picture")
            }
            .addOnFailureListener {
                Log.d("PICTURE_UPLOAD", "Failed to upload picture")
            }
    }

    private fun downloadPhoto() {
        Log.d("PICTURE_DOWNLOAD", "Started download function")
        val imagesRef = storageRef.child("images/")
        val userPicRef = imagesRef.child("${auth.currentUser!!.uid}.jpg")

        // Check if file exists
        imagesRef.listAll().addOnSuccessListener {
            // If file exists download it
            if(it.items.contains(userPicRef)) {
                val maximumSizeOneMegabyte: Long = 1024 * 1024

                userPicRef.getBytes(maximumSizeOneMegabyte).addOnSuccessListener {
                    Log.d("PICTURE_DOWNLOAD", "Successfully downloaded picture")

                    val localFile = File.createTempFile("images", ".jpg")
                    localFile.writeBytes(it)

                    Log.d("PICTURE_DOWNLOAD", "Path file: ${localFile.absolutePath}")
                    pvtProfilePicturePath.value = localFile.absolutePath
                }.addOnFailureListener {
                    // Handle any errors
                    Log.d("PICTURE_DOWNLOAD", "Failed downloading picture: ${it.message}")
                    pvtProfilePicturePath.value = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
                }
            }else{
                Log.d("PICTURE_DOWNLOAD", "No picture on database")
                pvtProfilePicturePath.value = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
            }
        }
    }

    fun clear() {
        userInfoListener.remove()
    }
}

private fun DocumentSnapshot.toUserInfo(): UserInfo {
    return this.toObject(UserInfo::class.java) ?: UserInfo()
}