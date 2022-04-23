package it.polito.mad.g01_timebanking.model

import java.util.*

data class TimeSlotDetails (
    var title: String,
    var location: String,
    var calendar: Calendar,
    var duration: String,
    var description: String
    )