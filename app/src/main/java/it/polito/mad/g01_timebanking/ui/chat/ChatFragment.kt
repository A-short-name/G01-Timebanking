package it.polito.mad.g01_timebanking.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val recyclerViewChat = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerViewChat.layoutManager = LinearLayoutManager(context)

        adapter = MessageAdapter(listOf(), auth.currentUser!!.uid)
        recyclerViewChat.adapter = adapter

        chatViewModel.messageList.observe(this.viewLifecycleOwner){
            adapter!!.setMessages(it)
        }
    }
}