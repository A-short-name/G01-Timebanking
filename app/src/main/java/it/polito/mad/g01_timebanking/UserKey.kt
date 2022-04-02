package it.polito.mad.g01_timebanking

object UserKey {

    const val EDIT_ACTIVITY_REQUEST = 1

    const val FULL_NAME = "fullName"
    const val NICKNAME = "nickname"
    const val EMAIL = "email"
    const val LOCATION = "location"
    const val PROFILE_PICTURE_PATH = "profilePicturePath"
    const val SKILLS = "skills"
    const val BASE_EXTRA = "it.polito.mad.g01_timebanking."
    const val FULL_NAME_EXTRA_ID = BASE_EXTRA+ FULL_NAME
    const val NICKNAME_EXTRA_ID = BASE_EXTRA+ NICKNAME
    const val EMAIL_EXTRA_ID = BASE_EXTRA+ EMAIL
    const val LOCATION_EXTRA_ID = BASE_EXTRA+ LOCATION
    const val PROFILE_PICTURE_PATH_EXTRA_ID = BASE_EXTRA+ PROFILE_PICTURE_PATH
    const val SKILLS_EXTRA_ID = BASE_EXTRA + SKILLS
    const val FULL_NAME_PLACEHOLDER = "No name"
    const val NICKNAME_PLACEHOLDER = "No nick"
    const val EMAIL_PLACEHOLDER = "No email"
    const val LOCATION_PLACEHOLDER = "No loc"
    const val PROFILE_PICTURE_PATH_PLACEHOLDER = ""
    const val MINIMUM_SKILLS_LENGTH = 3
}