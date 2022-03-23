package it.polito.mad.g01_timebanking

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.TextView
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    var fullName: String = ""
    lateinit var ivFullName: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        ivFullName = findViewById(R.id.inputFullName)
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
}