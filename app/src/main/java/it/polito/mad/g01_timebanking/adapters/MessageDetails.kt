package it.polito.mad.g01_timebanking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.ui.MessageDiffCallback
import java.util.*

data class MessageDetails (
    val messageId: String = "",
    var receiverUid: String = "",
    val senderUid: String = "",
    var timestamp: Date = Date(),
    var content: String = ""
) {
    override fun equals(other: Any?): Boolean {
        other as MessageDetails
        return messageId == other.messageId
    }

    override fun hashCode(): Int {
        return messageId.hashCode()
    }
}

enum class MessageType {
    SENT, RECEIVED
}

class MessageAdapter(
    private var data:List<MessageDetails>,
    private val currentUserUid: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(private val parent: ViewGroup,
                            v: View): RecyclerView.ViewHolder(v) {
        private val messageTextView = v.findViewById<TextView>(R.id.messageTextView)
        private val messengerTextView = v.findViewById<TextView>(R.id.messengerTextView)

        fun bind(message: MessageDetails) {
            messageTextView.text = message.content
            messengerTextView.text = message.timestamp.toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = data[position]

        return when(message.senderUid) {
            currentUserUid -> MessageType.SENT.ordinal
            else -> MessageType.RECEIVED.ordinal
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val v : View = when(viewType) {
            MessageType.SENT.ordinal -> {
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.single_sender_message_layout, parent,false)
            }
            else -> {
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.single_receiver_message_layout, parent,false)
            }
        }

        return MessageViewHolder(parent, v)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = data[position]

        holder.bind(message)
    }

    override fun getItemCount() = data.size

    fun setMessages(newMessages: List<MessageDetails>) {
        val diffs = DiffUtil.calculateDiff( MessageDiffCallback(data, newMessages) )
        data = newMessages.toList() //update data
        diffs.dispatchUpdatesTo(this) //animate UI
    }
}

data class MessageCollection(
    var chatId : String = "",
    var advId : String = "",
    val messages : MutableList<MessageDetails> = mutableListOf()
)