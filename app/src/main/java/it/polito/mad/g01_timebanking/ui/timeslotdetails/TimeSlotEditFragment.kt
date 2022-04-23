package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.navigation.fragment.findNavController
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotEditBinding
import it.polito.mad.g01_timebanking.model.TimeSlotDetails
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotEditFragment : Fragment() {
    // View model variable
    private val timeSlotDetailsViewModel : TimeSlotDetailsViewModel by activityViewModels()
    // This var will contain user information
    private lateinit var timeSlotDetailsLD : LiveData<TimeSlotDetails>

    // Views to be handled
    private lateinit var editTextTitle: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var editTextDuration: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var editTextDescription: EditText

    private var _binding: FragmentTimeSlotEditBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSlotEditBinding.inflate(inflater, container, false)

        timeSlotDetailsLD = timeSlotDetailsViewModel.timeSlotDetails

        val root: View = binding.root

        editTextTitle = root.findViewById(R.id.editTitle)
        editTextLocation = root.findViewById(R.id.editTextLocation)
        editTextDuration = root.findViewById(R.id.editTextDuration)
        editTextDate = root.findViewById(R.id.editTextDate)
        editTextTime = root.findViewById(R.id.editTextTime)
        editTextDescription = root.findViewById(R.id.editTextTextDescription)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Set fields */
        editTextTitle.setText(timeSlotDetailsLD.value!!.title)
        editTextLocation.setText(timeSlotDetailsLD.value!!.location)
        editTextDuration.setText(timeSlotDetailsLD.value!!.duration)
        editTextDate.setText(fromDateToString(timeSlotDetailsLD.value!!.calendar.time))
        editTextTime.setText(fromTimeToString(timeSlotDetailsLD.value!!.calendar.time))
        editTextDescription.setText(timeSlotDetailsLD.value!!.description)

        /* Code fragment to generate time and date picker  */

        // When a date is selected by the user this function is called.
        // It updates the date in the calendar object and the editText shown to the user
        val date = OnDateSetListener { _, year, month, day ->
            timeSlotDetailsViewModel.setDate(year, month, day)

            val dateString = fromDateToString(timeSlotDetailsLD.value?.calendar?.time!!)
            editTextDate.setText(dateString)
        }

        // When the edit text is clicked, pop-up the date picker instead
        editTextDate.setOnClickListener {
            DatePickerDialog(
                this.requireContext(),
                date, // This is the callback that will be called when date is selected
                timeSlotDetailsLD.value!!.calendar.get(Calendar.YEAR), // This is the date that will be shown to user
                timeSlotDetailsLD.value!!.calendar.get(Calendar.MONTH),
                timeSlotDetailsLD.value!!.calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // When a time is selected by the user this function is called.
        // It updates the time in the calendar object and the editText shown to the user
        val time = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            timeSlotDetailsViewModel.setTime(hour,minute)

            // Format time as e.g. 13:30 (if 24 hours) or 01:30 PM (if 12 hours)
            val timeString = fromTimeToString(timeSlotDetailsLD.value?.calendar?.time!!)
            editTextTime.setText(timeString)
        }

        // When the edit text is clicked, pop-up the date picker instead
        editTextTime.setOnClickListener {
            TimePickerDialog(
                this.requireContext(),
                time,
                timeSlotDetailsLD.value!!.calendar.get(Calendar.HOUR),
                timeSlotDetailsLD.value!!.calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(activity)
            ).show()
        }
        /* End of Code fragment to generate time and date picker */
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fromTimeToString(date : Date): String? {
        val myFormat = if (DateFormat.is24HourFormat(activity)) "hh:mm" else "hh:mm a"

        val dateFormat = SimpleDateFormat(myFormat, Locale.US)

        return dateFormat.format(date)
    }

    private fun fromDateToString(date: Date): String? {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        return dateFormat.format(date)
    }
}