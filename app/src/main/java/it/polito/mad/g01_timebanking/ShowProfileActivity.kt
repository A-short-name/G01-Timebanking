/*
package it.polito.mad.g01_timebanking

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson


class ShowProfileActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ShowProfileActivity"
    }
    private lateinit var scrollView:ScrollView
    private lateinit var frameView:FrameLayout
    private lateinit var tvFullName:TextView
    private lateinit var tvNickname:TextView
    private lateinit var tvEmail:TextView
    private lateinit var tvLocation:TextView
    private lateinit var tvBiography:TextView
    private lateinit var ivProfilePicture:ImageView
    private lateinit var skillGroup:ChipGroup
    private lateinit var noSkills:TextView

    private lateinit var fullName:String
    private lateinit var nickName:String
    private lateinit var email:String
    private lateinit var location:String
    private lateinit var biography:String
    private lateinit var profilePicturePath:String

    private lateinit var skills : MutableSet<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        initializeView()
        //the only way to set height image to 1/3 of the screen is programmatically
        //This is ue to the fact that we use a scroll view with a bio with variable length
        arrangeViewByRatio()

        initializeData()
        updateView()
        if(!FileHelper.isExternalStorageWritable())
            Log.e(TAG, "No external volume mounted")
    }

    private fun arrangeViewByRatio() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            scrollView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    frameView.post {
                        frameView.layoutParams =
                            LinearLayout.LayoutParams(scrollView.width, scrollView.height / 3)
                    }
                    scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    //Resize consequently cardView with image
                    val cardView = findViewById<CardView>(R.id.imageCard)
                    val relativeDimension = scrollView.height / 3 - 32
                    //I want a square box for the image that doesn't fit all the space in the parent frameView
                    cardView.layoutParams.width = relativeDimension
                    cardView.layoutParams.height = relativeDimension
                    //different from before because cardView doesn't work with LinearLayout.LayoutP....
                }
            })
        }

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
        biography = u.biography
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
            noSkills.isVisible = false
        } else {
            noSkills.isVisible = true
        }
    }

    private fun initializeView() {
        // Fetch views
        scrollView = findViewById(R.id.sv)
        frameView = findViewById(R.id.frameView1)
        tvFullName = findViewById(R.id.fullname)
        tvNickname = findViewById(R.id.nickname)
        tvEmail = findViewById(R.id.email)
        tvLocation = findViewById(R.id.location)
        tvBiography = findViewById(R.id.biography)
        ivProfilePicture = findViewById(R.id.profilePicture)
        skillGroup = findViewById(R.id.skillgroup)
        noSkills = findViewById(R.id.noSkillsTextView)
    }

    private fun updateView() {
        tvFullName.text = fullName
        tvNickname.text = nickName
        tvEmail.text = email
        tvLocation.text = location
        tvBiography.text = biography
        if (profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
            FileHelper.readImage(profilePicturePath, ivProfilePicture)
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
        return super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val gson = Gson()
        val serializedSkills: String = gson.toJson(skills)

        val i = Intent(this, EditProfileActivity::class.java)
        val b : Bundle = bundleOf(
            UserKey.FULL_NAME_EXTRA_ID to tvFullName.text,
            UserKey.NICKNAME_EXTRA_ID to tvNickname.text,
            UserKey.EMAIL_EXTRA_ID to tvEmail.text,
            UserKey.LOCATION_EXTRA_ID to tvLocation.text,
            UserKey.BIOGRAPHY_EXTRA_ID to tvBiography.text,
            UserKey.PROFILE_PICTURE_PATH_EXTRA_ID to profilePicturePath,
            UserKey.SKILLS_EXTRA_ID to serializedSkills
            )
        i.putExtras(b)

        startActivityForResult(i, UserKey.EDIT_ACTIVITY_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode){
            UserKey.EDIT_ACTIVITY_REQUEST -> {
                if(resultCode == RESULT_OK) {
                    readResult(data)
                    updateView()
                }
                else
                    Log.e(TAG,"activity result not OK,\n\tresultCode: $resultCode\n\trequestCode: $requestCode ")
            }
            else -> { Log.e(TAG,"activity request code not recognized, requestCode: $requestCode")
            }
        }

    }

    private fun readResult(data: Intent?) {
        fullName = data?.getStringExtra(UserKey.FULL_NAME_EXTRA_ID) ?: UserKey.FULL_NAME_PLACEHOLDER
        nickName = data?.getStringExtra(UserKey.NICKNAME_EXTRA_ID) ?: UserKey.NICKNAME_PLACEHOLDER
        email = data?.getStringExtra(UserKey.EMAIL_EXTRA_ID) ?: UserKey.EMAIL_PLACEHOLDER
        location = data?.getStringExtra(UserKey.LOCATION_EXTRA_ID) ?: UserKey.LOCATION_PLACEHOLDER
        biography = data?.getStringExtra(UserKey.BIOGRAPHY_EXTRA_ID) ?: UserKey.BIOGRAPHY_PLACEHOLDER
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
        outState.putString(UserKey.BIOGRAPHY_EXTRA_ID, biography)
        outState.putString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID,profilePicturePath)

        val gson = Gson()
        val serializedSkills: String = gson.toJson(skills)
        outState.putString(UserKey.SKILLS_EXTRA_ID, serializedSkills)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        fullName = savedInstanceState.getString(UserKey.FULL_NAME_EXTRA_ID) ?: UserKey.FULL_NAME_PLACEHOLDER
        nickName = savedInstanceState.getString(UserKey.NICKNAME_EXTRA_ID) ?: UserKey.NICKNAME_PLACEHOLDER
        email = savedInstanceState.getString(UserKey.EMAIL_EXTRA_ID) ?: UserKey.EMAIL_PLACEHOLDER
        location = savedInstanceState.getString(UserKey.LOCATION_EXTRA_ID) ?: UserKey.LOCATION_PLACEHOLDER
        biography = savedInstanceState.getString(UserKey.BIOGRAPHY_EXTRA_ID) ?: UserKey.BIOGRAPHY_PLACEHOLDER
        profilePicturePath = savedInstanceState.getString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID).toString()


        val gson = Gson();
        val serializedJson = savedInstanceState.getString(UserKey.SKILLS_EXTRA_ID)
        skills = gson.fromJson(serializedJson, MutableSet::class.java) as MutableSet<String>

        updateView()
    }

}
*/