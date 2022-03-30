package it.polito.mad.g01_timebanking

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class EditProfileActivity : AppCompatActivity() {

    lateinit var profilePicture:ImageButton
    lateinit var ivFullName: EditText
    lateinit var ivNickname: EditText
    lateinit var ivEmail: EditText
    lateinit var ivLocation: EditText
    lateinit var skillGroup: ChipGroup

    // TODO: read from file
    var skills = mutableSetOf("Lavavetri","Pelatore di castagne")
    var profilePicturePath: String? = null
    val CAPTURE_IMAGE_REQUEST = 1
    val PICK_IMAGE_REQUEST = 2

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
        skillGroup = findViewById(R.id.skillgroup)

        profilePicture.setOnClickListener { showPopup(profilePicture) }
        val i = intent
        ivFullName.setText(i.getStringExtra(UserKey.FULL_NAME_EXTRA_ID))
        ivNickname.setText(i.getStringExtra(UserKey.NICKNAME_EXTRA_ID))
        ivEmail.setText(i.getStringExtra(UserKey.EMAIL_EXTRA_ID))
        ivLocation.setText(i.getStringExtra(UserKey.LOCATION_EXTRA_ID))
        profilePicturePath = i.getStringExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID)

        if (profilePicturePath is String) {
            val bitMapProfilePicture = BitmapFactory.decodeFile(profilePicturePath)
            profilePicture.setImageBitmap(bitMapProfilePicture)
        }

        // TODO: TEMPORARY
        skills.forEach{
            val chip = Chip(this)
            chip.isCloseIconVisible = true;
            chip.text = it
            chip.isCheckable = false
            chip.isClickable = false
            chip.setOnCloseIconClickListener { skills.remove(chip.text); skillGroup.removeView(chip) }
            skillGroup.addView(chip)
        }
    }

    override fun onBackPressed() {
        val i2 = Intent()
        prepareResult(i2)
        setResult(Activity.RESULT_OK,i2)
        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            println("In edit")
            putString(getString(R.string.name), ivFullName.text.toString())
            apply()
        }
        //TODO: Salva in un file tutti i campi
        super.onBackPressed() //finish is inside the onBackPressed()
    }

    private fun prepareResult(i2: Intent) {
        i2.putExtra(UserKey.FULL_NAME_EXTRA_ID, ivFullName.text.toString())
        i2.putExtra(UserKey.NICKNAME_EXTRA_ID, ivNickname.text.toString())
        i2.putExtra(UserKey.EMAIL_EXTRA_ID, ivEmail.text.toString())
        i2.putExtra(UserKey.LOCATION_EXTRA_ID, ivLocation.text.toString())
        i2.putExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID, profilePicturePath)
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
            //Basically, starting with API level 30, if you're targeting that version or higher, your app cannot see, or directly interact with, most external packages without explicitly requesting allowance, either through a blanket QUERY_ALL_PACKAGES permission, or by including an appropriate <queries> element in your manifest.
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
            if(profilePicturePath is String) {
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
                if (resultCode == RESULT_OK && data != null && data.data != null) {

                }
                return
            }
            CAPTURE_IMAGE_REQUEST -> if (resultCode == RESULT_OK) { //For CAMERA
                //You can use image PATH that you already created its file by the intent that launched the CAMERA (MediaStore.EXTRA_OUTPUT)

                    if(profilePicturePath is String) {                // by this point we have the camera photo on disk
                        readImage()
                        galleryAddPic()
                    } else println("result: profilePicturePath is null")               // RESIZE BITMAP, see section below
                //https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show()
            }
            }
        }

    private fun readImage() {
        val takenImage = BitmapFactory.decodeFile(profilePicturePath)

        val ei = ExifInterface(profilePicturePath!!)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        var rotatedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap =
                rotateImage(takenImage, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap =
                rotateImage(takenImage, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap =
                rotateImage(takenImage, 270)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = takenImage
            else -> rotatedBitmap = takenImage
        }

        profilePicture.setImageBitmap(rotatedBitmap)
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
                true
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

