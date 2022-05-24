package it.polito.mad.g01_timebanking.ui.chat

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.adapters.MessageCollection
import it.polito.mad.g01_timebanking.adapters.MessageDetails
import java.util.*

class ChatViewModel(val a: Application) : AndroidViewModel(a) {
    private var _messagesCollection : MessageCollection = MessageCollection()

    private val pvtMessagesCollection = MutableLiveData<MessageCollection>().also {
        it.value = _messagesCollection
    }

    val messagesCollection = pvtMessagesCollection

    private val pvtReceiverUid = MutableLiveData<String>().also {
        it.value = ""
    }

    private val receiverUid = pvtReceiverUid

    private val pvtAdvertisementId = MutableLiveData<String>().also {
        it.value = ""
    }

    private val advertisementId = pvtAdvertisementId

    private val pvtMessageText = MutableLiveData<String>().also {
        it.value = ""
    }

    val messageText = pvtMessageText

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private var messagesListener: ListenerRegistration? = null

    fun getMessagesList() {
        val chatId = "${auth.currentUser!!.uid}-${receiverUid.value!!}-${advertisementId.value!!}"
        messagesListener = db.collection("chats")
            .document(chatId)
            .addSnapshotListener { value, e ->
                if (e == null && value?.exists() == true) {
                    Log.d("Messages_Listener", "Data found on database. Updating!")
                    _messagesCollection = value.toMessageCollection()
                    pvtMessagesCollection.value = _messagesCollection
                } else if (e == null) {
                    Log.d("Messages_Listener", "Data not found on database.")
                    val newCollection = MessageCollection().apply {
                        this.advId = advertisementId.value!!
                        this.chatId = chatId
                    }
                    addOrUpdateData(newCollection, chatId)
                }
            }
    }

    private fun addOrUpdateData(newCollection: MessageCollection, chatId: String) {
        db.collection("chats").document(chatId).set(newCollection)
            .addOnSuccessListener {
                Log.d("InsertOrUpdateMesColl", "Success: $it")
                _messagesCollection = newCollection
                pvtMessagesCollection.value = _messagesCollection
            }
            .addOnFailureListener {
                Log.d("InsertOrUpdateMesColl", "Exception: ${it.message}")
                Toast.makeText(
                    a.applicationContext,
                    "Failed updating data. Try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun DocumentSnapshot.toMessageCollection(): MessageCollection {
        return this.toObject(MessageCollection::class.java) ?: MessageCollection()
    }

    override fun onCleared() {
        messagesListener?.remove()
    }

    fun setReceiverUid(uid: String) {
        pvtReceiverUid.value = uid
    }

    fun setAdvertisementId(id: String) {
        pvtAdvertisementId.value = id
    }

    fun sendMessage() {
        val message = MessageDetails(
            "",
            receiverUid.value!!,
            auth.currentUser!!.uid,
            Calendar.getInstance().time,
            messageText.value!!)

        _messagesCollection.messages.add(message)
        val chatId = "${auth.currentUser!!.uid}-${receiverUid.value!!}-${advertisementId.value!!}"
        addOrUpdateData(_messagesCollection, chatId)
        messageText.value = ""
    }

    fun setMessageText(text: String) {
        pvtMessageText.value = text
    }
}