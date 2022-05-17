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
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import it.polito.mad.g01_timebanking.Skill
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.adapters.SkillDetails
import java.io.ByteArrayOutputStream


import java.io.File

class ProfileViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageRef = Firebase.storage.reference
    private val auth = Firebase.auth

    private lateinit var userInfoListener: ListenerRegistration
    private lateinit var suggestedSkillsListener: ListenerRegistration

    // Initialization placeholder variable
    private var _user = UserInfo()

    // This variable contains user info synchronized with the database
    private val pvtUser = MutableLiveData<UserInfo>().also {
        // Initial values, then database query will arise from activity
        it.value = _user
        // Retrieve user info from database
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

    var tmpPicturePath: String = ""

    val profilePicturePath: LiveData<String> = pvtProfilePicturePath

    private var tmpSkills: MutableSet<String> = _user.skills.toMutableSet()

    private val pvtSkills = MutableLiveData<MutableSet<String>>().also {
        it.value = tmpSkills
    }
    val skills: LiveData<MutableSet<String>> = pvtSkills

    private var tmpSuggestedSkills: MutableSet<Skill> =
        _user.skills.map { Skill(name = it) }.toMutableSet()

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
        tmpSkills = userInfo.skills.toMutableSet()
        pvtUser.value = userInfo
        pvtFullName.value = userInfo.fullName
        pvtNickname.value = userInfo.nickname
        pvtEmail.value = userInfo.email
        pvtLocation.value = userInfo.location
        pvtBiography.value = userInfo.biography
        pvtSkills.value = userInfo.skills.toMutableSet()

    }

    fun updatePhoto(newProfilePicturePath: String, imageView: ImageView) {
        tmpPicturePath = newProfilePicturePath
        uploadPhoto(imageView)
        val u = UserInfo(
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
        addOrUpdateSkills(toBeSaved.skills)
        db.collection("users").document(auth.currentUser!!.uid).set(toBeSaved)
            .addOnSuccessListener {
                Log.d("InsertOrUpdateUserInfo", "Success: $it")
                pvtUser.value = toBeSaved
                _user = toBeSaved
            }
            .addOnFailureListener {
                Log.d("InsertOrUpdateUserInfo", "Exception: ${it.message}")
                Toast.makeText(
                    a.applicationContext,
                    "Failed updating data. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    fun addOrUpdateSkills(newUserSkillsName: MutableList<String>) {
        val oldUser = _user

        val addedSkills = newUserSkillsName.toSet() subtract oldUser.skills
        val removedSkills = oldUser.skills subtract newUserSkillsName.toSet()
        Log.d("UserSkills", "removed skills: $removedSkills and added skills: $addedSkills")
        /* take all users */
        addedSkills.forEach { addedSkill ->
            db.collection("suggestedSkills").document(addedSkill).get().addOnSuccessListener {
                var addedSkillDoc = db.collection("suggestedSkills").document(addedSkill)

                addedSkillDoc.get()
                    .addOnSuccessListener {

                        if (it.exists())
                        //se lo trovo faccio l'update incrementando il contatore
                            addedSkillDoc.update("usage_in_user", FieldValue.increment(1))
                        else
                        //nuovo doc con contatori 1 0
                            addedSkillDoc.set(SkillDetails(addedSkill, usageInUser = 1L))
                    }
                    .addOnFailureListener {
                        Log.d("UpdateSkillUsageUser", "Exception: ${it.message}")
                    }
            }
        }
        removedSkills.forEach { removedSkill ->
            db.collection("suggestedSkills").document(removedSkill).get().addOnSuccessListener {
                var removedSkillDoc = db.collection("suggestedSkills").document(removedSkill)
                removedSkillDoc.get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            if (it["usage_in_user"] as Long <= 1L && it["usage_in_adv"] as Long <= 0L)
                                db.collection("suggestedSkills").document(removedSkill).delete()
                            else
                                removedSkillDoc.set(SkillDetails(removedSkill, usageInUser = -1L))
                        } else
                            Log.d(
                                "UpdateSkillUsageUser",
                                "Removing an unexisting skill $removedSkill"
                            )
                    }

            }
        }
        //TODO: usare update merge
    }


    private fun getSuggestedSkills() {

        suggestedSkillsListener = db.collection("suggestedSkills")
            .addSnapshotListener { value, error ->
                if (error == null && value != null) {
                    pvtSuggestedSkills.value = value.documents.map { it.toSkill() }.toMutableSet()
                }
            }
    }

    fun getUserInfo() {
        userInfoListener = db.collection("users").document(auth.currentUser!!.uid)
            .addSnapshotListener { v, e ->
                if (e == null && v?.exists() == true) {
                    Log.d("UserInfo_Listener", "Data found on database. Updating!")
                    pvtUser.value = v.toUserInfo()
                    _user = v.toUserInfo()
                    Log.d(
                        "User_Picture",
                        "_user pp = ${_user.profilePicturePath} , newProfPic = ${tmpPicturePath}"
                    )
                    if (_user.profilePicturePath != tmpPicturePath)
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
        if (imageView.width == 0 || imageView.height == 0)
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
            if (it.items.contains(userPicRef)) {
                val maximumSizeOneMegabyte: Long = 1024 * 1024

                userPicRef.getBytes(maximumSizeOneMegabyte).addOnSuccessListener {
                    Log.d("PICTURE_DOWNLOAD", "Successfully downloaded picture")

                    val localFile = File.createTempFile("images", ".jpg")
                    localFile.writeBytes(it)

                    Log.d("PICTURE_DOWNLOAD", "Path file: ${localFile.absolutePath}")
                    tmpPicturePath = localFile.absolutePath
                    pvtProfilePicturePath.value = localFile.absolutePath
                }.addOnFailureListener {
                    // Handle any errors
                    Log.d("PICTURE_DOWNLOAD", "Failed downloading picture: ${it.message}")
                    pvtProfilePicturePath.value = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
                }
            } else {
                Log.d("PICTURE_DOWNLOAD", "No picture on database")
                tmpPicturePath = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
                pvtProfilePicturePath.value = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
            }
        }
    }

    override fun onCleared() {
        userInfoListener.remove()
    }
}

private fun DocumentSnapshot.toUserInfo(): UserInfo {
    return this.toObject(UserInfo::class.java) ?: UserInfo()
}

private fun DocumentSnapshot.toSkill(): Skill {
    return this.toObject(Skill::class.java) ?: Skill()
}