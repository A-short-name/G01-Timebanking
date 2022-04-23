package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import it.polito.mad.g01_timebanking.R
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

        // Enable menu options
        setHasOptionsMenu(true)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /**
         * The following code displays the fragment that
         * has the same ID as the menu item. (This code only works if the menu item
         * and the fragment have identical ID values.)
         */
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }
}