package it.polito.mad.g01_timebanking

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    lateinit var profilePicture:ImageButton
    var fullName: String = ""
    lateinit var ivFullName: TextInputEditText

    val REQUEST_IMAGE_CAPTURE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        ivFullName = findViewById(R.id.inputFullName)
        profilePicture = findViewById(R.id.profilePictureButton)
        profilePicture.setOnClickListener { dispatchTakePictureIntent() }
        val i = intent
        ivFullName.setText(i.getStringExtra("it.polito.mad.g01_timebanking.fullName"))

    }

    override fun onBackPressed() {
        //TODO: riempire il result con i valori di tutti i campi
        val i2 = Intent()
        i2.putExtra("fullName", ivFullName.text.toString())
        setResult(Activity.RESULT_OK,i2)
        //TODO: Salva in un file tutti i campi
        super.onBackPressed() //finish is inside the onBackPressed()
    }


    // get reference to button
// set on-click listener


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            profilePicture.setImageBitmap(imageBitmap)
        }
    }
}