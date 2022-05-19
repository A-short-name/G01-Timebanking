package it.polito.mad.g01_timebanking.ui.timeslotlistbyskill

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotFiltersBinding
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromDateToString
import java.util.*

class TimeSlotFiltersFragment : Fragment() {
    private val timeSlotListBySkillViewModel : TimeSlotListBySkillViewModel by activityViewModels()

    private var _binding: FragmentTimeSlotFiltersBinding? = null
    private val binding get() = _binding!!

    // Variables to handle date and calculation
    private var nowTimeDate = Calendar.getInstance()
    private var actualFromTimeDate : Calendar? = null
    private var actualToTimeDate : Calendar? = null
    private var desiredTimeDate : Calendar? = null

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

        val editTextFromDate = view.findViewById<EditText>(R.id.filterByFromDateEditText)
        val editTextToDate = view.findViewById<EditText>(R.id.filterByToDateEditText)
        val editTextLocation = view.findViewById<EditText>(R.id.filterByLocationEditText)
        val applyFilterButton = view.findViewById<Button>(R.id.ApplyFilterButton)
        val cancelFilterButton = view.findViewById<Button>(R.id.CancelFilterButton)

        val seekBar = view.findViewById<SeekBar>(R.id.durationSeekBar)
        val selectedDurationText = view.findViewById<TextView>(R.id.selectedDurationText)

        timeSlotListBySkillViewModel.fromCalendarFilter.observe(this.viewLifecycleOwner) {
            editTextFromDate.setText(it?.fromDateToString())
            actualFromTimeDate = it
        }

        timeSlotListBySkillViewModel.toCalendarFilter.observe(this.viewLifecycleOwner) {
            editTextToDate.setText(it?.fromDateToString())
            actualToTimeDate = it
        }

        timeSlotListBySkillViewModel.locationFilter.observe(this.viewLifecycleOwner) {
            editTextLocation.setText(it)
        }

        timeSlotListBySkillViewModel.durationFilter.observe(this.viewLifecycleOwner) {
            if (it != "" && it != "Disabled")
                seekBar.progress = it.toInt()
        }

        val fromDate = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            nowTimeDate = Calendar.getInstance()

            desiredTimeDate = Calendar.getInstance()

            desiredTimeDate!!.set(Calendar.YEAR, year)
            desiredTimeDate!!.set(Calendar.MONTH, month)
            desiredTimeDate!!.set(Calendar.DAY_OF_MONTH, day)

            if (actualToTimeDate != null)
                if (desiredTimeDate!! > actualToTimeDate!!) {
                    desiredTimeDate = actualFromTimeDate
                    val text: CharSequence = "Origin date cannot be later than the end date"
                    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                    toast.show()
                    return@OnDateSetListener
                }
            if (desiredTimeDate!! >= nowTimeDate) {
                timeSlotListBySkillViewModel.setDateTime(true, desiredTimeDate!!)
                actualFromTimeDate = desiredTimeDate
            }
            else {
                desiredTimeDate = actualFromTimeDate
                val text: CharSequence = "Time is already passed. Choose a future one!"
                val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                toast.show()
            }
        }

        val toDate = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            nowTimeDate = Calendar.getInstance()

            desiredTimeDate = Calendar.getInstance()

            desiredTimeDate!!.set(Calendar.YEAR, year)
            desiredTimeDate!!.set(Calendar.MONTH, month)
            desiredTimeDate!!.set(Calendar.DAY_OF_MONTH, day)

            if (actualFromTimeDate != null)
                if (desiredTimeDate!! < actualFromTimeDate!!) {
                    desiredTimeDate = actualToTimeDate
                    val text: CharSequence = "End date cannot be earlier than the origin date"
                    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                    toast.show()
                    return@OnDateSetListener
                }

            if (desiredTimeDate!! >= nowTimeDate) {
                timeSlotListBySkillViewModel.setDateTime(false, desiredTimeDate!!)
                actualToTimeDate = desiredTimeDate
            }
            else {
                desiredTimeDate = actualToTimeDate
                val text: CharSequence = "Time is already passed. Choose a future one!"
                val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
                toast.show()
            }
        }



        // When the edit text is clicked, pop-up the date picker instead
        editTextFromDate.setOnClickListener {
            nowTimeDate = Calendar.getInstance()

            val toBeShownTimeDate = actualFromTimeDate ?: nowTimeDate

            val dtDialog = DatePickerDialog(
                this.requireContext(),
                fromDate, // This is the callback that will be called when date is selected
                toBeShownTimeDate.get(Calendar.YEAR), // This is the date that will be shown to user
                toBeShownTimeDate.get(Calendar.MONTH),
                toBeShownTimeDate.get(Calendar.DAY_OF_MONTH)
            )
            dtDialog.datePicker.minDate = nowTimeDate.timeInMillis
            dtDialog.show()
        }

        editTextToDate.setOnClickListener {
            nowTimeDate = Calendar.getInstance()

            val toBeShownTimeDate = actualToTimeDate ?: nowTimeDate

            val dtDialog = DatePickerDialog(
                this.requireContext(),
                toDate, // This is the callback that will be called when date is selected
                toBeShownTimeDate.get(Calendar.YEAR), // This is the date that will be shown to user
                toBeShownTimeDate.get(Calendar.MONTH),
                toBeShownTimeDate.get(Calendar.DAY_OF_MONTH)
            )
            dtDialog.datePicker.minDate = nowTimeDate.timeInMillis
            dtDialog.show()
        }

        applyFilterButton.setOnClickListener {
            timeSlotListBySkillViewModel.setDateTime(true,actualFromTimeDate)
            timeSlotListBySkillViewModel.setDateTime(false,actualToTimeDate)
            timeSlotListBySkillViewModel.setLocationFilter(editTextLocation.text.toString())
            timeSlotListBySkillViewModel.setDurationFilter(selectedDurationText.text.toString())
            timeSlotListBySkillViewModel.applyFilters()
            activity?.onBackPressed()
        }

        cancelFilterButton.setOnClickListener {
            actualFromTimeDate = null
            actualToTimeDate = null
            timeSlotListBySkillViewModel.setDateTime(true,actualFromTimeDate)
            timeSlotListBySkillViewModel.setDateTime(false,actualToTimeDate)
            timeSlotListBySkillViewModel.setLocationFilter("")
            timeSlotListBySkillViewModel.setDurationFilter("")
            timeSlotListBySkillViewModel.removeFilters()
            activity?.onBackPressed()
        }

        seekBar.setOnSeekBarChangeListener(MyOnSeekBarChangeListener(requireView()))
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class MyOnSeekBarChangeListener(val view: View) : SeekBar.OnSeekBarChangeListener {
    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
        val text = view.findViewById<TextView>(R.id.selectedDurationText)

        if(p1 != 0)
            text.text = (p1.toString())
        else
            text.text = "Disabled"
    }

    override fun onStartTrackingTouch(p0: SeekBar?) {
    }

    override fun onStopTrackingTouch(p0: SeekBar?) {
    }

}