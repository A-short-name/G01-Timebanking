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
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
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

/*    class myClass(val v: View): OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            v.findNavController().popBackStack()
        }
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_edit_profile, container, false)
        //val myclass = myClass(requireView())

        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner){
            Toast.makeText(context,"Programmatore: ricordati di salvare i dati", Toast.LENGTH_SHORT).show()
            requireView().findNavController().popBackStack()
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView(view)
        initializeSkillSuggestion(view)

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
    }

    private fun initializeSkillSuggestion(view: View) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line, UserKey.SKILL_SUGGESTION
        );
        val actv = view.findViewById<AutoCompleteTextView>(R.id.editTextAddSkills)
        actv.setAdapter(adapter)
        actv.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l
            ->
            val selected: String = adapterView.getItemAtPosition(i) as String
            if (profileViewModel.tryToAddSkill(selected.lowercase())) {
                // PillView is added to the PillGroup thanks to the observer
                // Reset editText field for new skills
                ivSkills.setText("")
            } else {
                Toast.makeText(context, "Skill already present", Toast.LENGTH_SHORT).show()
                ivSkills.setText("")
            }
        })
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

    var tmpProfilePicturePath = ""

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
                val f = File(profileViewModel.profilePicturePath.value)
                mediaScanIntent.data = Uri.fromFile(f)
                activity?.sendBroadcast(mediaScanIntent)
            } else Log.e(TAG,"profilePicturePath is null")
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
                if (grantResults.size > 0 && grantResults[0] ==
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
}