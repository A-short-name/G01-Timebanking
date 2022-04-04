package it.polito.mad.g01_timebanking


data class UserInfo(
    var fullName:String,
    var nickname: String,
    var email: String,
    var location: String,
    var biography: String,
    var profilePicturePath: String,
    var skills: MutableSet<String>
) {
    constructor() : this(
        fullName = UserKey.FULL_NAME_PLACEHOLDER,
        nickname = UserKey.NICKNAME_PLACEHOLDER,
        email = UserKey.EMAIL_PLACEHOLDER,
        location = UserKey.LOCATION_PLACEHOLDER,
        biography = UserKey.BIOGRAPHY_PLACEHOLDER,
        profilePicturePath = UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER,
        skills = mutableSetOf<String>()      //pay attention
    )
}
