package it.polito.mad.g01_timebanking.ui.timeslotedit

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotEditBinding
import java.text.SimpleDateFormat
import java.util.*


class TimeSlotEditFragment : Fragment() {
    /** Vars for Date Picker **/
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText

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
            ViewModelProvider(this)[TimeSlotEditViewModel::class.java]

        _binding = FragmentTimeSlotEditBinding.inflate(inflater, container, false)

        val root: View = binding.root

        editTextDate = root.findViewById(R.id.editTextDate)
        editTextTime = root.findViewById(R.id.editTextTime)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Code fragment to generate time and date picker  */

        // When a date is selected by the user this function is called.
        // It updates the date in the calendar object and the editText shown to the user
        val date = OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                val myFormat = "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat(myFormat, Locale.US)
                editTextDate.setText(dateFormat.format(calendar.time))
        }

        // When the edit text is clicked, pop-up the date picker instead
        editTextDate.setOnClickListener {
            DatePickerDialog(
                this.requireContext(),
                date, // This is the callback that will be called when date is selected
                calendar.get(Calendar.YEAR), // This is the date that will be shown to user
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // When a time is selected by the user this function is called.
        // It updates the time in the calendar object and the editText shown to the user
        val time = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR, hour)
            calendar.set(Calendar.MINUTE, minute)

            // Format time as e.g. 13:30 (if 24 hours) or 01:30 PM (if 12 hours)
            val myFormat = if(DateFormat.is24HourFormat(activity)) "hh:mm" else "hh:mm a"

            val dateFormat = SimpleDateFormat(myFormat, Locale.US)
            editTextTime.setText(dateFormat.format(calendar.time))
        }

        // When the edit text is clicked, pop-up the date picker instead
        editTextTime.setOnClickListener {
            TimePickerDialog(
                this.requireContext(),
                time,
                calendar.get(Calendar.HOUR),
                calendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(activity)
            ).show()
        }
        /* End of Code fragment to generate time and date picker */
    }
}