/*


class ShowProfileActivity : AppCompatActivity() {
    private fun editProfile() {
        val gson = Gson()
        val serializedSkills: String = gson.toJson(skills)

        val i = Intent(this, EditProfileActivity::class.java)
        val b : Bundle = bundleOf(
            UserKey.FULL_NAME_EXTRA_ID to tvFullName.text,
            UserKey.NICKNAME_EXTRA_ID to tvNickname.text,
            UserKey.EMAIL_EXTRA_ID to tvEmail.text,
            UserKey.LOCATION_EXTRA_ID to tvLocation.text,
            UserKey.BIOGRAPHY_EXTRA_ID to tvBiography.text,
            UserKey.PROFILE_PICTURE_PATH_EXTRA_ID to profilePicturePath,
            UserKey.SKILLS_EXTRA_ID to serializedSkills
            )
        i.putExtras(b)

        startActivityForResult(i, UserKey.EDIT_ACTIVITY_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            UserKey.EDIT_ACTIVITY_REQUEST -> {
                if(resultCode == RESULT_OK) {
                    readResult(data)
                    updateView()
                }
                else
                    Log.e(TAG,"activity result not OK,\n\tresultCode: $resultCode\n\trequestCode: $requestCode ")
            }
            else -> { Log.e(TAG,"activity request code not recognized, requestCode: $requestCode")
            }
        }

    }

    private fun readResult(data: Intent?) {
        fullName = data?.getStringExtra(UserKey.FULL_NAME_EXTRA_ID) ?: UserKey.FULL_NAME_PLACEHOLDER
        nickName = data?.getStringExtra(UserKey.NICKNAME_EXTRA_ID) ?: UserKey.NICKNAME_PLACEHOLDER
        email = data?.getStringExtra(UserKey.EMAIL_EXTRA_ID) ?: UserKey.EMAIL_PLACEHOLDER
        location = data?.getStringExtra(UserKey.LOCATION_EXTRA_ID) ?: UserKey.LOCATION_PLACEHOLDER
        biography = data?.getStringExtra(UserKey.BIOGRAPHY_EXTRA_ID) ?: UserKey.BIOGRAPHY_PLACEHOLDER
        profilePicturePath = data?.getStringExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID) ?: UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
        val gson = Gson()
        val serializedJson = data?.getStringExtra(UserKey.SKILLS_EXTRA_ID)
        skills = gson.fromJson(serializedJson, MutableSet::class.java) as MutableSet<String>

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(UserKey.FULL_NAME_EXTRA_ID, fullName)
        outState.putString(UserKey.NICKNAME_EXTRA_ID, nickName)
        outState.putString(UserKey.EMAIL_EXTRA_ID, email)
        outState.putString(UserKey.LOCATION_EXTRA_ID, location)
        outState.putString(UserKey.BIOGRAPHY_EXTRA_ID, biography)
        outState.putString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID,profilePicturePath)

        val gson = Gson()
        val serializedSkills: String = gson.toJson(skills)
        outState.putString(UserKey.SKILLS_EXTRA_ID, serializedSkills)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        fullName = savedInstanceState.getString(UserKey.FULL_NAME_EXTRA_ID) ?: UserKey.FULL_NAME_PLACEHOLDER
        nickName = savedInstanceState.getString(UserKey.NICKNAME_EXTRA_ID) ?: UserKey.NICKNAME_PLACEHOLDER
        email = savedInstanceState.getString(UserKey.EMAIL_EXTRA_ID) ?: UserKey.EMAIL_PLACEHOLDER
        location = savedInstanceState.getString(UserKey.LOCATION_EXTRA_ID) ?: UserKey.LOCATION_PLACEHOLDER
        biography = savedInstanceState.getString(UserKey.BIOGRAPHY_EXTRA_ID) ?: UserKey.BIOGRAPHY_PLACEHOLDER
        profilePicturePath = savedInstanceState.getString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID).toString()


        val gson = Gson();
        val serializedJson = savedInstanceState.getString(UserKey.SKILLS_EXTRA_ID)
        skills = gson.fromJson(serializedJson, MutableSet::class.java) as MutableSet<String>

        updateView()
    }

}
*/