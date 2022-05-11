package it.polito.mad.g01_timebanking.ui.profile

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.g01_timebanking.Skill
import it.polito.mad.g01_timebanking.UserInfo
import java.io.ByteArrayOutputStream


import java.io.File

class ProfileViewModel(a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageRef = Firebase.storage.reference
    private val auth = Firebase.auth

    private lateinit var userInfoListener: ListenerRegistration
    private lateinit var suggestedSkillsListener: ListenerRegistration

    // Initialization placeholder variable
    private var _user = UserInfo()

    // This variable contains user info synchronized with the database
    private val pvtUser = MutableLiveData<UserInfo>().also {
        // Initial values, then database query
        it.value = _user
        getUserInfo()
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

    private var tmpSuggestedSkills: MutableSet<Skill> = _user.skills.map{Skill(name = it)}.toMutableSet()

    private val pvtSuggestedSkills = MutableLiveData<MutableSet<Skill>>().also {
        it.value = tmpSuggestedSkills
        getSuggestedSkills()
    }
    val suggestedSkills: LiveData<MutableSet<Skill>> = pvtSuggestedSkills

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

    fun addOrUpdateData(user: UserInfo) {
        addOrUpdateSkills(user.skills)
        insertOrUpdateUserInfo(user)
        pvtUser.value = user
        _user = user
    }

    fun addOrUpdateSkills(newUserSkillsName :MutableList<String>){
        var oldUser = db.collection("users").document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                val oldUser = it.toUserInfo()
                for (oldUserSkillName in oldUser.skills) {
                    if(! newUserSkillsName.contains(oldUserSkillName))
                        decrementUsageInUserSkill(oldUserSkillName)

                }
                for (newUserSkillName in newUserSkillsName) {
                    if(! oldUser.skills.contains(newUserSkillName))
                        insertOrincrementUsageInUserSkill(newUserSkillName)
                }
            }

    }

    private fun insertOrincrementUsageInUserSkill(newUserSkillName: String) {
        TODO("Not yet implemented, like the decrement it should search for the old skill if present update the usage," +
                " if not present a new skill should be create")
    }

    fun decrementUsageInUserSkill(skillName : String){
        db.collection("suggestedSkills").document(skillName).get().addOnSuccessListener { oldSkillFromDb ->
            var tempSkill = oldSkillFromDb.toSkill()
            tempSkill.usageInUser--
            db.collection("suggestedSkills").document(skillName).set(tempSkill).addOnSuccessListener {
                Log.d("UpdateSkillUsageUser", "Success: $it")
            }
                .addOnFailureListener {
                    Log.d("UpdateSkillUsageUser", "Exception: ${it.message}")
                }
        }
    }

    private fun getSuggestedSkills(){

        suggestedSkillsListener = db.collection("suggestedSkills")
            .addSnapshotListener { value, error ->
                if (error == null && value != null){
                    pvtSuggestedSkills.value = value.documents.map { it.toSkill() }.toMutableSet()
                }
            }
    }

    private fun getUserInfo() {
        userInfoListener = db.collection("users").document(auth.currentUser!!.uid)
            .addSnapshotListener { v, e ->
                if (e == null && v?.exists() == true) {
                    Log.d("UserInfo_Listener", "Data found on database. Updating!")
                    pvtUser.value = v.toUserInfo()
                } else if (e == null) {
                    Log.d("UserInfo_Listener", "Data not found on database. Setting new user info")
                    val newUser = UserInfo().apply {
                        email = auth.currentUser!!.email.toString()
                        downloadPhoto()
                        fullName = auth.currentUser!!.displayName.toString()
                    }

                    insertOrUpdateUserInfo(newUser)
                    pvtUser.value = newUser
                }
            }
    }

    private fun insertOrUpdateUserInfo(toBeSaved: UserInfo) {
        db.collection("users").document(auth.currentUser!!.uid).set(toBeSaved)
            .addOnSuccessListener {
                Log.d("InsertOrUpdateUserInfo", "Success: $it")
            }
            .addOnFailureListener {
                Log.d("InsertOrUpdateUserInfo", "Exception: ${it.message}")
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
        val userPicRef = storageRef.child("images/${auth.currentUser!!.uid}.jpg")

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
        }
    }

    fun clear() {
        userInfoListener.remove()
    }
}

private fun DocumentSnapshot.toUserInfo(): UserInfo {
    return this.toObject(UserInfo::class.java) ?: UserInfo()
}

private fun DocumentSnapshot.toSkill(): Skill {
    return this.toObject(Skill::class.java) ?: Skill()
}