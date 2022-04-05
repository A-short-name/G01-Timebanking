package it.polito.mad.g01_timebanking

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.util.*


class EditProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EditProfileActivity"
    }

    // Views
    lateinit var profilePicture:ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeView()
        initializeSkillSuggestion()
        if(!FileHelper.isExternalStorageWritable())
            Log.e(TAG, "No external volume mounted")
    }

    private fun initializeView() {
        // Fetch views
        ivFullName = findViewById(R.id.editTextFullName)
        ivNickname = findViewById(R.id.editTextNickname)
        ivEmail = findViewById(R.id.editTextEmail)
        ivLocation = findViewById(R.id.editTextLocation)
        ivBiography = findViewById(R.id.editTextBiography)
        ivSkills = findViewById(R.id.editTextAddSkills)
        profilePicture = findViewById(R.id.profilePicture)
        val profilePictureButton = findViewById<ImageButton>(R.id.profilePictureTransparentButton)
        skillGroup = findViewById(R.id.skillgroup)
        noSkills = findViewById(R.id.noSkillsTextView)

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
    }

    override fun onBackPressed() {
        val i2 = Intent()
        prepareResult(i2)
        setResult(Activity.RESULT_OK,i2)
        //Salva in un file tutti i campi
        updatePreferences()

        Log.i(TAG,"profile preference wrote in local cache")
        super.onBackPressed() //finish is inside the onBackPressed()
    }

    private fun updatePreferences() {
        val u = UserInfo (
            fullName = ivFullName.text.toString(),
            nickname = ivNickname.text.toString(),
            email = ivEmail.text.toString(),
            location = ivLocation.text.toString(),
            biography = ivBiography.text.toString(),
            profilePicturePath = profilePicturePath,
            skills = skills
        )

        val gson = Gson();
        val serializedUser: String = gson.toJson(u)

        val sharedPref =
            this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
                ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.user_info), serializedUser)
            apply()
        }
    }

    private fun prepareResult(i2: Intent) {
        i2.putExtra(UserKey.FULL_NAME_EXTRA_ID, ivFullName.text.toString())
        i2.putExtra(UserKey.NICKNAME_EXTRA_ID, ivNickname.text.toString())
        i2.putExtra(UserKey.EMAIL_EXTRA_ID, ivEmail.text.toString())
        i2.putExtra(UserKey.LOCATION_EXTRA_ID, ivLocation.text.toString())
        i2.putExtra(UserKey.BIOGRAPHY_EXTRA_ID, ivBiography.text.toString())
        i2.putExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID, profilePicturePath)
        val gson = Gson();
        val serializedSkills: String = gson.toJson(skills)
        i2.putExtra(UserKey.SKILLS_EXTRA_ID, serializedSkills)
    }

    private fun dispatchChoosePictureIntent(){
        val choosePictureIntent = Intent(Intent.ACTION_GET_CONTENT)
        choosePictureIntent.type = "image/*"
        try {
            startActivityForResult(choosePictureIntent, PICK_IMAGE_REQUEST)
        }   catch (e: ActivityNotFoundException) {
            Toast.makeText(this,"Error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dispatchTakePictureIntent() {

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create the File where the photo should go
        val photoFile: File? = try {
            FileHelper.createImageFile(this).apply { profilePicturePath = absolutePath }
        } catch (ex: IOException) {
            // Error occurred while creating the File
            null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
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
                Toast.makeText(this,"Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Add the photo to a gallery
    //https://developer.android.com/training/camera/photobasics#TaskGallery
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            if(profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
                val f = File(profilePicturePath)
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            } else Log.e(TAG,"profilePicturePath is null")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> { //For Image Gallery
                if (resultCode == RESULT_OK) {
                    if(data != null && data.data != null) {
                        try {
                            profilePicturePath = FileHelper.getRealPathFromURI(data.data, this)
                            //Now I Copy the file loaded from the gallery (which path is stored in profilePicturePath) to a new file in the app-specific media storage folder
                            File(profilePicturePath).copyTo(FileHelper.createImageFile(this).apply { profilePicturePath = absolutePath })
                            //After createImageFile the profilePicturePath is changed to the one of the copy in app-specific folder
                            FileHelper.readImage(profilePicturePath, profilePicture)
                            galleryAddPic()
                        } catch (e: IOException) {
                            Log.i("TAG", "Some exception $e")
                        }
                    }
                    else Log.e(TAG,"result: profilePicturePath is null")
                }
                else Toast.makeText(this, "Picture was not loaded", Toast.LENGTH_SHORT).show()
                return
            }
            CAPTURE_IMAGE_REQUEST -> if (resultCode == RESULT_OK) { //For CAMERA
                //You can use image PATH that you already created its file by the intent that launched the CAMERA (MediaStore.EXTRA_OUTPUT)

                    if(profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {                // by this point we have the camera photo on disk
                        FileHelper.readImage(profilePicturePath, profilePicture)
                        galleryAddPic()
                    } else Log.e(TAG,"result: profilePicturePath is null")               // RESIZE BITMAP, see section below
                //https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
            }
        }

    private fun showPopup(v: View) {
        val popup = PopupMenu(this, v)
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
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
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
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID,profilePicturePath)
        val gson = Gson();
        val serializedSkills: String = gson.toJson(skills)
        outState.putString(UserKey.SKILLS_EXTRA_ID, serializedSkills)
        //alternativa, credo perdo l'ordine
        //outState.putStringArray(UserKey.SKILLS_EXTRA_ID, skills.toTypedArray())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        populateSkillGroup(savedInstanceState.getString(UserKey.SKILLS_EXTRA_ID))

        profilePicturePath = savedInstanceState.getString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID) ?: return

        if(profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER)
            FileHelper.readImage(profilePicturePath, profilePicture)
    }

    private fun populateSkillGroup(serializedJson: String?) {
        val gson = Gson()
        //se qualcosa va storto, e non riesce neanche a fare il cast, ritorna null. In quel caso lo rimappo sul set vuoto
        //introdotto a causa di un errore ruotando lo schermo in editProfile, causato da cast errato di MutableSet
                //as? MutableSet<String> ?: mutableSetOf<String>()
        skills = gson.fromJson(serializedJson, MutableSet::class.java) as MutableSet<String> //pay attention to this cast

        skillGroup.removeAllViews()

        if(skills.isEmpty())
            noSkills.isVisible = true

        skills.forEach {
            addSkillView(it)
        }
    }

    private fun addSkillView(skillText: String) {
        val chip = Chip(this)
        chip.isCloseIconVisible = true;
        chip.text = skillText
        chip.isCheckable = false
        chip.isClickable = false
        chip.setOnCloseIconClickListener {
            skills.remove(chip.text)
            skillGroup.removeView(chip)
            if(skills.isEmpty())
                noSkills.isVisible=true
        }
        skillGroup.addView(chip)
        noSkills.isVisible = false
    }


    private fun initializeSkillSuggestion() {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            this,
            android.R.layout.simple_dropdown_item_1line, UserKey.SKILL_SUGGESTION
        );
        val actv = findViewById<AutoCompleteTextView>(R.id.editTextAddSkills)
        actv.setAdapter(adapter)
        actv.setOnItemClickListener(AdapterView.OnItemClickListener { adapterView, view, i, l
            ->
            val selected: String = adapterView.getItemAtPosition(i) as String
            if (skills.add(selected.lowercase())) {
                // Add Pill
                addSkillView(selected.lowercase())
                // Reset editText field for new skills
                ivSkills.setText("")
            } else {
                Toast.makeText(this, "Skill already present", Toast.LENGTH_SHORT).show()
                ivSkills.setText("")
            }
        })
    }

}

