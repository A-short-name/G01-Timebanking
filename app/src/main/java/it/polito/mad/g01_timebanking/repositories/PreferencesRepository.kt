package it.polito.mad.g01_timebanking.repositories

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import java.lang.reflect.Type

class PreferencesRepository(val a: Application) {
    private val gson = Gson()
    private val context = a.applicationContext

    private val sharedPref = context.getSharedPreferences(
        context.getString(R.string.preference_file_key), AppCompatActivity.MODE_PRIVATE
    )

    val userInfo = readUserInfo()
    val advertisementList = readAdvertisementList()

    private fun readUserInfo() : UserInfo {
        val s: String = sharedPref?.getString(
            context.getString(R.string.user_info),
            "") ?: ""
        return  if(s!="") gson.fromJson(s, UserInfo::class.java) else UserInfo()
    }

    private fun readAdvertisementList() : List<AdvertisementDetails> {
        val typeMyType: Type = object : TypeToken<ArrayList<AdvertisementDetails?>?>() {}.type

        val s: String = sharedPref.getString(
            context.getString(R.string.adv_list),
            "") ?: ""

        return if(s!="") gson.fromJson(s, typeMyType) as List<AdvertisementDetails> else listOf()
    }

    fun <T> save(toBeSaved: T) {
        val serializedObj: String = gson.toJson(toBeSaved)
        val requiredKey = when (toBeSaved) {
            is UserInfo -> R.string.user_info
            is List<*> -> R.string.adv_list
            else -> throw Exception("Not recognized object") // Should not happen
        }

        with(sharedPref.edit()) {
            putString(
                context.getString(requiredKey),
                serializedObj)
            apply()
        }
    }
}