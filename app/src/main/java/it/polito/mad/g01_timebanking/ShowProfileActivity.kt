package it.polito.mad.g01_timebanking

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var tvFullName:TextView
    private lateinit var tvNickname:TextView
    private lateinit var tvEmail:TextView
    private lateinit var tvLocation:TextView
    private lateinit var ivProfilePicture:ImageView

    private lateinit var fullName:String
    private lateinit var nickName:String
    private lateinit var email:String
    private lateinit var location:String
    private var profilePicturePath:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("hello")
        setContentView(R.layout.activity_show_profile)

        initializeView()
        initializeData()
        updateView()
    }

    private fun initializeData() {
        //TODO: initialize the variables reading from file
        fullName = UserKey.FULL_NAME_PLACEHOLDER
        nickName = UserKey.NICKNAME_PLACEHOLDER
        email = UserKey.EMAIL_PLACEHOLDER
        location = UserKey.LOCATION_PLACEHOLED

        val sharedPref = this?.getSharedPreferences(
            getString(R.string.preference_file_key), MODE_PRIVATE
        )
        val defaultValue = resources.getString(R.string.name)
        println(defaultValue)
    }

    private fun initializeView() {
        tvFullName = findViewById(R.id.fullname)
        tvNickname = findViewById(R.id.nickname)
        tvEmail = findViewById(R.id.email)
        tvLocation = findViewById(R.id.location)
        ivProfilePicture = findViewById(R.id.profilePicture)
    }

    private fun updateView() {
        tvFullName.text = fullName
        tvNickname.text = nickName
        tvEmail.text = email
        tvLocation.text = location
        if (profilePicturePath is String) {
            val bitMapProfilePicture = BitmapFactory.decodeFile(profilePicturePath)
            ivProfilePicture.setImageBitmap(bitMapProfilePicture)
        }
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
            UserKey.FULL_NAME_EXTRA_ID to tvFullName.text,
            UserKey.NICKNAME_EXTRA_ID to tvNickname.text,
            UserKey.EMAIL_EXTRA_ID to tvEmail.text,
            UserKey.LOCATION_EXTRA_ID to tvLocation.text,
            UserKey.PROFILE_PICTURE_PATH_EXTRA_ID to profilePicturePath
            )
        i.putExtras(b)

        startActivityForResult(i, 0)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        readResult(data)
        updateView()
        //TODO: prendere tutti i nomi ritornati dal result
    }

    private fun readResult(data: Intent?) {
        fullName = data?.getStringExtra(UserKey.FULL_NAME_EXTRA_ID) ?: UserKey.FULL_NAME_PLACEHOLDER
        nickName = data?.getStringExtra(UserKey.NICKNAME_EXTRA_ID) ?: UserKey.NICKNAME_PLACEHOLDER
        email = data?.getStringExtra(UserKey.EMAIL_EXTRA_ID) ?: UserKey.EMAIL_PLACEHOLDER
        location = data?.getStringExtra(UserKey.LOCATION_EXTRA_ID) ?: UserKey.LOCATION_PLACEHOLED
        profilePicturePath = data?.getStringExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID)
    }
}