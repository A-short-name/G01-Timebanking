package it.polito.mad.g01_timebanking.ui.mychats

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.adapters.MessageCollection

class MyChatsViewModel(val a: Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private var chatsListener: ListenerRegistration? = null

    private var _myChats : MutableList<MessageCollection> = mutableListOf()

    private val pvtMyChats = MutableLiveData<List<MessageCollection>>().apply {
        this.value = _myChats
        getAllMyChats()
    }

    val chatsList : LiveData<List<MessageCollection>> = pvtMyChats

    private val pvtSelectedTab = MutableLiveData<Int>().apply {
        this.value = 0
    }

    val selectedTab : LiveData<Int> = pvtSelectedTab

    private val pvtChatCounter = MutableLiveData<Int>().also {
        it.value = 0
    }

    val chatCounter : LiveData<Int> = pvtChatCounter

//    fun getIncomingRequestsChats() {
//        chatsListener?.remove()
//        chatsListener = db.collection("chats")
//            .whereEqualTo("advOwnerUid",auth.currentUser!!.uid)
//            .addSnapshotListener { value, e ->
//                _myChats = mutableListOf()
//                if (e != null) {
//                    Log.d("Chats", "Error searching for chats. err:${e.message}")
//                } else if (value!!.isEmpty) {
//                    Log.d("Chats", "No chats")
//                } else {
//                    for (doc in value) {
//                        val chat = doc.toObject(MessageCollection::class.java)
//                        _myChats.add(chat)
//                    }
//                }
//                pvtMyChats.value = _myChats
//            }
//    }
//
//    fun getMyRequestsChats() {
//        chatsListener?.remove()
//        chatsListener = db.collection("chats")
//            .whereEqualTo("requesterUid",auth.currentUser!!.uid)
//            .addSnapshotListener { value, e ->
//                _myChats = mutableListOf()
//                if (e != null) {
//                    Log.d("Chats", "Error searching for chats. err:${e.message}")
//                } else if (value!!.isEmpty) {
//                    Log.d("Chats", "No chats")
//                } else {
//                    for (doc in value) {
//                        val chat = doc.toObject(MessageCollection::class.java)
//                        _myChats.add(chat)
//                    }
//                }
//                pvtMyChats.value = _myChats
//            }
//    }

    private fun getAllMyChats() {
        chatsListener = db.collection("chats")
            .addSnapshotListener { value, e ->
                _myChats = mutableListOf()
                if (e != null) {
                    Log.d("Chats", "Error searching for chats. err:${e.message}")
                } else if (value!!.isEmpty) {
                    Log.d("Chats", "No chats")
                } else {
                    var counter = 0
                    for (doc in value) {
                        val chat = doc.toObject(MessageCollection::class.java)
                        if(chat.requesterUid == auth.currentUser!!.uid || chat.advOwnerUid == auth.currentUser!!.uid) {
                            if (chat.messages.isNotEmpty() && !(chat.messages.last().readBy.contains(auth.currentUser!!.uid)))
                                counter++
                            else if (chat.advOwnerUid == auth.currentUser!!.uid && chat.buyerHasRequested && !chat.ownerHasDecided)
                                counter++
                        }
                        val index = _myChats.indexOf(chat)
                        if(index == -1)
                            _myChats.add(chat)
                        else
                            _myChats[index] = chat
                    }
                    pvtChatCounter.value = counter
                    when(selectedTab.value!!) {
                        0 -> getIncomingRequestsChats()
                        else -> getMyRequestsChats()
                    }
                }
            }
    }

    fun getIncomingRequestsChats() {
        var localList = _myChats.toList()
        localList = localList.filter { it.advOwnerUid == auth.currentUser!!.uid }
        pvtMyChats.value = localList
    }

    fun getMyRequestsChats() {
        var localList = _myChats.toList()
        localList = localList.filter { it.requesterUid == auth.currentUser!!.uid }
        pvtMyChats.value = localList
    }

    override fun onCleared() {
        chatsListener?.remove()
        super.onCleared()
    }

    fun setSelectedTab(position: Int) {
        pvtSelectedTab.value = position
    }
}