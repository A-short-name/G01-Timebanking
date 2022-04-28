package it.polito.mad.g01_timebanking.helpers

import java.text.SimpleDateFormat
import java.util.*

class CalendarHelper {
    companion object {
        fun Calendar.fromTimeToString(is24HourFormat: Boolean): String? {
            val myFormat = if (is24HourFormat) "HH:mm" else "hh:mm a"

            val dateFormat = SimpleDateFormat(myFormat, Locale.US)

            return dateFormat.format(this.time)
        }

        fun Calendar.fromDateToString(): String? {
            val myFormat = "dd/MM/yyyy"
            val dateFormat = SimpleDateFormat(myFormat, Locale.US)
            return dateFormat.format(this.time)
        }
    }
}