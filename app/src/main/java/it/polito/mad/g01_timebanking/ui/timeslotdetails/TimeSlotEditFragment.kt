package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
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

    // Variables to handle date and time calculation
    private var actTimeDate = Calendar.getInstance()
    private var desiredTimeDate = Calendar.getInstance()

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

        desiredTimeDate = timeSlotDetailsLD.value!!.calendar

        // This function calls setTitle each time there is a change on text.
        // A possible workaround could be "setOnFocusChangeListener"
        // editTextTitle.setOnFocusChangeListener(){ _, _ -> //view, hasFocus parameters
        // }
        // The problem with the above function is that it is not called if the user is editing
        // and then presses back (without losing focus on the editText)
        editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                timeSlotDetailsViewModel.setTitle(editTextTitle.text.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {

            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        // Same for all other editTexts
        editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                timeSlotDetailsViewModel.setDescription(editTextDescription.text.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {

            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        editTextLocation.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                timeSlotDetailsViewModel.setLocation(editTextLocation.text.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {

            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        editTextDuration.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                timeSlotDetailsViewModel.setLocation(editTextDuration.text.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {

            }

            override fun afterTextChanged(s: Editable) {
            }
        })

        /* Code fragment to generate time and date picker  */

        // When a date is selected by the user this function is called.
        // It updates the date in the calendar object and the editText shown to the user
        val date = OnDateSetListener { _, year, month, day ->
            actTimeDate = Calendar.getInstance()

            desiredTimeDate.set(Calendar.YEAR,year)
            desiredTimeDate.set(Calendar.MONTH,month)
            desiredTimeDate.set(Calendar.DAY_OF_MONTH,day)

            if(actTimeDate < desiredTimeDate) {
                timeSlotDetailsViewModel.setDateTime(desiredTimeDate)

                val dateString = fromDateToString(timeSlotDetailsLD.value!!.calendar.time)
                editTextDate.setText(dateString)
            } else {
                val text: CharSequence = "Date is already passed. Choose a future one!"
                val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                toast.show()
            }
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
            println("Selected hour: $hour")
            actTimeDate = Calendar.getInstance()

            desiredTimeDate.set(Calendar.HOUR, hour)
            desiredTimeDate.set(Calendar.MINUTE, minute)

            // If the selected date is today, make sure the time selected is not passed
            if (actTimeDate < desiredTimeDate) {
                timeSlotDetailsViewModel.setDateTime(desiredTimeDate)

                // Format time as e.g. 13:30 (if 24 hours) or 01:30 PM (if 12 hours)
                val timeString = fromTimeToString(timeSlotDetailsLD.value!!.calendar.time)
                editTextTime.setText(timeString)
            } else {
                val text: CharSequence = "Time is already passed. Choose a future one!"
                val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                toast.show()
            }

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
        val myFormat = if (DateFormat.is24HourFormat(activity)) "HH:mm" else "hh:mm aa"

        val dateFormat = SimpleDateFormat(myFormat, Locale.US)

        return dateFormat.format(date)
    }

    private fun fromDateToString(date: Date): String? {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        return dateFormat.format(date)
    }
}