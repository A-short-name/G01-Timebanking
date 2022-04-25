package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.app.Application
import androidx.lifecycle.*
import it.polito.mad.g01_timebanking.model.TimeSlotDetails
import java.util.*

class TimeSlotDetailsViewModel(a:Application) : AndroidViewModel(a) {
    val timeSlotDetails = MutableLiveData<TimeSlotDetails>().also {
        it.value = loadDetails()
    }

    private fun loadDetails() : TimeSlotDetails {
        // TODO: to be substituted with the reading of the correct advertisement
        return TimeSlotDetails(
            "No title",
            "No location",
            Calendar.getInstance(),
            "No duration",
            "No description")
    }

    fun setTitle(title: String) {
        timeSlotDetails.value!!.title = title
    }

    fun setLocation(location: String) {
        timeSlotDetails.value!!.location = location
    }

    fun setDescription(description: String) {
        timeSlotDetails.value!!.description = description
    }

    fun setDate(calendar: Calendar) {
        timeSlotDetails.value!!.calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR))
        timeSlotDetails.value!!.calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
        timeSlotDetails.value!!.calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
    }

    fun setTime(hour: Int, minute: Int) {
        timeSlotDetails.value!!.calendar.set(Calendar.HOUR, hour)
        timeSlotDetails.value!!.calendar.set(Calendar.MINUTE, minute)
    }
}