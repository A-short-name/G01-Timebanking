package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotDetailsBinding
import it.polito.mad.g01_timebanking.model.TimeSlotDetails
import java.text.SimpleDateFormat
import java.util.*


class TimeSlotDetailsFragment : Fragment() {
    private val timeSlotDetailsViewModel : TimeSlotDetailsViewModel by activityViewModels()

    private lateinit var timeSlotDetailsLD : LiveData<TimeSlotDetails>

    // Views to be handled
    private lateinit var textViewTitle: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewDuration: TextView
    private lateinit var textViewDate: TextView
    private lateinit var textViewDescription: TextView

    private var _binding: FragmentTimeSlotDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        timeSlotDetailsLD = timeSlotDetailsViewModel.timeSlotDetails

        _binding = FragmentTimeSlotDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        textViewTitle = root.findViewById(R.id.title_time_slot_details)
        textViewLocation = root.findViewById(R.id.location_body_time_slot_details)
        textViewDuration = root.findViewById(R.id.duration_body_time_slot_details)
        textViewDate = root.findViewById(R.id.date_body_time_slot_details)
        textViewDescription = root.findViewById(R.id.description_body_time_slot_details)

        // Enable menu options
        setHasOptionsMenu(true)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        populateFields()
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

    private fun populateFields() {
        textViewTitle.text = timeSlotDetailsLD.value!!.title
        textViewLocation.text = timeSlotDetailsLD.value!!.location
        textViewDuration.text = timeSlotDetailsLD.value!!.duration
        textViewDescription.text = timeSlotDetailsLD.value!!.description

        val myFormat =
            if (DateFormat.is24HourFormat(activity)) "dd/MM/yyyy hh:mm" else "dd/MM/yyyy hh:mm aa"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        textViewDate.text = dateFormat.format(timeSlotDetailsLD.value!!.calendar.time)
    }
}