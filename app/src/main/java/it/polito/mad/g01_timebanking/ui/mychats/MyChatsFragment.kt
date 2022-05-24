package it.polito.mad.g01_timebanking.ui.mychats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.MessageCollectionAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentMyChatsBinding
import it.polito.mad.g01_timebanking.ui.chat.ChatViewModel

class MyChatsFragment : Fragment() {
    private val myChatsViewModel : MyChatsViewModel by activityViewModels()
    private val chatViewModel : ChatViewModel by activityViewModels()

    private var _binding: FragmentMyChatsBinding? = null
    private val auth = Firebase.auth

    private val binding get() = _binding!!

    private lateinit var tabLayout : TabLayout

    private var adapter: MessageCollectionAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyChatsBinding.inflate(inflater, container, false)

        setHasOptionsMenu(false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = view.findViewById(R.id.myChatsTabLayout)

        tabLayout.addOnTabSelectedListener(MyOnTabSelectedListener(myChatsViewModel))
        val incRequests = tabLayout.getTabAt(0)
        val myRequests = tabLayout.getTabAt(1)

        val recyclerViewMyChat = view.findViewById<RecyclerView>(R.id.myChatsRecyclerView)
        recyclerViewMyChat.layoutManager = LinearLayoutManager(context)

        adapter = MessageCollectionAdapter(listOf(), myChatsViewModel, chatViewModel, findNavController())
        recyclerViewMyChat.adapter = adapter

        val emptyChatsText = view.findViewById<TextView>(R.id.emptyChatsTextView)
        myChatsViewModel.chatsList.observe(this.viewLifecycleOwner) {
            if (it.isEmpty()) {
                emptyChatsText.visibility = View.VISIBLE
            } else {
                emptyChatsText.visibility = View.GONE
            }

            adapter!!.setMyChats(it)
        }
    }
}

class MyOnTabSelectedListener(private val vm : MyChatsViewModel) : TabLayout.OnTabSelectedListener {
    override fun onTabSelected(tab: TabLayout.Tab?) {
        if(tab == null)
            return

        when(tab.position) {
            0 -> vm.getIncomingRequestsChats()
            else -> vm.getMyRequestsChats()
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

}