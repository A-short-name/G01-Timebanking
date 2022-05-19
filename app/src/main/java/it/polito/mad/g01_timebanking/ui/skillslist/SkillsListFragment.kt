package it.polito.mad.g01_timebanking.ui.skillslist

import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.SkillAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentSkillsListBinding
import it.polito.mad.g01_timebanking.ui.timeslotlistbyskill.TimeSlotListBySkillViewModel


class SkillsListFragment : Fragment() {
    private val skillsListViewModel : SkillsListViewModel by activityViewModels()
    private val tsListBySkillViewModel: TimeSlotListBySkillViewModel by activityViewModels()

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
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_menu, menu)
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

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}