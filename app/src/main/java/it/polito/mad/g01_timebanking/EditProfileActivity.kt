package it.polito.mad.g01_timebanking

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.util.*


class EditProfileActivity : AppCompatActivity() {



/*    override fun onBackPressed() {
        val i2 = Intent()
        prepareResult(i2)
        setResult(Activity.RESULT_OK,i2)
        //Salva in un file tutti i campi
        updatePreferences()

        Log.i(TAG,"profile preference wrote in local cache")
        super.onBackPressed() //finish is inside the onBackPressed()
    }

    private fun updatePreferences() {
        val u = UserInfo (
            fullName = ivFullName.text.toString(),
            nickname = ivNickname.text.toString(),
            email = ivEmail.text.toString(),
            location = ivLocation.text.toString(),
            biography = ivBiography.text.toString(),
            profilePicturePath = profilePicturePath,
            skills = skills
        )

        val gson = Gson();
        val serializedUser: String = gson.toJson(u)

        val sharedPref =
            this.getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE)
                ?: return
        with(sharedPref.edit()) {
            putString(getString(R.string.user_info), serializedUser)
            apply()
        }
    }

    private fun prepareResult(i2: Intent) {
        i2.putExtra(UserKey.FULL_NAME_EXTRA_ID, ivFullName.text.toString())
        i2.putExtra(UserKey.NICKNAME_EXTRA_ID, ivNickname.text.toString())
        i2.putExtra(UserKey.EMAIL_EXTRA_ID, ivEmail.text.toString())
        i2.putExtra(UserKey.LOCATION_EXTRA_ID, ivLocation.text.toString())
        i2.putExtra(UserKey.BIOGRAPHY_EXTRA_ID, ivBiography.text.toString())
        i2.putExtra(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID, profilePicturePath)
        val gson = Gson();
        val serializedSkills: String = gson.toJson(skills)
        i2.putExtra(UserKey.SKILLS_EXTRA_ID, serializedSkills)
    }*/

 /*
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID,profilePicturePath)
        val gson = Gson();
        val serializedSkills: String = gson.toJson(skills)
        outState.putString(UserKey.SKILLS_EXTRA_ID, serializedSkills)
        //alternativa, credo perdo l'ordine
        //outState.putStringArray(UserKey.SKILLS_EXTRA_ID, skills.toTypedArray())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        populateSkillGroup(savedInstanceState.getString(UserKey.SKILLS_EXTRA_ID))

        profilePicturePath = savedInstanceState.getString(UserKey.PROFILE_PICTURE_PATH_EXTRA_ID) ?: return

        if(profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER)
            FileHelper.readImage(profilePicturePath, profilePicture)
    }
    */

}

