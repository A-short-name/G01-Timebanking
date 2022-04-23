package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotDetailsBinding


class TimeSlotDetailsFragment : Fragment() {

    private var _binding: FragmentTimeSlotDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[TimeSlotDetailsViewModel::class.java]

        _binding = FragmentTimeSlotDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.titleTimeSlotDetails
        homeViewModel.text.observe(viewLifecycleOwner) {
            //textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}