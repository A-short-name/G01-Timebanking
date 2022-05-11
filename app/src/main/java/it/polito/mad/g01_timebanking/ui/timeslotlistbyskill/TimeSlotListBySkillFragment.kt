package it.polito.mad.g01_timebanking.ui.timeslotlistbyskill

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
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotListBinding
import it.polito.mad.g01_timebanking.adapters.AdvertisementAdapter
import it.polito.mad.g01_timebanking.ui.timeslotdetails.TimeSlotDetailsViewModel


class TimeSlotListBySkillFragment : Fragment() {
    private val timeSlotDetailsViewModel : TimeSlotDetailsViewModel by activityViewModels()
    private val timeSlotListBySkillViewModel : TimeSlotListBySkillViewModel by activityViewModels()

    private var _binding: FragmentTimeSlotListBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSlotListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewAdv = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerViewAdv.layoutManager = LinearLayoutManager(context)

        val emptyAdvText = view.findViewById<TextView>(R.id.emptyAdvertisementsText)

        timeSlotListBySkillViewModel.advList.observe(this.viewLifecycleOwner){
            if (it.isEmpty()) {
                recyclerViewAdv.visibility = View.GONE
                emptyAdvText.visibility = View.VISIBLE
            } else {
                recyclerViewAdv.visibility = View.VISIBLE
                emptyAdvText.visibility = View.GONE
            }

            val adapter = AdvertisementAdapter(it, timeSlotDetailsViewModel, true)
            recyclerViewAdv.adapter = adapter
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}