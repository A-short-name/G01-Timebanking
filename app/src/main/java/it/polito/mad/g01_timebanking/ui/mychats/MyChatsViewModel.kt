package it.polito.mad.g01_timebanking.ui.mychats

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.adapters.MessageCollection
import it.polito.mad.g01_timebanking.adapters.SkillDetails

class MyChatsViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private var chatsListener: ListenerRegistration? = null

    private var _mychats : MutableList<MessageCollection> = mutableListOf()

    private val pvtMyChats = MutableLiveData<List<MessageCollection>>().apply {
        this.value = _mychats
    }

    val chatsList = pvtMyChats

    fun getIncomingRequestsChats() {
        chatsListener = db.collection("chats")
            .whereEqualTo("advOwnerUid",auth.currentUser!!.uid)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("Chats", "Error searching for chats. err:${e.message}")
                } else if (value!!.isEmpty) {
                    Log.d("Chats", "No chats")
                    _mychats = mutableListOf()
                } else {
                    for (doc in value) {
                        val chat = doc.toObject(MessageCollection::class.java)
                        _mychats = mutableListOf()
                        _mychats.add(chat)
                    }
                }
                pvtMyChats.value = _mychats
            }
    }

    fun getMyRequestsChats() {
        chatsListener = db.collection("chats")
            .whereEqualTo("requesterUid",auth.currentUser!!.uid)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("Chats", "Error searching for chats. err:${e.message}")
                } else if (value!!.isEmpty) {
                    Log.d("Chats", "No chats")
                    _mychats = mutableListOf()
                } else {
                    for (doc in value) {
                        val chat = doc.toObject(MessageCollection::class.java)
                        _mychats = mutableListOf()
                        _mychats.add(chat)
                    }
                }
                pvtMyChats.value = _mychats
            }
    }
}