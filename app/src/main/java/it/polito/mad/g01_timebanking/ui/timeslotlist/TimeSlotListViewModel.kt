package it.polito.mad.g01_timebanking.ui.timeslotlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails

class TimeSlotListViewModel : ViewModel() {
    private val mAdvList : MutableList<AdvertisementDetails> = mutableListOf()

    private val pvtList = MutableLiveData<MutableList<AdvertisementDetails>>().also {
        it.value = mAdvList
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
        val pos = mAdvList.indexOf(a)
        if(pos != -1)
            mAdvList.removeAt(pos)

        mAdvList.add(a)
        pvtList.value = mAdvList
    }

    fun count() = mAdvList.size
}