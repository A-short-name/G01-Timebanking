package it.polito.mad.g01_timebanking.ui.profile

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import it.polito.mad.g01_timebanking.FileHelper
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserKey
import java.io.File
import java.io.IOException

class EditProfileFragment: Fragment() {
    private val profileViewModel : ProfileViewModel by activityViewModels()

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
    lateinit var noSkills: TextView

    val CAPTURE_IMAGE_REQUEST = 1
    val PICK_IMAGE_REQUEST = 2
    val PERMISSION_CODE = 1001

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView(view)
        //initializeData()

        if(!FileHelper.isExternalStorageWritable())
            Log.e(EditProfileFragment.TAG, "No external volume mounted")
    }
    private fun initializeView(view: View) {
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

        profileViewModel.fullName.observe(this.viewLifecycleOwner) {
            ivFullName.setText(it)
        }
        profileViewModel.nickname.observe(this.viewLifecycleOwner) {
            ivNickname.setText(it)
        }
        profileViewModel.email.observe(this.viewLifecycleOwner) {
            ivEmail.setText(it)
        }
        profileViewModel.location.observe(this.viewLifecycleOwner) {
            ivLocation.setText(it)
        }
        profileViewModel.biography.observe(this.viewLifecycleOwner) {
            ivBiography.setText(it)
        }
        profileViewModel.profilePicturePath.observe(this.viewLifecycleOwner) {
            if (it != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
                FileHelper.readImage(it, profilePicture)
            }
        }
        profileViewModel.skills.observe(this.viewLifecycleOwner) {
            skillGroup.removeAllViews()

            if (it.isEmpty())
                noSkills.isVisible = true

            it.forEach { content ->
                val chip = Chip(context)
                chip.isCloseIconVisible = true;
                chip.text = content
                chip.isCheckable = false
                chip.isClickable = false
                chip.setOnCloseIconClickListener {
                    profileViewModel.removeSkill(chip.text.toString())
                }
                skillGroup.addView(chip)
                noSkills.isVisible = false
            }
        }
            // Set listener on "add skills" field
            ivSkills.setOnEditorActionListener { v, actionId, event ->
                // If user presses enter
                if(actionId == EditorInfo.IME_ACTION_DONE){

                    if(v.text.toString().length>UserKey.MINIMUM_SKILLS_LENGTH) {
                        // Add skillText on set
                        if(profileViewModel.tryToAddSkill(v.text.toString().lowercase())){
                            // PillView is added to the PillGroup thanks to the observer
                            // Reset editText field for new skills
                            v.text = ""
                        } else {
                            Toast.makeText(context,"Skill already present", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context,"Skill description is too short.\nUse at least ${UserKey.MINIMUM_SKILLS_LENGTH+1} characters", Toast.LENGTH_SHORT).show()
                    }
                    true
                } else {
                    false
                }
            }


        fun addSkillView(skillText: String) {

        }
/*        populateSkillGroup(i.getStringExtra(UserKey.SKILLS_EXTRA_ID))

        // Set listener on "add skills" field
        ivSkills.setOnEditorActionListener { v, actionId, event ->
            // If user presses enter
            if(actionId == EditorInfo.IME_ACTION_DONE){

                if(v.text.toString().length> UserKey.MINIMUM_SKILLS_LENGTH) {
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
        }*/
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(context, v)
        //Set on click listener for the menu
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item -> onMenuItemClick(item) })
        popup.inflate(R.menu.edit_profile_picture_menu)
        popup.show()

    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        //Toast.makeText(this, "Selected Item: " + item.title, Toast.LENGTH_SHORT).show()
        return when (item.itemId) {
            R.id.gallery ->                 // do your code
            {
                //checkPermissionAndChoose()
                true
            }
            R.id.camera ->                 // do your code
            {
                //dispatchTakePictureIntent()
                return true
            }
            else -> false
        }
    }

/*    private fun checkPermissionAndChoose(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (context?.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE);
            }
            else{
                //permission already granted
                dispatchChoosePictureIntent();
            }
        }
        else{
            //system OS is < Marshmallow
            dispatchChoosePictureIntent();
        }
    }
    private fun dispatchChoosePictureIntent(){
        val choosePictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        choosePictureIntent.type = "image/*"
        try {
            startActivityForResult(choosePictureIntent, PICK_IMAGE_REQUEST)
        }   catch (e: ActivityNotFoundException) {
            Toast.makeText(context,"Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchTakePictureIntent() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create the File where the photo should go
        val photoFile: File? = try {
            FileHelper.createImageFile(context).apply { profilePicturePath = absolutePath }
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "it.polito.mad.g01_timebanking.fileprovider",
                it
            )
            //FileProvider return a content in the form
            //content://it.polito.mad.g01_timebanking.fileprovider/my_images/JPEG_20220329_123453_7193664665067830656.jpg
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

            //Original problem with resolveActivity as in the documentation:
            //This appears to be due to the new restrictions on "package visibility" introduced in Android 11.
            //Basically, starting with API level 30, if you're targeting that version or higher,
            //your app cannot see, or directly interact with, most external packages without
            //explicitly requesting allowance, either through a blanket QUERY_ALL_PACKAGES
            //permission, or by including an appropriate <queries> element in your manifest.
            //https://stackoverflow.com/questions/62535856/intent-resolveactivity-returns-null-in-api-30
            //https://cketti.de/2020/09/03/avoid-intent-resolveactivity/
            try {
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST)
            }   catch (e: ActivityNotFoundException) {
                Toast.makeText(context,"Error", Toast.LENGTH_SHORT).show()
            }
        }
    }*/


 */

}