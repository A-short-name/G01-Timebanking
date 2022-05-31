package it.polito.mad.g01_timebanking.ui.timeslotlistsaved

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.adapters.MessageCollection

class TimeSlotListSavedViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private var timeslotsListener: ListenerRegistration? = null

    private var mAdvList : MutableList<AdvertisementDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<AdvertisementDetails>>().also {
        it.value = mAdvList
        // Retrieve advertisements of the user
        getAdvertisementList()
    }

    val advList : LiveData<List<AdvertisementDetails>> = pvtList

    private fun getAdvertisementList() {
        timeslotsListener = db.collection("advertisements")
            .whereArrayContains("savedBy", auth.currentUser!!.uid)
            .whereEqualTo("sold", false)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("Advertisement_Listener", "Error retrieving data.")
                } else if (value!!.isEmpty) {
                    Log.d("Advertisement_Listener", "No saved advertisements on database.")
                    mAdvList = mutableListOf()
                    pvtList.value = mAdvList
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
        timeslotsListener?.remove()
    }
}