package it.polito.mad.g01_timebanking

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
}