package it.polito.mad.g01_timebanking.ui.reviewslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.adapters.ReviewDetails

class ReviewsListViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private lateinit var reviewsListener: ListenerRegistration

    private var mRevList : MutableList<ReviewDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<ReviewDetails>>().also {
        it.value = mRevList
    }

    val revList : LiveData<List<ReviewDetails>> = pvtList

    private val pvtSelectedTab = MutableLiveData<Int>().apply {
        this.value = 0
    }

    val selectedTab : LiveData<Int> = pvtSelectedTab


    fun setReviews(uid: String){
        reviewsListener = db.collection("reviews")
            .whereEqualTo("toUid", uid)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("ReviewList_Listener", "Error retrieving data.")
                } else if (value!!.isEmpty) {
                    Log.d("ReviewList_Listener", "No reviews on database.")
                    mRevList = mutableListOf()
                    pvtList.value = mRevList
                } else {
                    val reviews = mutableListOf<ReviewDetails>()

                    for (doc in value) {
                        reviews.add(doc.toObject(ReviewDetails::class.java))
                    }
                    mRevList = reviews
                    when(selectedTab.value!!) {
                        0 -> showBuyerReviews()
                        else -> showSellerReviews()
                    }
                    //pvtList.value = mRevList
                }
            }
    }

    fun setSelectedTab(position: Int) {
        pvtSelectedTab.value = position
    }

    fun showBuyerReviews(){
        var localList = mRevList.toList()
        localList = localList.filter { it.reviewerIsTheOwner }
        pvtList.value = localList
    }

    fun showSellerReviews(){
        var localList = mRevList.toList()
        localList = localList.filter { !it.reviewerIsTheOwner }
        pvtList.value = localList
    }


    override fun onCleared() {
        reviewsListener.remove()
    }
}