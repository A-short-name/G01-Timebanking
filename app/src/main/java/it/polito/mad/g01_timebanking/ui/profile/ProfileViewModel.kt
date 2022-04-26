package it.polito.mad.g01_timebanking.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.g01_timebanking.UserKey
import java.util.*

class ProfileViewModel: ViewModel() {

    private val pvtFullName = MutableLiveData<String>().also {
        it.value = UserKey.FULL_NAME_PLACEHOLDER
    }
    val fullName : LiveData<String> = pvtFullName

    private val pvtNickname = MutableLiveData<String>().also {
        it.value = UserKey.NICKNAME_PLACEHOLDER
    }
    val nickname : LiveData<String> = pvtNickname

    private val pvtEmail = MutableLiveData<String>().also {
        it.value = UserKey.EMAIL_PLACEHOLDER
    }
    val email : LiveData<String> = pvtEmail

    private val pvtBiography = MutableLiveData<String>().also {
        it.value = UserKey.BIOGRAPHY_PLACEHOLDER
    }
    val biography : LiveData<String> = pvtBiography

    private val pvtLocation = MutableLiveData<String>().also {
        it.value = UserKey.LOCATION_PLACEHOLDER
    }
    val location : LiveData<String> = pvtLocation

    private val pvtProfilePicturePath = MutableLiveData<String>().also {
        it.value = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
    }
    val profilePicturePath : LiveData<String> = pvtProfilePicturePath

    private val pvtSkills = MutableLiveData<MutableSet<String>>().also {
        it.value = mutableSetOf<String>()
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

    fun setSkills(skills: MutableSet<String>) {
        pvtSkills.value = skills
    }
}