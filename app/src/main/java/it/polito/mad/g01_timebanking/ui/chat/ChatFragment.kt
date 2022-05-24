package it.polito.mad.g01_timebanking.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.g01_timebanking.databinding.FragmentChatBinding

class ChatFragment : Fragment() {
    private val chatViewModel : ChatViewModel by activityViewModels()

    private var _binding: FragmentChatBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        setHasOptionsMenu(false)

        return binding.root
    }
}