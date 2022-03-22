package it.polito.mad.g01_timebanking

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView

class ShowProfileActivity : AppCompatActivity() {
    private val fullName = "Matti"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        val tmpName = findViewById<TextView>(R.id.name)
        tmpName.text = fullName
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
                // TODO: call intent
                // val intent = Intent(this, MyActivity::class.java)
                // startActivity(intent)
                println("Button clicked")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}