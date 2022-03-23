package it.polito.mad.g01_timebanking

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView

class ShowProfileActivity : AppCompatActivity() {
    lateinit var tv:TextView
    private val fullNamePlaceholder = "No name"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        tv = findViewById<TextView>(R.id.name)
        tv.text = fullNamePlaceholder
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.editButton -> {
                editProfile()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val i = Intent(this, EditProfileActivity::class.java)
        val putExtra = i.putExtra("it.polito.mad.g01_timebanking.fullName", tv.text)
        startActivityForResult(i, 0)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val b = data?.getStringExtra("fullName")
        tv.text = b
        //TODO: prendere tutti i nomi ritornati dal result
    }
}