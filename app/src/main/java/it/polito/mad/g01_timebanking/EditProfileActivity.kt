package it.polito.mad.g01_timebanking

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        val i = intent
        val tvName = findViewById<TextView>(R.id.name)
        tvName.text = i.getStringExtra("it.polito.mad.g01_timebanking.nome")



    }

    override fun onBackPressed() {
        //riempire il result con i valori di tutti i campi
        val i2 = Intent()
        i2.putExtra("myK","myV")
        setResult(Activity.RESULT_OK,i2)
        println("[edit profile] back button pressed")
        super.onBackPressed()
    }
}