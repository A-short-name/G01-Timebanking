package it.polito.mad.g01_timebanking

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.core.os.bundleOf

class ShowProfileActivity : AppCompatActivity() {
    lateinit var tvFullName:TextView
    lateinit var tvNickname:TextView
    lateinit var tvEmail:TextView
    lateinit var tvLocation:TextView

    private val fullNamePlaceholder = "No name"
    private val nicknamePlaceholder = "No nick"
    private val emailPlaceholder = "No email"
    private val locationPlaceholder = "No loc"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        tvFullName = findViewById<TextView>(R.id.fullname)
        tvNickname = findViewById<TextView>(R.id.nickname)
        tvEmail = findViewById<TextView>(R.id.email)
        tvLocation = findViewById<TextView>(R.id.location)

        tvFullName.text = fullNamePlaceholder
        tvNickname.text = nicknamePlaceholder
        tvEmail.text = emailPlaceholder
        tvLocation.text = locationPlaceholder
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
        val b : Bundle = bundleOf(
            "it.polito.mad.g01_timebanking.fullName" to tvFullName.text,
            "it.polito.mad.g01_timebanking.nickname" to tvNickname.text,
            "it.polito.mad.g01_timebanking.email" to tvEmail.text,
            "it.polito.mad.g01_timebanking.location" to tvLocation.text,
            )
        i.putExtras(b)

        startActivityForResult(i, 0)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fn = data?.getStringExtra("fullName")
        tvFullName.text = fn
        val nn = data?.getStringExtra("nickname")
        tvNickname.text = nn
        val em = data?.getStringExtra("email")
        tvEmail.text = em
        val loc = data?.getStringExtra("location")
        tvLocation.text = loc
        //TODO: prendere tutti i nomi ritornati dal result
    }
}