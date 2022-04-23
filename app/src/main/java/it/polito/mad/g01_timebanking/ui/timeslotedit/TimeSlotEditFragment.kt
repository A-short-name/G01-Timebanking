package it.polito.mad.g01_timebanking.ui.timeslotedit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotEditBinding

class TimeSlotEditFragment : Fragment() {

    private var _binding: FragmentTimeSlotEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(TimeSlotEditViewModel::class.java)

        _binding = FragmentTimeSlotEditBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}