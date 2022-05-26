package it.polito.mad.g01_timebanking.ui.skillslist

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.SkillAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentSkillsListBinding
import it.polito.mad.g01_timebanking.ui.mychats.MyChatsViewModel
import it.polito.mad.g01_timebanking.ui.timeslotlistbyskill.TimeSlotListBySkillViewModel


class SkillsListFragment : Fragment() {
    private val skillsListViewModel : SkillsListViewModel by activityViewModels()
    private val tsListBySkillViewModel: TimeSlotListBySkillViewModel by activityViewModels()
    private val myChatsViewModel : MyChatsViewModel by activityViewModels()

    private var _binding: FragmentSkillsListBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsListBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        return binding.root
    }

    private var adapter: SkillAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewSkills = view.findViewById<RecyclerView>(R.id.skillRecyclerView)
        recyclerViewSkills.layoutManager = LinearLayoutManager(context)

        val emptySkillsText = view.findViewById<TextView>(R.id.emptySkillsText)

        adapter = SkillAdapter(listOf(), tsListBySkillViewModel)
        recyclerViewSkills.adapter = adapter

        skillsListViewModel.skillList.observe(this.viewLifecycleOwner){
            if (it.isEmpty()) {
                recyclerViewSkills.visibility = View.GONE
                emptySkillsText.visibility = View.VISIBLE
            } else {
                recyclerViewSkills.visibility = View.VISIBLE
                emptySkillsText.visibility = View.GONE
            }

            adapter!!.setSkills(it)
            adapter!!.dataFull = it.toList()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        inflater.inflate(R.menu.notifications_menu, menu)

        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter!!.filter.filter(newText)
                return false
            }
        })

        val menuHotList  = menu.findItem(R.id.menu_hotlist).actionView
        val uiHot = menuHotList.findViewById(R.id.hotlist_hot) as TextView

        myChatsViewModel.chatCounter.observe(this.viewLifecycleOwner) {
            if(it == 0) {
                uiHot.visibility = View.INVISIBLE
            } else {
                uiHot.visibility = View.VISIBLE
                uiHot.text = it.toString()
            }
        }

        menuHotList.setOnClickListener{
            findNavController().navigate(R.id.action_nav_skills_list_to_nav_my_chats)
        }

        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}