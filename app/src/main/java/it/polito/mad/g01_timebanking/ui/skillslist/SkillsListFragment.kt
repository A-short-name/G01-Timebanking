package it.polito.mad.g01_timebanking.ui.skillslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.SkillAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentSkillsListBinding

class SkillsListFragment : Fragment() {
    private val skillsListViewModel : SkillsListViewModel by activityViewModels()

    private var _binding: FragmentSkillsListBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkillsListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewSkills = view.findViewById<RecyclerView>(R.id.skillRecyclerView)
        recyclerViewSkills.layoutManager = LinearLayoutManager(context)

        val emptySkillsText = view.findViewById<TextView>(R.id.emptySkillsText)

        skillsListViewModel.skillList.observe(this.viewLifecycleOwner){
            if (it.isEmpty()) {
                recyclerViewSkills.visibility = View.GONE
                emptySkillsText.visibility = View.VISIBLE
            } else {
                recyclerViewSkills.visibility = View.VISIBLE
                emptySkillsText.visibility = View.GONE
            }

            val adapter = SkillAdapter(it)
            recyclerViewSkills.adapter = adapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}