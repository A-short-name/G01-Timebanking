package it.polito.mad.g01_timebanking.ui.timeslotlist

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import it.polito.mad.g01_timebanking.MainActivity
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import java.util.*

class TimeSlotListViewModel() : ViewModel() {

    private val pvtList = MutableLiveData<MutableList<AdvertisementDetails>>().also {
        it.value = mutableListOf(AdvertisementDetails(0,"First adv","Turin", Calendar.getInstance(), "3","Long description"),
            AdvertisementDetails(1, "Second adv","Milan", Calendar.getInstance(), "5","another description"))
/*
        val gson = Gson()

        val sharedPref = application.applicationContext.getSharedPreferences(
            R.string.preference_file_key.toString(), AppCompatActivity.MODE_PRIVATE
        )
        val s: String = sharedPref.getString(R.string.adv_list.toString(), "") ?: ""

        val l =  if(s!="") gson.fromJson(s, List::class.java) else mutableListOf("")
        it.value = l as MutableList<AdvertisementDetails>

 */
    }
    val advList : LiveData<MutableList<AdvertisementDetails>> = pvtList

    fun addOrUpdateElement(a: AdvertisementDetails){
        val pos = pvtList.value?.indexOf(a) ?: -1
        if(pos != -1)
            pvtList.value?.removeAt(pos)

        pvtList.value?.add(a)
    }

    fun count(): Int{
        return pvtList.value?.size ?: 0
    }
}