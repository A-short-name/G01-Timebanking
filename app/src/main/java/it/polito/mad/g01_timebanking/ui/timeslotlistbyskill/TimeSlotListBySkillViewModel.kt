package it.polito.mad.g01_timebanking.ui.timeslotlistbyskill

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.g01_timebanking.adapters.AdvertisementAdapter
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.adapters.SkillDetails
import java.util.*

class TimeSlotListBySkillViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var timeslotsBySkillListener: ListenerRegistration

    private var mAdvList : MutableList<AdvertisementDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<AdvertisementDetails>>().also {
        it.value = mAdvList
    }

    val advList : LiveData<List<AdvertisementDetails>> = pvtList

    fun setAdvertisementsBySkill(skill: SkillDetails) {
        timeslotsBySkillListener = db.collection("advertisements")
            .whereArrayContains("skills", skill.name)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("TSBySkill_Listener", "Error retrieving data.")
                } else if (value!!.isEmpty) {
                    Log.d("TSBySkill_Listener", "No advertisements on database.")
                    mAdvList = mutableListOf()
                    pvtList.value = mutableListOf()
                } else {
                    val advertisements = mutableListOf<AdvertisementDetails>()

                    for (doc in value) {
                        advertisements.add(doc.toObject(AdvertisementDetails::class.java))
                    }
                    mAdvList = advertisements
                    pvtList.value = mAdvList
                }
            }
    }

    override fun onCleared() {
        timeslotsBySkillListener.remove()
    }

    fun sortAtoZ() {
        val localList = mAdvList
        localList.sortBy { it.title }
        pvtList.value = localList
    }

    fun sortZtoA() {
        val localList = mAdvList
        localList.sortBy { it.title }
        pvtList.value = localList.reversed()
    }

    fun sortMostRecents() {
        val localList = mAdvList
        localList.sortBy { it.calendar.time }
        pvtList.value = localList.reversed()
    }

    fun sortLessRecents() {
        val localList = mAdvList
        localList.sortBy { it.calendar.time }
        pvtList.value = localList
    }

    fun applyFilters(filteredList: List<AdvertisementDetails>) {
        pvtList.value = filteredList
    }
}