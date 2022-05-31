package it.polito.mad.g01_timebanking.ui.timeslotlistassigned

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

class TimeSlotListAssignedViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth

    private var timeslotsListener1: ListenerRegistration? = null
    private var timeslotsListener2: ListenerRegistration? = null

    private var mAdvList : MutableList<AdvertisementDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<AdvertisementDetails>>().also {
        it.value = mAdvList
        // Retrieve advertisements of the user
        getAdvertisementList()
    }

    val advList : LiveData<List<AdvertisementDetails>> = pvtList

    private fun getAdvertisementList() {
        timeslotsListener1 = db.collection("advertisements")
            .whereEqualTo("uid", auth.currentUser!!.uid)
            .whereEqualTo("sold", true)
            .addSnapshotListener { value, e ->
                var mySoldAdvList: MutableList<AdvertisementDetails> = mutableListOf()
                var myBoughtAdvList: MutableList<AdvertisementDetails> = mutableListOf()
                if (e != null) {
                    Log.d("Advertisement_Listener", "Error retrieving data.")
                } else if (value!!.isEmpty) {
                    Log.d("Advertisement_Listener", "No sold advertisements on database.")
                    //mAdvList = mutableListOf()
                    //pvtList.value = mAdvList
                } else {
                    val advertisements = mutableListOf<AdvertisementDetails>()

                    for (doc in value) {
                        advertisements.add(doc.toObject(AdvertisementDetails::class.java))
                    }
                    //mAdvList = advertisements
                    mySoldAdvList = advertisements
                    //pvtList.value = mAdvList
                }
                db.collection("advertisements")
                    .whereEqualTo("soldToUid", auth.currentUser!!.uid)
                    .whereEqualTo("sold", true)
                    .get().addOnSuccessListener { value2  ->
                         if (value2!!.isEmpty) {
                            Log.d("Advertisement_Listener", "No bought advertisements on database.")
                            //mAdvList = mutableListOf()
                            //pvtList.value = mAdvList
                        } else {
                            val advertisements = mutableListOf<AdvertisementDetails>()

                            for (doc in value2) {
                                advertisements.add(doc.toObject(AdvertisementDetails::class.java))
                            }
                            //mAdvList = advertisements
                            myBoughtAdvList = advertisements
                            //pvtList.value = mAdvList
                        }
                        pvtList.value = (mySoldAdvList.toSet() union myBoughtAdvList.toSet()).toList()
                    }.addOnFailureListener{
                        pvtList.value = (mySoldAdvList.toSet() union myBoughtAdvList.toSet()).toList()
                    }
            }
        //Second listener, triggered by the other query
        timeslotsListener2 = db.collection("advertisements")
            .whereEqualTo("soldToUid", auth.currentUser!!.uid)
            .whereEqualTo("sold", true)
            .addSnapshotListener { value, e ->
                var mySoldAdvList: MutableList<AdvertisementDetails> = mutableListOf()
                var myBoughtAdvList: MutableList<AdvertisementDetails> = mutableListOf()
                if (e != null) {
                    Log.d("Advertisement_Listener", "Error retrieving data.")
                } else if (value!!.isEmpty) {
                    Log.d("Advertisement_Listener", "No sold advertisements on database.")
                    //mAdvList = mutableListOf()
                    //pvtList.value = mAdvList
                } else {
                    val advertisements = mutableListOf<AdvertisementDetails>()

                    for (doc in value) {
                        advertisements.add(doc.toObject(AdvertisementDetails::class.java))
                    }
                    //mAdvList = advertisements
                    myBoughtAdvList = advertisements
                    //pvtList.value = mAdvList
                }
                db.collection("advertisements")
                    .whereEqualTo("uid", auth.currentUser!!.uid)
                    .whereEqualTo("sold", true)
                    .get().addOnSuccessListener { value2 ->
                        if (value2!!.isEmpty) {
                            Log.d("Advertisement_Listener", "No bought advertisements on database.")
                            //mAdvList = mutableListOf()
                            //pvtList.value = mAdvList
                        } else {
                            val advertisements = mutableListOf<AdvertisementDetails>()

                            for (doc in value2) {
                                advertisements.add(doc.toObject(AdvertisementDetails::class.java))
                            }
                            //mAdvList = advertisements
                            mySoldAdvList = advertisements
                            //pvtList.value = mAdvList
                        }
                        pvtList.value = (mySoldAdvList.toSet() union myBoughtAdvList.toSet()).toList()
                    }.addOnFailureListener{
                        pvtList.value = (mySoldAdvList.toSet() union myBoughtAdvList.toSet()).toList()
                    }
            }
    }

    override fun onCleared() {
        timeslotsListener1?.remove()
        timeslotsListener2?.remove()
    }
}