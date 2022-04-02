package it.polito.mad.g01_timebanking

object UserKey {

    const val EDIT_ACTIVITY_REQUEST = 1

    private const val FULL_NAME = "fullName"
    private const val NICKNAME = "nickname"
    private const val EMAIL = "email"
    private const val LOCATION = "location"
    private const val BIOGRAPHY = "biography"
    private const val PROFILE_PICTURE_PATH = "profilePicturePath"
    private const val SKILLS = "skills"
    private const val BASE_EXTRA = "it.polito.mad.g01_timebanking."
    const val FULL_NAME_EXTRA_ID = BASE_EXTRA+ FULL_NAME
    const val NICKNAME_EXTRA_ID = BASE_EXTRA+ NICKNAME
    const val EMAIL_EXTRA_ID = BASE_EXTRA+ EMAIL
    const val LOCATION_EXTRA_ID = BASE_EXTRA+ LOCATION
    const val BIOGRAPHY_EXTRA_ID = BASE_EXTRA+ BIOGRAPHY
    const val PROFILE_PICTURE_PATH_EXTRA_ID = BASE_EXTRA+ PROFILE_PICTURE_PATH
    const val SKILLS_EXTRA_ID = BASE_EXTRA + SKILLS
    const val FULL_NAME_PLACEHOLDER = "No name"
    const val NICKNAME_PLACEHOLDER = "No nick"
    const val EMAIL_PLACEHOLDER = "No email"
    const val LOCATION_PLACEHOLDER = "No loc"
    const val BIOGRAPHY_PLACEHOLDER = "No biography"
    const val PROFILE_PICTURE_PATH_PLACEHOLDER = ""
    const val MINIMUM_SKILLS_LENGTH = 3
}