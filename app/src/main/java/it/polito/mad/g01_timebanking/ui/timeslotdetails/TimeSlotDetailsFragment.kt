package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotDetailsBinding
import java.text.SimpleDateFormat
import java.util.*


class TimeSlotDetailsFragment : Fragment() {
    private val timeSlotDetailsViewModel : TimeSlotDetailsViewModel by activityViewModels()

    // Views to be handled
    private lateinit var textViewTitle: EditText
    private lateinit var textViewLocation: EditText
    private lateinit var textViewDuration: EditText
    private lateinit var textViewDate: EditText
    private lateinit var textViewTime: EditText
    private lateinit var textViewDescription: EditText

    private var _binding: FragmentTimeSlotDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTimeSlotDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Enable menu options
        setHasOptionsMenu(true)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        textViewTitle = view.findViewById(R.id.titleShowText)
        textViewLocation = view.findViewById(R.id.locationShowText)
        textViewDuration = view.findViewById(R.id.durationShowText)
        textViewDate = view.findViewById(R.id.dateShowText)
        textViewTime = view.findViewById(R.id.timeShowText)
        textViewDescription = view.findViewById(R.id.descriptionShowText)

        timeSlotDetailsViewModel.advertisement.observe(this.viewLifecycleOwner) {
            textViewTitle.setText(it.title)
            textViewLocation.setText(it.location)
            textViewDuration.setText(it.duration)
            textViewDescription.setText(it.description)
            textViewDate.setText(it.calendar.fromDateToString())
            textViewTime.setText(it.calendar.fromTimeToString())
        }
//        timeSlotDetailsViewModel.title.observe(this.viewLifecycleOwner) {
//        }
//
//        timeSlotDetailsViewModel.location.observe(this.viewLifecycleOwner) {
//        }
//
//        timeSlotDetailsViewModel.duration.observe(this.viewLifecycleOwner) {
//        }
//
//        timeSlotDetailsViewModel.description.observe(this.viewLifecycleOwner) {
//        }
//
//        timeSlotDetailsViewModel.calendar.observe(this.viewLifecycleOwner) {
//
//        }
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

    private fun Calendar.fromTimeToString(): String? {
        val myFormat = if (DateFormat.is24HourFormat(activity)) "HH:mm" else "hh:mm a"

        val dateFormat = SimpleDateFormat(myFormat, Locale.US)

        return dateFormat.format(this.time)
    }

    private fun Calendar.fromDateToString(): String? {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        return dateFormat.format(this.time)
    }
}