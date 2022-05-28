package it.polito.mad.g01_timebanking.ui.review

// Temporary here. It will be moved in an adapter later on
data class Review (
        var chatId : String = "",
        var reviewId : String = "",
        var rating : Int = -1,
        var text : String = "",
        var fromUid : String = "",
        var toUid : String = "",
        var advId : String = "",
        var reviewerToName : String = "",
        var reviewerIsTheOwner : Boolean = false
        )