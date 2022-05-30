package it.polito.mad.g01_timebanking.ui.review

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.adapters.MessageCollection
import it.polito.mad.g01_timebanking.adapters.ReviewDetails

class ReviewViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private val pvtReview = MutableLiveData<ReviewDetails>().also {
        it.value = ReviewDetails()
    }

    val review : LiveData<ReviewDetails> = pvtReview

    fun sendReview(review: ReviewDetails, reviewText: String, rating: Int) {
        review.text = reviewText
        review.rating = rating
        db.collection("reviews")
            .document(review.reviewId)
            .set(review)
            .addOnSuccessListener {
                Log.d("Send_Review","Success!")
                db.collection("chats")
                    .document(review.chatId)
                    .get()
                    .addOnSuccessListener {
                        val chat = it.toObject(MessageCollection::class.java) ?: MessageCollection()
                        if(review.toUid == chat.advOwnerUid)
                            chat.requesterHasReviewed = true
                        else
                            chat.ownerHasReviewed = true

                        db.collection("chats").document(review.chatId).set(chat).addOnSuccessListener {
                            Log.d("Has_Reviewed", "Success!")
                        }
                    }
            }
    }

    fun setReview(newReview: ReviewDetails) {
        pvtReview.value = newReview
    }

}

private fun DocumentSnapshot.toReview(): ReviewDetails {
    return this.toObject(ReviewDetails::class.java) ?: ReviewDetails()
}