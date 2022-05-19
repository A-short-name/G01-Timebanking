package it.polito.mad.g01_timebanking.ui.timeslotlistbyskill

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.adapters.SkillDetails
import java.util.*

class TimeSlotListBySkillViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var timeslotsBySkillListener: ListenerRegistration? = null

    private var mAdvList : MutableList<AdvertisementDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<AdvertisementDetails>>().also {
        it.value = mAdvList
    }

    val advList : LiveData<List<AdvertisementDetails>> = pvtList

    /* Filters */
    private var isFiltered = false

    private var mlocationFilter : String = ""

    private val pvtLocationFilter = MutableLiveData<String>().also {
        it.value = mlocationFilter
    }

    val locationFilter : LiveData<String> = pvtLocationFilter

    private var mFromCalendarFilter : Calendar? = null

    private val pvtFromCalendarFilter = MutableLiveData<Calendar?>().also {
        it.value = mFromCalendarFilter
    }
    val fromCalendarFilter : LiveData<Calendar?> = pvtFromCalendarFilter

    private var mToCalendarFilter : Calendar? = null

    private val pvtToCalendarFilter = MutableLiveData<Calendar?>().also {
        it.value = mToCalendarFilter
    }
    val toCalendarFilter : LiveData<Calendar?> = pvtToCalendarFilter

    private var mDurationFilter = ""

    private val pvtDurationFilter = MutableLiveData<String>().also {
        it.value = mDurationFilter
    }

    val durationFilter : LiveData<String> = pvtDurationFilter

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
        timeslotsBySkillListener?.remove()
    }

    fun sortAtoZ() {
        val localList = if(!isFiltered) mAdvList else pvtList.value!!.toMutableList()
        localList.sortBy { it.title }
        pvtList.value = localList
    }

    fun sortZtoA() {
        val localList = if(!isFiltered) mAdvList else pvtList.value!!.toMutableList()
        localList.sortBy { it.title }
        pvtList.value = localList.reversed()
    }

    fun sortMostRecents() {
        val localList = if(!isFiltered) mAdvList else pvtList.value!!.toMutableList()
        localList.sortBy { it.calendar.time }
        pvtList.value = localList.reversed()
    }

    fun sortLessRecents() {
        val localList = if(!isFiltered) mAdvList else pvtList.value!!.toMutableList()
        localList.sortBy { it.calendar.time }
        pvtList.value = localList
    }

    fun setDateTime(isFromDate: Boolean, calendar: Calendar?) {
        if(calendar == null) {
            if(isFromDate) {
                pvtFromCalendarFilter.value = null
            } else {
                pvtToCalendarFilter.value = null
            }

            return
        }

        val settedCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        }

        if(isFromDate)
            pvtFromCalendarFilter.value = settedCalendar
        else
            pvtToCalendarFilter.value = settedCalendar
    }

    fun setLocationFilter(location: String) {
        pvtLocationFilter.value = location
    }

    fun setDurationFilter(duration: String) {
        pvtDurationFilter.value = duration
    }

    fun applyFilters() {
        isFiltered = true
        var filteredList = mAdvList.toList()

        if(locationFilter.value!!.isNotEmpty())
            filteredList = filteredList.filter { it.location.contains(locationFilter.value!!.toString()) }

        if(durationFilter.value!! != "Disabled") {
            filteredList = filteredList.filter {
                val actHours = it.duration.split(":")[0].toInt()
                actHours <= durationFilter.value!!.toInt()
            }
        }

        if(fromCalendarFilter.value != null) {
            filteredList = filteredList.filter {
                it.calendar >= fromCalendarFilter.value!!.time
            }
        }

        if (toCalendarFilter.value != null) {
            filteredList = filteredList.filter {
                it.calendar <= toCalendarFilter.value!!.time
            }
        }

        pvtList.value = filteredList
    }

    fun removeFilters() {
        isFiltered = false
        pvtLocationFilter.value = ""
        pvtDurationFilter.value = ""
        pvtFromCalendarFilter.value = null
        pvtToCalendarFilter.value = null

        pvtList.value = mAdvList
    }
}