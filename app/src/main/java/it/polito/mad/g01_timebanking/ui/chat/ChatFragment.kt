package it.polito.mad.g01_timebanking.ui.chat

import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.MessageAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentChatBinding
import it.polito.mad.g01_timebanking.ui.profile.ProfileViewModel

class ChatFragment : Fragment() {
    private val chatViewModel : ChatViewModel by activityViewModels()
    private val profileViewModel : ProfileViewModel by activityViewModels()

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

            if(chat.advOwnerUid == auth.currentUser!!.uid && !chat.hasDecided) {
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