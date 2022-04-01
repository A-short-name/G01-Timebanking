package it.polito.mad.g01_timebanking

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class EditProfileActivity : AppCompatActivity() {

    lateinit var profilePicture:ImageButton
    var fullName: String = ""
    lateinit var ivFullName: EditText
    lateinit var ivNickname: EditText
    lateinit var ivEmail: EditText
    lateinit var ivLocation: EditText
    lateinit var profilePicturePath: String

    val CAPTURE_IMAGE_REQUEST = 1
    val PICK_IMAGE_REQUEST = 2
    val PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initializeView()

    }

    private fun initializeView() {
        ivFullName = findViewById(R.id.editTextFullName)
        ivNickname = findViewById(R.id.editTextNickname)
        ivEmail = findViewById(R.id.editTextEmail)
        ivLocation = findViewById(R.id.editTextLocation)
        profilePicture = findViewById(R.id.profilePictureButton)

        profilePicture.setOnClickListener { showPopup(profilePicture) }
        val i = intent
        ivFullName.setText(i.getStringExtra(UserKey.FULL_NAME_EXTRA_ID))
        ivNickname.setText(i.getStringExtra(UserKey.NICKNAME_EXTRA_ID))
        ivEmail.setText(i.getStringExtra(UserKey.EMAIL_EXTRA_ID))
        ivLocation.setText(i.getStringExtra(UserKey.LOCATION_EXTRA_ID))
        profilePicturePath = i.getStringExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID).toString()

        if (profilePicturePath is String && profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER)
            readImage()


    }

    override fun onBackPressed() {
        val i2 = Intent()
        prepareResult(i2)
        setResult(Activity.RESULT_OK,i2)
        //Salva in un file tutti i campi
        updatePreferences()
        super.onBackPressed() //finish is inside the onBackPressed()
    }

    private fun updatePreferences() {
        val u = UserInfo (
            ivFullName.text.toString(),
            ivNickname.text.toString(),
            ivEmail.text.toString(),
            ivLocation.text.toString(),
            profilePicturePath
        )

        val gson : Gson = Gson()
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
        i2.putExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID, profilePicturePath)
    }

    private fun dispatchChoosePictureIntent(){
        val choosePictureIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
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
            createImageFile(this)
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

    //@Throws(IOException::class)
    private fun createImageFile(context: Context): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)  ///storage/sdcard0/Pictures
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            //eg. /storage/emulated/0/Android/data/it.polito.mad.g01_timebanking/files/Pictures/JPEG_20220329_123453_7193664665067830656.jpg
            profilePicturePath = absolutePath
        }
    }

    //Add the photo to a gallery
    //https://developer.android.com/training/camera/photobasics#TaskGallery
    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            if(profilePicturePath is String && profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
                val f = File(profilePicturePath)
                println(f.absolutePath)
                mediaScanIntent.data = Uri.fromFile(f)
                sendBroadcast(mediaScanIntent)
            } else println("galleryAddPict: profilePicturePath is null")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST -> { //For Image Gallery

                if (resultCode == RESULT_OK) {
                    if(data != null && data.data != null) {
                        try {
                            //val fileImage : File = File(data.data.toString())
                            //profilePicturePath = fileImage.absolutePath
                            //val takenImage = BitmapFactory.decodeFile(profilePicturePath) //returns null
                            profilePicturePath = data.data.toString()
                            profilePicture.setImageURI(profilePicturePath.toUri()) // handle chosen image
                        } catch (e: IOException) {
                            Log.i("TAG", "Some exception $e")
                        }
                    }
                    else // handle chosen image
                    {
                        println("result: data is null")
                    }
                }
                else Toast.makeText(this, "Picture was not loaded", Toast.LENGTH_SHORT).show()
                return
            }
            CAPTURE_IMAGE_REQUEST -> if (resultCode == RESULT_OK) { //For CAMERA
                //You can use image PATH that you already created its file by the intent that launched the CAMERA (MediaStore.EXTRA_OUTPUT)

                    if(profilePicturePath is String && profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {                // by this point we have the camera photo on disk
                        readImage()
                        galleryAddPic()
                    } else println("result: profilePicturePath is null")               // RESIZE BITMAP, see section below
                //https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media
            } else { // Result was a failure
                Toast.makeText(this, "Picture was not taken!", Toast.LENGTH_SHORT).show()
            }
            }
        }

    private fun readImage() {
        if(profilePicturePath.startsWith("content://com.google.android.apps.photos.contentprovider/")) {
            profilePicture.setImageURI(profilePicturePath.toUri())
        }
        else {
            val takenImage = BitmapFactory.decodeFile(profilePicturePath)

            val ei = ExifInterface(profilePicturePath)
            val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            var rotatedBitmap: Bitmap? = null
            rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(takenImage, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(takenImage, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(takenImage, 270)
                ExifInterface.ORIENTATION_NORMAL -> takenImage
                else -> takenImage
            }

            profilePicture.setImageBitmap(rotatedBitmap)
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
        Toast.makeText(this, "Selected Item: " + item.title, Toast.LENGTH_SHORT).show()
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

    fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }

    fun checkPermissionAndChoose(){
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
                if (grantResults.size >0 && grantResults[0] ==
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
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        profilePicturePath = savedInstanceState.getString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID) ?: return

        readImage()
    }
}

