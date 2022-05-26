package it.polito.mad.g01_timebanking.ui.chat

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    val messagesCollection : LiveData<MessageCollection> = pvtMessagesCollection

    private val pvtReceiverUid = MutableLiveData<String>().also {
        it.value = ""
    }

    val receiverUid : LiveData<String> = pvtReceiverUid

    private val pvtAdvertisement = MutableLiveData<AdvertisementDetails>().also {
        it.value = AdvertisementDetails()
    }

    val advertisement : LiveData<AdvertisementDetails> = pvtAdvertisement

    private val pvtMessageText = MutableLiveData<String>().also {
        it.value = ""
    }

    val messageText : LiveData<String> = pvtMessageText

    private val pvtChatId = MutableLiveData<String>().also {
        it.value = ""
    }

    val chatId : LiveData<String> = pvtChatId

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

                    db.collection("users")
                        .document(auth.currentUser!!.uid)
                        .get()
                        .addOnSuccessListener{ user ->
                            val userInfo = user.toObject(UserInfo::class.java) ?: UserInfo()

                            db.collection("users")
                                .document(pvtAdvertisement.value!!.uid)
                                .get()
                                .addOnSuccessListener { advOwner ->
                                    val userInfoOwner = advOwner.toObject(UserInfo::class.java) ?: UserInfo()

                                    val newCollection = MessageCollection().apply {
                                        this.advId = advertisement.value!!.id
                                        this.advTitle = pvtAdvertisement.value!!.title
                                        this.calendar = pvtAdvertisement.value!!.calendar
                                        this.duration = pvtAdvertisement.value!!.duration
                                        this.requesterName = userInfo.fullName
                                        this.chatId = chatId
                                        this.requesterUid = auth.currentUser!!.uid
                                        this.advOwnerUid = advertisement.value!!.uid
                                        this.advOwnerName = userInfoOwner.fullName
                                        this.ownerHasDecided = false
                                        this.accepted = false
                                    }
                                    addOrUpdateData(newCollection, chatId)
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
        collection.ownerHasDecided = true
        collection.accepted = accepted

        db.collection("chats").document(collection.chatId).set(collection)
            .addOnSuccessListener {
                Log.d("TESTING","Accepted is: ${collection.accepted}")
                _messagesCollection = collection
                pvtMessagesCollection.value = _messagesCollection

                db.collection("advertisements").document(collection.advId)
                    .get()
                    .addOnSuccessListener { adv ->
                        val advInfo = adv.toObject(AdvertisementDetails::class.java) ?: AdvertisementDetails()
                        advInfo.sold = true

                        db.collection("advertisements").document(collection.advId)
                            .set(advInfo)
                            .addOnSuccessListener { Log.d("InsertOrUpdateMesColl", "Success: $it") }

                        db.collection("chats")
                            .whereEqualTo("advId",advInfo.id)
                            .whereNotEqualTo("advId",collection.advId)
                            .get()
                            .addOnSuccessListener {
                                for(doc in it) {
                                    val chat = doc.toObject(MessageCollection::class.java)
                                    chat.accepted = false
                                    chat.buyerHasRequested = true
                                    chat.ownerHasDecided = true
                                    db.collection("chats").document(doc.id).set(chat)
                                }
                            }
                        // TODO: Decrement usage of adv in database
                    }
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

    fun sendMessage(chatId: String) {
        val message = MessageDetails(
            "",
            receiverUid.value!!,
            auth.currentUser!!.uid,
            Calendar.getInstance().time,
            messageText.value!!)

        _messagesCollection.messages.add(message)
        addOrUpdateData(_messagesCollection, chatId)
        pvtMessageText.value = ""
    }

    fun setMessageText(text: String) {
        pvtMessageText.value = text
    }

    fun setChatId(chatId: String) {
        pvtChatId.value = chatId
    }

    fun buyerTakesDecision(chat: MessageCollection, requested: Boolean) {
        chat.buyerHasRequested = requested

        db.collection("chats").document(chat.chatId).set(chat)
            .addOnSuccessListener {
                Log.d("InsertOrUpdateMesColl", "Success!")
                _messagesCollection = chat
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

    fun setChat(adv: AdvertisementDetails) {
        pvtReceiverUid.value = adv.uid
        pvtAdvertisement.value = adv
        pvtChatId.value = "${Firebase.auth.currentUser!!.uid}-${adv.uid}-${adv.id}"
    }
}