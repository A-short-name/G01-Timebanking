package it.polito.mad.g01_timebanking.ui.timeslotlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.repositories.PreferencesRepository

class TimeSlotListViewModel(a: Application) : AndroidViewModel(a) {
    private val repo = PreferencesRepository(a)

    private var mAdvList : MutableList<AdvertisementDetails> = repo.advertisementList.toMutableList()

    private val pvtList = MutableLiveData<List<AdvertisementDetails>>().also {
        it.value = mAdvList
    }

    val advList : LiveData<List<AdvertisementDetails>> = pvtList

    fun addOrUpdateElement(a: AdvertisementDetails){
        val pos = mAdvList.indexOf(a)

        if(pos != -1){
            mAdvList.removeAt(pos)
            mAdvList.add(pos,a)
        } else
           mAdvList.add(a)

        repo.save(mAdvList.toList())
        pvtList.value = mAdvList
    }

    fun count() = mAdvList.size
}