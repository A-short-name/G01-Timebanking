package it.polito.mad.g01_timebanking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.ui.MessageCollectionDiffCallback
import it.polito.mad.g01_timebanking.ui.chat.ChatViewModel
import it.polito.mad.g01_timebanking.ui.mychats.MyChatsViewModel

data class MessageCollection (
    var chatId : String = "",
    var advId : String = "",
    var advTitle : String = "",
    var advOwnerUid : String = "",
    var requesterUid : String = "",
    var requesterName : String = "",
    var hasDecided : Boolean = false,
    var accepted : Boolean = false,
    val messages : MutableList<MessageDetails> = mutableListOf()
)

class MessageCollectionAdapter(
    private var data:List<MessageCollection>,
    private val myChatsViewModel: MyChatsViewModel,
    private val chatViewModel: ChatViewModel,
    private val navController: NavController
) : RecyclerView.Adapter<MessageCollectionAdapter.MessageCollectionViewHolder>() {

    class MessageCollectionViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val chatCardView = v.findViewById<CardView>(R.id.chatCardView)
        private val chatAdvertisementTitle = v.findViewById<TextView>(R.id.chatAdvertisementTitle)
        private val fromTextView = v.findViewById<TextView>(R.id.fromTextView)
        private val lastMessageTextView = v.findViewById<TextView>(R.id.lastMessageTextView)

        fun bind (messageCollection: MessageCollection, navController: NavController, chatViewModel: ChatViewModel) {
            chatAdvertisementTitle.text = messageCollection.advTitle
            fromTextView.text = messageCollection.requesterName
            val messages = messageCollection.messages

            if(messages.size != 0)
                lastMessageTextView.text = messages[messages.lastIndex].content

            chatCardView.setOnClickListener {
                chatViewModel.setChatId(messageCollection.chatId)
                navController.navigate(R.id.action_nav_my_chats_to_nav_chat_list)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageCollectionViewHolder {
        val v : View =
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.single_chat_layout, parent,false)

        return MessageCollectionViewHolder(v)
    }

    override fun onBindViewHolder(holder: MessageCollectionViewHolder, position: Int) {
        val messageCollection = data[position]

        holder.bind(messageCollection, navController, chatViewModel)
    }

    override fun getItemCount() = data.size

    fun setMyChats(newMessageCollection: List<MessageCollection>) {
        val diffs = DiffUtil.calculateDiff( MessageCollectionDiffCallback(data, newMessageCollection) )
        data = newMessageCollection.toList() //update data
        diffs.dispatchUpdatesTo(this) //animate UI
    }
}