package it.polito.mad.g01_timebanking

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    var fullName: String = ""
    lateinit var ivFullName: EditText
    lateinit var ivNickname: EditText
    lateinit var ivEmail: EditText
    lateinit var ivLocation: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        ivFullName = findViewById(R.id.editTextFullName)
        ivNickname = findViewById(R.id.editTextNickname)
        ivEmail = findViewById(R.id.editTextEmail)
        ivLocation = findViewById(R.id.editTextLocation)

        val i = intent
        ivFullName.setText(i.getStringExtra("it.polito.mad.g01_timebanking.fullName"))
        ivNickname.setText(i.getStringExtra("it.polito.mad.g01_timebanking.nickname"))
        ivEmail.setText(i.getStringExtra("it.polito.mad.g01_timebanking.email"))
        ivLocation.setText(i.getStringExtra("it.polito.mad.g01_timebanking.location"))
    }

    override fun onBackPressed() {
        //TODO: riempire il result con i valori di tutti i campi
        val i2 = Intent()
        i2.putExtra("fullName", ivFullName.text.toString())
        i2.putExtra("nickname", ivNickname.text.toString())
        i2.putExtra("email", ivEmail.text.toString())
        i2.putExtra("location", ivLocation.text.toString())
        setResult(Activity.RESULT_OK,i2)
        //TODO: Salva in un file tutti i campi
        super.onBackPressed() //finish is inside the onBackPressed()
    }
}