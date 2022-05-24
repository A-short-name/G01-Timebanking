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
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
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

    val receiverUid = pvtReceiverUid

    private val pvtAdvertisement = MutableLiveData<AdvertisementDetails>().also {
        it.value = AdvertisementDetails()
    }

    val advertisement = pvtAdvertisement

    private val pvtMessageText = MutableLiveData<String>().also {
        it.value = ""
    }

    val messageText = pvtMessageText

    private val pvtChatId = MutableLiveData<String>().also {
        it.value = ""
    }

    val chatId = pvtChatId

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth = Firebase.auth
    private var messagesListener: ListenerRegistration? = null

    fun getMessagesList(chatId: String) {
        messagesListener = db.collection("chats")
            .document(chatId)
            .addSnapshotListener { value, e ->
                if (e == null && value?.exists() == true) {
                    Log.d("Messages_Listener", "Data found on database. Updating!")
                    _messagesCollection = value.toMessageCollection()
                    pvtMessagesCollection.value = _messagesCollection
                } else if (e == null) {
                    Log.d("Messages_Listener", "Data not found on database.")

                    db.collection("advertisements")
                        .document(advertisement.value!!.id)
                        .get()
                        .addOnSuccessListener{ adv ->
                            val advInfo = adv.toObject(AdvertisementDetails::class.java) ?: AdvertisementDetails()

                            db.collection("users")
                                .document(auth.currentUser!!.uid)
                                .get()
                                .addOnSuccessListener{ user ->
                                    val userInfo = user.toObject(UserInfo::class.java) ?: UserInfo()

                                    db.collection("users")
                                        .document(advInfo.uid)
                                        .get()
                                        .addOnSuccessListener { advOwner ->
                                            val userInfoOwner = advOwner.toObject(UserInfo::class.java) ?: UserInfo()

                                            val newCollection = MessageCollection().apply {
                                                this.advId = advertisement.value!!.id
                                                this.advTitle = advInfo.title
                                                this.requesterName = userInfo.fullName
                                                this.chatId = chatId
                                                this.requesterUid = auth.currentUser!!.uid
                                                this.advOwnerUid = advertisement.value!!.uid
                                                this.advOwnerName = userInfoOwner.fullName
                                                this.hasDecided = false
                                                this.accepted = false
                                            }
                                            addOrUpdateData(newCollection, chatId)
                                        }
                                    }

                        }
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

    fun takeDecision(collection: MessageCollection, accepted: Boolean) {
        collection.hasDecided = true
        collection.accepted = accepted
        db.collection("chats").document(collection.chatId).set(collection)
            .addOnSuccessListener {
                Log.d("InsertOrUpdateMesColl", "Success: $it")
                _messagesCollection = collection
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

    fun setAdvertisement(adv: AdvertisementDetails) {
        pvtAdvertisement.value = adv
    }

    fun sendMessage(chatId: String) {
        val message = MessageDetails(
            "",
            receiverUid.value!!,
            auth.currentUser!!.uid,
            Calendar.getInstance().time,
            messageText.value!!)

        _messagesCollection.messages.add(message)
        addOrUpdateData(_messagesCollection, chatId)
        messageText.value = ""
    }

    fun setMessageText(text: String) {
        pvtMessageText.value = text
    }

    fun setChatId(chatId: String) {
        this.chatId.value = chatId
    }
}