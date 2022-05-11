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
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.g01_timebanking.helpers.FileHelper
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.UserKey.CAPTURE_IMAGE_REQUEST
import it.polito.mad.g01_timebanking.UserKey.PERMISSION_CODE
import it.polito.mad.g01_timebanking.UserKey.PICK_IMAGE_REQUEST
import java.io.File
import java.io.IOException


class   EditProfileFragment: Fragment() {
    private val profileViewModel : ProfileViewModel by activityViewModels()

    companion object {
        private const val TAG = "EditProfileActivity"
    }

    // Views
    private lateinit var profilePicture: ImageView
    private lateinit var ivFullName: EditText
    private lateinit var ivNickname: EditText
    private lateinit var ivEmail: EditText
    private lateinit var ivLocation: EditText
    private lateinit var ivSkills: AutoCompleteTextView
    private lateinit var ivBiography: EditText
    private lateinit var skillGroup: ChipGroup
    private lateinit var noSkills: TextView

    private lateinit var currentProfilePicturePath: String
    private lateinit var currentSkills: MutableSet<String>

    //this variable is used in CAPTURE_IMAGE section of the onActivityResult
    //to change the vm only when the picture is saved
    private var tmpProfilePicturePath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        //val myclass = myClass(requireView())

        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner){
            val text: CharSequence
            if(validateFields()) {
                text = "Successfully updated profile details"
                Snackbar.make(root, text, Snackbar.LENGTH_LONG).show()
                confirm()
            } else {
                text = "Fields not valid. Changes not saved"
                Snackbar.make(root, text, Snackbar.LENGTH_LONG).show()
            }
            requireView().findNavController().popBackStack()
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch views
        ivFullName = view.findViewById(R.id.fullNameEditText)
        ivNickname = view.findViewById(R.id.nicknameEditText)
        ivEmail = view.findViewById(R.id.emailEditText)
        ivLocation = view.findViewById(R.id.locationEditText)
        ivBiography = view.findViewById(R.id.biographyEditText)
        ivSkills = view.findViewById(R.id.editTextAddSkills)
        profilePicture = view.findViewById(R.id.profilePicture)
        skillGroup = view.findViewById(R.id.skillgroup)
        noSkills = view.findViewById(R.id.noSkillsTextView)

        val profilePictureButton = view.findViewById<ImageButton>(R.id.profilePictureTransparentButton)
        profilePictureButton.setOnClickListener { showPopup(profilePictureButton) }

        profileViewModel.fullName.observe(this.viewLifecycleOwner) {
            if(it != ivFullName.text.toString())
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
            if (it != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER && it.isNotEmpty()) {
                Log.d("UPDATE_PICTURE", "Path is $it")
                FileHelper.readImage(it, profilePicture)
                profileViewModel.uploadPhoto(profilePicture)
            }
            currentProfilePicturePath = it
        }
        profileViewModel.skills.observe(this.viewLifecycleOwner) {
            skillGroup.removeAllViews()

            if (it.isEmpty())
                noSkills.isVisible = true

            it.forEach { content ->
                val chip = Chip(context)
                chip.isCloseIconVisible = true
                chip.text = content
                chip.isCheckable = false
                chip.isClickable = false
                chip.setOnCloseIconClickListener {
                    profileViewModel.removeSkill(chip.text.toString())
                }
                skillGroup.addView(chip)
                noSkills.isVisible = false
            }
            currentSkills = it
        }

        // Set listener on "add skills" field
        ivSkills.setOnEditorActionListener { v, actionId, _ ->
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

        initializeSkillSuggestion(view)

        if(!FileHelper.isExternalStorageWritable())
            Log.e(TAG, "No external volume mounted")
    }


    private fun initializeSkillSuggestion(view: View) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line, UserKey.SKILL_SUGGESTION
        )
        val actv = view.findViewById<AutoCompleteTextView>(R.id.editTextAddSkills)
        actv.setAdapter(adapter)
        actv.setOnItemClickListener { adapterView, _, i, _ ->
            val selected: String = adapterView.getItemAtPosition(i) as String
            if (profileViewModel.tryToAddSkill(selected.lowercase())) {
                // PillView is added to the PillGroup thanks to the observer
                // Reset editText field for new skills
                ivSkills.setText("")
            } else {
                Toast.makeText(context, "Skill already present", Toast.LENGTH_SHORT).show()
                ivSkills.setText("")
            }
        }
    }

    private fun showPopup(v: View) {
        val popup = PopupMenu(context, v)
        //Set on click listener for the menu
        popup.setOnMenuItemClickListener { item -> onMenuItemClick(item) }
        popup.inflate(R.menu.edit_profile_picture_menu)
        popup.show()

    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        //Toast.makeText(this, "Selected Item: " + item.title, Toast.LENGTH_SHORT).show()
        return when (item.itemId) {
            R.id.gallery ->                 // do your code
            {
                checkPermissionAndChoose()
                true
            }
            R.id.camera ->                 // do your code
            {
                dispatchTakePictureIntent()
                return true
            }
            else -> false
        }
    }

    private fun checkPermissionAndChoose(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (context?.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED){
                //permission denied
                val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE)
            }
            else{
                //permission already granted
                dispatchChoosePictureIntent()
            }
        }
        else{
            //system OS is < Marshmallow
            dispatchChoosePictureIntent()
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
            FileHelper.createImageFile(requireContext()).apply { tmpProfilePicturePath = absolutePath }
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
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
    }




    //Add the photo to a gallery
    //https://developer.android.com/training/camera/photobasics#TaskGallery
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            if(profileViewModel.profilePicturePath.value != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
                //val f = File(profileViewModel.profilePicturePath.value)
                val f = File(currentProfilePicturePath)
                mediaScanIntent.data = Uri.fromFile(f)
                activity?.sendBroadcast(mediaScanIntent)
            } else
                Log.e(TAG,"profilePicturePath is null")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> { //For Image Gallery
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    if(data != null && data.data != null) {
                        try {
                            val tmpProfilePicturePath = FileHelper.getRealPathFromURI(data.data, requireContext())
                            //Now I Copy the file loaded from the gallery (which path is stored in profilePicturePath) to a new file in the app-specific media storage folder
                            val newFile = File(tmpProfilePicturePath).copyTo(FileHelper.createImageFile(requireContext()))
                            profileViewModel.setProfilePicturePath(newFile.absolutePath)
                            //After createImageFile the profilePicturePath is changed to the one of the copy in app-specific folder
                            //New picture is loaded thanks to observer
                            galleryAddPic()
                        } catch (e: IOException) {
                            Log.i("TAG", "Some exception $e")
                        }
                    }
                    else Log.e(TAG,"result: profilePicturePath is null")
                }
                else Toast.makeText(context, "Picture was not loaded", Toast.LENGTH_SHORT).show()
                return
            }
            CAPTURE_IMAGE_REQUEST -> if (resultCode == AppCompatActivity.RESULT_OK) { //For CAMERA
                //You can use image PATH that you already created its file by the intent that launched the CAMERA (MediaStore.EXTRA_OUTPUT)
                //in dispatchIntent we change the value of the path and triggers the observer
                //call FileHelper.readImage here is redundant
                if(tmpProfilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {                // by this point we have the camera photo on disk
                    profileViewModel.setProfilePicturePath(tmpProfilePicturePath)
                    galleryAddPic()
                } else Log.e(TAG,"result: profilePicturePath is null")               // RESIZE BITMAP, see section below
                //https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media
            } else { // Result was a failure
                Toast.makeText(context, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted
                    dispatchChoosePictureIntent()
                }
                else{
                    //permission from popup denied
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onDetach() {
        //Attualmente questi salvano nel vm per preservare alla rotazione
        //Alternativamente si possono usare dei listener sugli editText che modificano il view model
        profileViewModel.setFullname(ivFullName.text.toString())
        profileViewModel.setNickname(ivNickname.text.toString())
        profileViewModel.setEmail(ivEmail.text.toString())
        profileViewModel.setLocation(ivLocation.text.toString())
        profileViewModel.setBiography(ivBiography.text.toString())
        //profileViewModel.setProfilePicturePath() is changed everytime
        //profileViewModel.setSkills() is changed everytime
        super.onDetach()
    }
    private fun confirm() {
        val u = UserInfo (
            fullName = ivFullName.text.toString(),
            nickname = ivNickname.text.toString(),
            email = ivEmail.text.toString(),
            location = ivLocation.text.toString(),
            biography = ivBiography.text.toString(),
            skills = currentSkills.toMutableList()
        )

        profileViewModel.addOrUpdateData(u)
        //To erase ephemeral data
        //profileViewModel.setUserInfo(u)
    }

    private fun validateFields() : Boolean {
        var valid = true

        val fieldsToValidate = listOf(ivFullName,
            ivNickname,
            ivEmail)

        fieldsToValidate.forEach{
            if (it.text.isBlank()) {
                //it.error = UserKey.REQUIRED
                valid = false
            } else {
                //it.error = null
            }
        }

        return valid
    }

}