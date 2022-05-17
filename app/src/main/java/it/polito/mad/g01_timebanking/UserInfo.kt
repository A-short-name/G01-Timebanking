package it.polito.mad.g01_timebanking

import com.google.firebase.firestore.PropertyName

data class UserInfo(
    @set:PropertyName("fullName")
    @get:PropertyName("fullName")
    var fullName:String = UserKey.FULL_NAME_PLACEHOLDER,

    @set:PropertyName("nickname")
    @get:PropertyName("nickname")
    var nickname: String = UserKey.NICKNAME_PLACEHOLDER,

    @set:PropertyName("email")
    @get:PropertyName("email")
    var email: String = UserKey.EMAIL_PLACEHOLDER,

    @set:PropertyName("location")
    @get:PropertyName("location")
    var location: String = UserKey.LOCATION_PLACEHOLDER,

    @set:PropertyName("biography")
    @get:PropertyName("biography")
    var biography: String = UserKey.BIOGRAPHY_PLACEHOLDER,

    @set:PropertyName("profilePicturePath")
    @get:PropertyName("profilePicturePath")
    var profilePicturePath : String = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER,

    @set:PropertyName("skills")
    @get:PropertyName("skills")
    var skills: MutableList<String> = mutableListOf()
)
