package it.polito.mad.g01_timebanking.ui.timeslotlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails

class TimeSlotListViewModel : ViewModel() {
    private var mAdvList : MutableList<AdvertisementDetails> = mutableListOf()

    private val pvtList = MutableLiveData<MutableList<AdvertisementDetails>>().also {
        it.value = mAdvList
    }

    val advList : LiveData<MutableList<AdvertisementDetails>> = pvtList

    fun initializeAdvList(list:MutableList<AdvertisementDetails>) {
        mAdvList = list
        pvtList.value = mAdvList
    }

    fun addOrUpdateElement(a: AdvertisementDetails){
        val pos = mAdvList.indexOf(a)

        if(pos != -1){
            mAdvList.removeAt(pos)
            mAdvList.add(pos,a)
        } else
           mAdvList.add(a)

        pvtList.value = mAdvList
    }

    fun count() = mAdvList.size
}