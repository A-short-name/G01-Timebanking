package it.polito.mad.g01_timebanking

import com.google.firebase.firestore.PropertyName

data class Skill(
    @set:PropertyName("name")
    @get:PropertyName("name")
    var name:String = "no name",


    @set:PropertyName("usage_in_user")
    @get:PropertyName("usage_in_user")
    var usageInUser:Long = 0L,

    @set:PropertyName("usage_in_adv")
    @get:PropertyName("usage_in_adv")
    var usageInAdv: Long = 0L,
)
