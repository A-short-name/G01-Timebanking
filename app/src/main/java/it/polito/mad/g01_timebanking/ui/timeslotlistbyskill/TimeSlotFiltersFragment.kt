package it.polito.mad.g01_timebanking.ui.timeslotlistbyskill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotFiltersBinding
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotListBinding

class TimeSlotFiltersFragment : Fragment() {
    private val timeSlotListBySkillViewModel : TimeSlotListBySkillViewModel by activityViewModels()

    private var _binding: FragmentTimeSlotFiltersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSlotFiltersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}