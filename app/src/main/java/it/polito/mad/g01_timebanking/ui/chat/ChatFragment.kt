package it.polito.mad.g01_timebanking.ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.MessageAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private val chatViewModel : ChatViewModel by activityViewModels()

    private val auth = Firebase.auth

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private var adapter: MessageAdapter? = null

    private lateinit var messageText: EditText
    private lateinit var sendImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        setHasOptionsMenu(false)

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        messageText = view.findViewById(R.id.messageTextEdit)
        sendImageView = view.findViewById(R.id.sendImageView)

        val acceptButton = view.findViewById<Button>(R.id.acceptRequestButton)
        val refuseButton = view.findViewById<Button>(R.id.refuseRequestButton)
        val recyclerViewChat = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        val llManager = LinearLayoutManager(context)
        llManager.stackFromEnd = true
        recyclerViewChat.layoutManager = llManager

        adapter = MessageAdapter(listOf(), auth.currentUser!!.uid)
        recyclerViewChat.adapter = adapter

        chatViewModel.messagesCollection.observe(this.viewLifecycleOwner){ chat ->
            val requestLayout = view.findViewById<LinearLayout>(R.id.requestToAcceptLayout)
            val messageAcceptTextView = view.findViewById<TextView>(R.id.messageAcceptTextView)

            val isTheOwner = chat.advOwnerUid == auth.currentUser!!.uid

            if(!isTheOwner && !chat.buyerHasRequested && !chat.ownerHasDecided) {
                messageAcceptTextView.text = "Do you want to send a request for this advertisement?"
                acceptButton.text = "YES"
                refuseButton.text = "NOT YET"

                acceptButton.setOnClickListener {
                    chatViewModel.buyerTakesDecision(chat,true)
                }

                refuseButton.setOnClickListener {
                    requestLayout.visibility = View.GONE
                }

                requestLayout.visibility = View.VISIBLE
            } else if (!isTheOwner && chat.buyerHasRequested) {
                requestLayout.visibility = View.GONE
            } else if(isTheOwner && !chat.ownerHasDecided && chat.buyerHasRequested) {
                messageAcceptTextView.text = "Request arrived! \nDo you want to accept this request?"
                acceptButton.text = "ACCEPT"
                refuseButton.text = "REFUSE"

                acceptButton.setOnClickListener {
                    chatViewModel.takeDecision(chat,true)
                }

                refuseButton.setOnClickListener {
                    chatViewModel.takeDecision(chat, false)
                }
                requestLayout.visibility = View.VISIBLE
            }
            else
                requestLayout.visibility = View.GONE

            adapter!!.setMessages(chat.messages)
        }

        chatViewModel.messageText.observe(this.viewLifecycleOwner) {
            messageText.setText(it)
        }

        chatViewModel.chatId.observe(this.viewLifecycleOwner) { chatId ->
            chatViewModel.getMessagesList(chatId)

            sendImageView.setOnClickListener {
                chatViewModel.setMessageText(messageText.text.toString())
                chatViewModel.sendMessage(chatId)
            }
        }
    }
}