package it.polito.mad.g01_timebanking

import android.provider.ContactsContract

data class UserInfo(
    var fullName:String,
    var nickname: String,
    var email: String,
    var location: String,
    var profilePicturePath: String,
    var skills: MutableSet<String>
) {
    constructor() : this(
        UserKey.FULL_NAME_PLACEHOLDER,
        UserKey.NICKNAME_PLACEHOLDER,
        UserKey.EMAIL_PLACEHOLDER,
        UserKey.LOCATION_PLACEHOLDER,
        UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER,
        mutableSetOf<String>()      //TODO check this
    )
}
