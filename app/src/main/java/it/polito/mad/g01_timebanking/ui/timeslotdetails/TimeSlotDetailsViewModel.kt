package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.app.Application
import androidx.lifecycle.*
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import java.util.*

class TimeSlotDetailsViewModel(a:Application) : AndroidViewModel(a) {

    private val placeholderAdvertisementDetails = AdvertisementDetails(-1,
        "Placeholder title",
        "Placeholder location",
        Calendar.getInstance(),
        "Placeholder duration",
        "Placeholder description")

    /* This will be the valid advertisement used by Show */
    private val pvtAdvertisement = MutableLiveData<AdvertisementDetails>().also {
        it.value = placeholderAdvertisementDetails
    }

    val advertisement : LiveData<AdvertisementDetails> = pvtAdvertisement

    /* Ephemeral variables used from the Edit fragment to handle temporary save */

    private val pvtId = MutableLiveData<Int>().also {
        it.value = placeholderAdvertisementDetails.id
    }
    val id : LiveData<Int> = pvtId

    private val pvtTitle = MutableLiveData<String>().also {
        it.value = placeholderAdvertisementDetails.title
    }
    val title : LiveData<String> = pvtTitle

    private val pvtDescription = MutableLiveData<String>().also {
        it.value = placeholderAdvertisementDetails.description
    }
    val description : LiveData<String> = pvtDescription

    private val pvtLocation = MutableLiveData<String>().also {
        it.value = placeholderAdvertisementDetails.location
    }
    val location : LiveData<String> = pvtLocation

    private val pvtCalendar = MutableLiveData<Calendar>().also {
        it.value = placeholderAdvertisementDetails.calendar
    }
    val calendar : LiveData<Calendar> = pvtCalendar

    private val pvtDuration = MutableLiveData<String>().also {
        it.value = placeholderAdvertisementDetails.duration
    }
    val duration : LiveData<String> = pvtDuration

    fun setId(id: Int) {
        pvtId.value = id
    }

    fun setTitle(title: String) {
        pvtTitle.value = title
    }

    fun setLocation(location: String) {
        pvtLocation.value = location
    }

    fun setDescription(description: String) {
        pvtDescription.value = description
    }

    fun setDuration(duration: String) {
        pvtDuration.value = duration
    }

    fun setDateTime(calendar: Calendar) {
        pvtCalendar.value = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        }
    }

    fun setAdvertisement(adv: AdvertisementDetails) {
        pvtAdvertisement.value = adv
        pvtId.value = adv.id
        pvtTitle.value = adv.title
        pvtLocation.value = adv.location
        pvtDescription.value = adv.description
        pvtDuration.value = adv.duration
        pvtCalendar.value = adv.calendar
    }
}