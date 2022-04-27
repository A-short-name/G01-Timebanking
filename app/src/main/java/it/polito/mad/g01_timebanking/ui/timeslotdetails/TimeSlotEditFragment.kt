package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserKey.HASTOBEEMPTY
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotEditBinding
import it.polito.mad.g01_timebanking.ui.timeslotlist.TimeSlotListViewModel
import java.text.SimpleDateFormat
import java.util.*


class TimeSlotEditFragment : Fragment() {
    // View model variable
    private val timeSlotDetailsViewModel : TimeSlotDetailsViewModel by activityViewModels()
    private val advListViewModel : TimeSlotListViewModel by activityViewModels()

    // Variables to handle date and time calculation
    private var nowTimeDate = Calendar.getInstance()
    private var actualTimeDate = Calendar.getInstance()
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

    private var actualAdvId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSlotEditBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Get text views */
        editTextTitle = view.findViewById(R.id.titleEditText)
        editTextLocation = view.findViewById(R.id.locationEditText)
        editTextDuration = view.findViewById(R.id.durationEditText)
        editTextDate = view.findViewById(R.id.dateEditText)
        editTextTime = view.findViewById(R.id.timeEditText)
        editTextDescription = view.findViewById(R.id.descriptionEditText)

        /* Set fields */
        timeSlotDetailsViewModel.id.observe(this.viewLifecycleOwner) {
            actualAdvId = it
        }

        timeSlotDetailsViewModel.title.observe(this.viewLifecycleOwner) {
            editTextTitle.setText(it)
        }

        timeSlotDetailsViewModel.location.observe(this.viewLifecycleOwner) {
            editTextLocation.setText(it)
        }

        timeSlotDetailsViewModel.duration.observe(this.viewLifecycleOwner) {
            editTextDuration.setText(it)
        }

        timeSlotDetailsViewModel.description.observe(this.viewLifecycleOwner) {
            editTextDescription.setText(it)
        }

        timeSlotDetailsViewModel.calendar.observe(this.viewLifecycleOwner) {
            editTextDate.setText(fromDateToString(it.time))
            editTextTime.setText(fromTimeToString(it.time))
            actualTimeDate = it
        }

//        advListViewModel.advList.observe(this.viewLifecycleOwner) {
//            val gson = Gson();
//            val serializedAdvList: String = gson.toJson(it)
//            println(serializedAdvList);
//
//            val sharedPref =
//                context?.getSharedPreferences(getString(R.string.preference_file_key), AppCompatActivity.MODE_PRIVATE) ?: return@observe
//            with(sharedPref.edit()) {
//                putString(getString(R.string.adv_list), serializedAdvList)
//                apply()
//            }
//        }

        // Check if the fragment is called from the FAB (so it has to be empty)
        if (arguments?.getBoolean(HASTOBEEMPTY) == true) {
            timeSlotDetailsViewModel.setTitle("")
            timeSlotDetailsViewModel.setDuration("")
            timeSlotDetailsViewModel.setDescription("")
            timeSlotDetailsViewModel.setLocation("")
            val expTime = Calendar.getInstance()
            expTime.add(Calendar.HOUR_OF_DAY,+1)
            expTime.set(Calendar.MINUTE,0)
            timeSlotDetailsViewModel.setDateTime(expTime)
            timeSlotDetailsViewModel.setId(advListViewModel.count())
        }

        /* Code fragment to generate time and date picker  */

        // When a date is selected by the user this function is called.
        // It updates the date in the calendar object and the textInput shown to the user

        val date = OnDateSetListener { _, year, month, day ->
            nowTimeDate = Calendar.getInstance()

            desiredTimeDate.set(Calendar.YEAR,year)
            desiredTimeDate.set(Calendar.MONTH,month)
            desiredTimeDate.set(Calendar.DAY_OF_MONTH,day)

            if(desiredTimeDate > nowTimeDate)
                timeSlotDetailsViewModel.setDateTime(desiredTimeDate)
            else {
                desiredTimeDate = actualTimeDate
                val text: CharSequence = "Time is already passed. Choose a future one!"
                val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                toast.show()
            }
        }

        // When the edit text is clicked, pop-up the date picker instead
        editTextDate.setOnClickListener {
            nowTimeDate = Calendar.getInstance()

            val dtDialog = DatePickerDialog(
                this.requireContext(),
                date, // This is the callback that will be called when date is selected
                actualTimeDate.get(Calendar.YEAR), // This is the date that will be shown to user
                actualTimeDate.get(Calendar.MONTH),
                actualTimeDate.get(Calendar.DAY_OF_MONTH)
            )
            dtDialog.datePicker.minDate = nowTimeDate.timeInMillis
            dtDialog.show()
        }

        // When a time is selected by the user this function is called.
        // It updates the time in the calendar object and the textInput shown to the user
        val time = TimePickerDialog.OnTimeSetListener { _, hour, minute ->

            nowTimeDate = Calendar.getInstance()

            desiredTimeDate.set(Calendar.HOUR_OF_DAY, hour)
            desiredTimeDate.set(Calendar.MINUTE, minute)

            // If the selected date is today, make sure the time selected is not passed
            if(DateUtils.isToday(actualTimeDate.timeInMillis)) {

                // timeNow < timeDesired
                val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
                val nowTime = dateFormat.format(nowTimeDate.time)
                val desiredTime = dateFormat.format(desiredTimeDate.time)

                if(nowTime <= desiredTime) {
                    timeSlotDetailsViewModel.setDateTime(desiredTimeDate)
                } else {
                    desiredTimeDate = actualTimeDate
                    val text: CharSequence = "Time is already passed. Choose a future one!"
                    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                    toast.show()
                }

            } else {
                timeSlotDetailsViewModel.setDateTime(desiredTimeDate)
            }
        }

        // When the edit text is clicked, pop-up the date picker instead
        editTextTime.setOnClickListener {
            TimePickerDialog(
                this.requireContext(),
                time,
                actualTimeDate.get(Calendar.HOUR_OF_DAY),
                actualTimeDate.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(activity)
            ).show()
        }
        /* End of Code fragment to generate time and date picker */
    }

    override fun onDetach() {
        timeSlotDetailsViewModel.setTitle(editTextTitle.text.toString())
        timeSlotDetailsViewModel.setDuration(editTextDuration.text.toString())
        timeSlotDetailsViewModel.setDescription(editTextDescription.text.toString())
        timeSlotDetailsViewModel.setLocation(editTextLocation.text.toString())

        val a = AdvertisementDetails (
            id = actualAdvId,
            title = editTextTitle.text.toString(),
            location = editTextLocation.text.toString(),
            calendar = actualTimeDate,
            duration = editTextDuration.text.toString(),
            description = editTextDescription.text.toString()
        )
        //There is an observer that update preferences
        advListViewModel.addOrUpdateElement(a)

        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fromTimeToString(date : Date): String? {
        val myFormat = if (DateFormat.is24HourFormat(activity)) "HH:mm" else "hh:mm a"

        val dateFormat = SimpleDateFormat(myFormat, Locale.US)

        return dateFormat.format(date)
    }

    private fun fromDateToString(date: Date): String? {
        val myFormat = "dd/MM/yyyy"
        val dateFormat = SimpleDateFormat(myFormat, Locale.US)
        return dateFormat.format(date)
    }
}