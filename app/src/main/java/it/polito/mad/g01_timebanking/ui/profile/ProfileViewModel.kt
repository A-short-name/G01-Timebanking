package it.polito.mad.g01_timebanking.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.UserKey
import java.util.*

class ProfileViewModel: ViewModel() {

    //private val repo = PreferencesRepo()

    private val pvtUser = MutableLiveData<UserInfo>().also {
        it.value = UserInfo()
    }
    val user : LiveData<UserInfo> = pvtUser

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

    var tmpSkills : MutableSet<String> = mutableSetOf()
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
        tmpSkills=skills
        pvtSkills.value = skills
    }

    fun removeSkill(skillText: String) {
        tmpSkills.remove(skillText)
        pvtSkills.value = tmpSkills
        //pvtSkills.value.remove(skillText)
    }

    fun tryToAddSkill(skillText: String): Boolean {
        return if(tmpSkills.add(skillText)){
                    pvtSkills.value = tmpSkills
                    true
                }
                else
                    false
    }

    fun persistData(user: UserInfo){
        //repo.save(user)
        pvtUser.value = user
    }

    fun loadData(){
        //val u :UserInfo= repo.load("userinfo")
        val u = UserInfo()
        pvtUser.value = u
        pvtFullName.value = u.fullName
        pvtNickname.value = u.nickname
        pvtEmail.value = u.email
        pvtLocation.value = u.location
        pvtBiography.value = u.biography
        pvtProfilePicturePath.value = u.profilePicturePath
        pvtSkills.value = u.skills
    }
}