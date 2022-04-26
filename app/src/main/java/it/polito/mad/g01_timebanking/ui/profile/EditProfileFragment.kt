package it.polito.mad.g01_timebanking.ui.profile

import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.chip.ChipGroup

class EditProfileFragment: Fragment() {
    companion object {
        private const val TAG = "EditProfileActivity"
    }

    // Views
    lateinit var profilePicture: ImageView
    lateinit var ivFullName: EditText
    lateinit var ivNickname: EditText
    lateinit var ivEmail: EditText
    lateinit var ivLocation: EditText
    lateinit var ivSkills: AutoCompleteTextView
    lateinit var ivBiography: EditText
    lateinit var skillGroup: ChipGroup
    lateinit var profilePicturePath: String
    lateinit var noSkills: TextView

    // Variables
    lateinit var skills : MutableSet<String>


    val CAPTURE_IMAGE_REQUEST = 1
    val PICK_IMAGE_REQUEST = 2
    val PERMISSION_CODE = 1001


   /* private fun initializeView(view: View) {
        // Fetch views
        ivFullName = view.findViewById(R.id.editTextFullName)
        ivNickname = view.findViewById(R.id.editTextNickname)
        ivEmail = view.findViewById(R.id.editTextEmail)
        ivLocation = view.findViewById(R.id.editTextLocation)
        ivBiography = view.findViewById(R.id.editTextBiography)
        ivSkills = view.findViewById(R.id.editTextAddSkills)
        profilePicture = view.findViewById(R.id.profilePicture)
        val profilePictureButton = view.findViewById<ImageButton>(R.id.profilePictureTransparentButton)
        skillGroup = view.findViewById(R.id.skillgroup)
        noSkills = view.findViewById(R.id.noSkillsTextView)

        // Set listener for picture clicks
        profilePictureButton.setOnClickListener { showPopup(profilePictureButton) }

        // Initialize values
        val i = intent
        ivFullName.setText(i.getStringExtra(UserKey.FULL_NAME_EXTRA_ID))
        ivNickname.setText(i.getStringExtra(UserKey.NICKNAME_EXTRA_ID))
        ivEmail.setText(i.getStringExtra(UserKey.EMAIL_EXTRA_ID))
        ivLocation.setText(i.getStringExtra(UserKey.LOCATION_EXTRA_ID))
        ivBiography.setText(i.getStringExtra(UserKey.BIOGRAPHY_EXTRA_ID))
        profilePicturePath = i.getStringExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID).toString()

        if (profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER)
            FileHelper.readImage(profilePicturePath, profilePicture)


        populateSkillGroup(i.getStringExtra(UserKey.SKILLS_EXTRA_ID))

        // Set listener on "add skills" field
        ivSkills.setOnEditorActionListener { v, actionId, event ->
            // If user presses enter
            if(actionId == EditorInfo.IME_ACTION_DONE){

                if(v.text.toString().length>UserKey.MINIMUM_SKILLS_LENGTH) {
                    // Add skillText on set
                    if(skills.add(v.text.toString().lowercase())){
                        // Add Pill
                        addSkillView(v.text.toString().lowercase())
                        // Reset editText field for new skills
                        v.text = ""
                    } else {
                        Toast.makeText(this,"Skill already present", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this,"Skill description is too short.\nUse at least ${UserKey.MINIMUM_SKILLS_LENGTH+1} characters", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
    }*/

}