package it.polito.mad.g01_timebanking.ui.mychats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.MessageCollectionAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentMyChatsBinding

class MyChatsFragment : Fragment() {
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

        val incRequests = tabLayout.getTabAt(0)
        val myRequests = tabLayout.getTabAt(1)

        val recyclerViewMyChat = view.findViewById<RecyclerView>(R.id.myChatsRecyclerView)
        recyclerViewMyChat.layoutManager = LinearLayoutManager(context)

        adapter = MessageCollectionAdapter(listOf(), auth.currentUser!!.uid)
        recyclerViewMyChat.adapter = adapter
    }
}