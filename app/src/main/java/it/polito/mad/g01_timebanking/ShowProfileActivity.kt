package it.polito.mad.g01_timebanking

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson

class ShowProfileActivity : AppCompatActivity() {
    private lateinit var tvFullName:TextView
    private lateinit var tvNickname:TextView
    private lateinit var tvEmail:TextView
    private lateinit var tvLocation:TextView
    private lateinit var ivProfilePicture:ImageView
    private lateinit var skillGroup:ChipGroup
    private lateinit var noSkills:TextView

    private lateinit var fullName:String
    private lateinit var nickName:String
    private lateinit var email:String
    private lateinit var location:String
    private lateinit var profilePicturePath:String

    private lateinit var skills : MutableSet<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        initializeView()
        initializeData()
        updateView()
    }

    private fun initializeData() {
        //initialize the variables reading from file
        skills = mutableSetOf()
        val gson = Gson()
        val sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), MODE_PRIVATE
        )
        val s: String = sharedPref.getString(getString(R.string.user_info), "" ) ?: ""

        val u =  if(s!="") gson.fromJson(s,UserInfo::class.java) else UserInfo()

        fullName = u.fullName
        nickName = u.nickname
        email = u.email
        location = u.location
        profilePicturePath = u.profilePicturePath
        skills = u.skills

        if(skills.isNotEmpty()) {
            skills.forEach {
                val chip = Chip(this)
                chip.text = it
                chip.isCheckable = false
                chip.isClickable = true
                skillGroup.addView(chip)
            }

            noSkills.isVisible = false;
        } else {
            noSkills.isVisible = true;
        }
    }

    private fun initializeView() {
        // Fetch views
        tvFullName = findViewById(R.id.fullname)
        tvNickname = findViewById(R.id.nickname)
        tvEmail = findViewById(R.id.email)
        tvLocation = findViewById(R.id.location)
        ivProfilePicture = findViewById(R.id.profilePicture)
        skillGroup = findViewById(R.id.skillgroup)
        noSkills = findViewById(R.id.noSkillsTextView)
    }

    private fun updateView() {
        tvFullName.text = fullName
        tvNickname.text = nickName
        tvEmail.text = email
        tvLocation.text = location
        if (profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
            readImage()
        }

        skillGroup.removeAllViews()

        if(skills.isEmpty())
            noSkills.isVisible = true
        else
            skills.forEach{
                val chip = Chip(this)
                chip.text = it
                chip.isCheckable = false
                chip.isClickable = true
                skillGroup.addView(chip)
            }.also{noSkills.isVisible = false}
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
        val gson = Gson();
        val serializedSkills: String = gson.toJson(skills)

        val i = Intent(this, EditProfileActivity::class.java)
        val b : Bundle = bundleOf(
            UserKey.FULL_NAME_EXTRA_ID to tvFullName.text,
            UserKey.NICKNAME_EXTRA_ID to tvNickname.text,
            UserKey.EMAIL_EXTRA_ID to tvEmail.text,
            UserKey.LOCATION_EXTRA_ID to tvLocation.text,
            UserKey.PROFILE_PICTURE_PATH_EXTRA_ID to profilePicturePath,
            UserKey.SKILLS_EXTRA_ID to serializedSkills
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
        location = data?.getStringExtra(UserKey.LOCATION_EXTRA_ID) ?: UserKey.LOCATION_PLACEHOLDER
        profilePicturePath = data?.getStringExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID) ?: UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER
        val gson = Gson()
        val serializedJson = data?.getStringExtra(UserKey.SKILLS_EXTRA_ID)
        skills = gson.fromJson(serializedJson, MutableSet::class.java) as MutableSet<String>

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(UserKey.FULL_NAME_EXTRA_ID, fullName)
        outState.putString(UserKey.NICKNAME_EXTRA_ID, nickName)
        outState.putString(UserKey.EMAIL_EXTRA_ID, email)
        outState.putString(UserKey.LOCATION_EXTRA_ID, location)
        outState.putString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID,profilePicturePath)

        val gson = Gson();
        val serializedSkills: String = gson.toJson(skills)
        outState.putString(UserKey.SKILLS_EXTRA_ID, serializedSkills)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        fullName = savedInstanceState.getString(UserKey.FULL_NAME_EXTRA_ID) ?: UserKey.FULL_NAME_PLACEHOLDER
        nickName = savedInstanceState.getString(UserKey.NICKNAME_EXTRA_ID) ?: UserKey.NICKNAME_PLACEHOLDER
        email = savedInstanceState.getString(UserKey.EMAIL_EXTRA_ID) ?: UserKey.EMAIL_PLACEHOLDER
        location = savedInstanceState.getString(UserKey.LOCATION_EXTRA_ID) ?: UserKey.LOCATION_PLACEHOLDER
        profilePicturePath = savedInstanceState.getString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID).toString()


        val gson = Gson();
        val serializedJson = savedInstanceState.getString(UserKey.SKILLS_EXTRA_ID)
        skills = gson.fromJson(serializedJson, MutableSet::class.java) as MutableSet<String>

        updateView()
    }

    private fun readImage() {
        val takenImage = BitmapFactory.decodeFile(profilePicturePath)

        val ei = ExifInterface(profilePicturePath)
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

        ivProfilePicture.setImageBitmap(rotatedBitmap)
    }

    fun rotateImage(source: Bitmap, angle: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(
            source, 0, 0, source.width, source.height,
            matrix, true
        )
    }
}