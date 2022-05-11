package it.polito.mad.g01_timebanking.ui.timeslotlist

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails

class TimeSlotListViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private lateinit var timeslotsListener: ListenerRegistration

    private var mAdvList : MutableList<AdvertisementDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<AdvertisementDetails>>().also {
        it.value = mAdvList
    }

    val advList : LiveData<List<AdvertisementDetails>> = pvtList

    fun getAdvertisementList() {
        timeslotsListener = db.collection("advertisements")
            .whereEqualTo("uid", auth.currentUser!!.uid)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("Advertisement_Listener", "Error retrieving data.")
                } else if (value!!.isEmpty) {
                    Log.d("Advertisement_Listener", "No advertisements on database.")
                } else {
                    val advertisements = mutableListOf<AdvertisementDetails>()

                    for (doc in value) {
                        advertisements.add(doc.toObject(AdvertisementDetails::class.java))
                    }
                    mAdvList = advertisements
                    pvtList.value = advertisements
                }
            }
    }

    fun addOrUpdateElement(toBeSaved: AdvertisementDetails) {
        val id = if (toBeSaved.id == UserKey.ID_PLACEHOLDER)
            db.collection("advertisements").document().id
        else
            toBeSaved.id

        toBeSaved.id = id

        db.collection("advertisements").document(id).set(toBeSaved)
            .addOnSuccessListener {
                Log.d("updateAdvertisement", "Success!")
                val pos = mAdvList.indexOf(toBeSaved)

                if (pos != -1) {
                    mAdvList.removeAt(pos)
                    mAdvList.add(pos, toBeSaved)
                } else
                    mAdvList.add(toBeSaved)

                pvtList.value = mAdvList
            }
            .addOnFailureListener {
                Log.d("updateAdvertisement", "Exception: ${it.message}")
                Toast.makeText(
                    a.applicationContext,
                    "Failed updating data. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun count() = mAdvList.size
}