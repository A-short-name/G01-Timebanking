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
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.UserKey.HASTOBEEMPTY
import it.polito.mad.g01_timebanking.UserKey.REQUIRED
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotEditBinding
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromDateToString
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromTimeToString
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

    /* Views to be handled */

    // Variables for validation
    private lateinit var textInputTitle: TextInputLayout
    private lateinit var textInputLocation: TextInputLayout
    private lateinit var textInputDuration: TextInputLayout
    private lateinit var textInputDescription: TextInputLayout

    // Edit text of the fields of the advertisement
    private lateinit var editTextTitle: EditText
    private lateinit var editTextLocation: EditText
    private lateinit var ivSkills: AutoCompleteTextView
    private lateinit var skillGroup: ChipGroup
    private lateinit var noSkills: TextView
    private lateinit var editTextDuration: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var confirmAdvButton: Button
    private lateinit var cancelAdvButton: Button

    private lateinit var currentProfilePicturePath: String
    private lateinit var currentSkills: MutableSet<String>
    private  var suggestedSkills: MutableList<String> = mutableListOf()

    // Variable to handle button state
    private var clickedButton = ""
    private lateinit var actAdv : AdvertisementDetails

    private var _binding: FragmentTimeSlotEditBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private var actualAdvId = UserKey.ID_PLACEHOLDER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSlotEditBinding.inflate(inflater, container, false)

        activity?.onBackPressedDispatcher?.addCallback(this.viewLifecycleOwner) {
            // If not rotating
            // timeSlotDetailsViewModel.id.removeObservers(viewLifeCycleOwner)
            myOnBackPressedCallback()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* Get text input layout (material design wrapper) */
        textInputTitle = view.findViewById(R.id.titleTextInputLayout)
        textInputLocation = view.findViewById(R.id.locationTextInputLayout)
        textInputDuration = view.findViewById(R.id.durationTextInputLayout)
        textInputDescription = view.findViewById(R.id.descriptionTextInputLayout)

        /* Get text views */
        editTextTitle = view.findViewById(R.id.titleEditText)
        editTextLocation = view.findViewById(R.id.locationEditText)
        editTextDuration = view.findViewById(R.id.durationEditText)
        editTextDate = view.findViewById(R.id.dateEditText)
        editTextTime = view.findViewById(R.id.timeEditText)
        editTextDescription = view.findViewById(R.id.descriptionEditText)
        confirmAdvButton = view.findViewById(R.id.confirmAdvButton)
        cancelAdvButton = view.findViewById(R.id.cancelAdvButton)

        /* skills chip views */
        skillGroup = view.findViewById(R.id.skillgroup)
        noSkills = view.findViewById(R.id.noSkillsTextView)
        ivSkills = view.findViewById(R.id.editTextAddSkills)

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

        timeSlotDetailsViewModel.skills.observe(this.viewLifecycleOwner){
            skillGroup.removeAllViews()

            if(it.isEmpty())
                noSkills.isVisible = true
            it.forEach { content ->
                val chip = Chip(context)
                chip.isCloseIconVisible = true
                chip.text = content
                chip.isCheckable = false
                chip.isClickable = false
                chip.setOnCloseIconClickListener {
                    timeSlotDetailsViewModel.removeSkill(chip.text.toString())
                }
                skillGroup.addView(chip)
                noSkills.isVisible = false
            }
            currentSkills = it
        }
        /* Dynamic Suggested Skills list  */
        timeSlotDetailsViewModel.suggestedSkills.observe(this.viewLifecycleOwner){ it1 ->
            suggestedSkills = it1.map { it.name }.toMutableList()
            initializeSkillSuggestion(view)
        }
        // Set listener on "add skills" field
        ivSkills.setOnEditorActionListener { v, actionId, _ ->
            // If user presses enter
            if(actionId == EditorInfo.IME_ACTION_DONE){

                if(v.text.toString().length>UserKey.MINIMUM_SKILLS_LENGTH) {
                    // Add skillText on set
                    if(timeSlotDetailsViewModel.tryToAddSkill(v.text.toString().lowercase())){
                        // PillView is added to the PillGroup thanks to the observer
                        // Reset editText field for new skills
                        v.text = ""
                    } else {
                        Toast.makeText(context,"Skill already present", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context,"Skill description is too short.\nUse at least ${UserKey.MINIMUM_SKILLS_LENGTH+1} characters", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }

        initializeSkillSuggestion(view)

        timeSlotDetailsViewModel.duration.observe(this.viewLifecycleOwner) {
            editTextDuration.setText(it)
        }

        timeSlotDetailsViewModel.description.observe(this.viewLifecycleOwner) {
            editTextDescription.setText(it)
        }

        timeSlotDetailsViewModel.calendar.observe(this.viewLifecycleOwner) {
            editTextDate.setText(it.fromDateToString())
            editTextTime.setText(it.fromTimeToString(DateFormat.is24HourFormat(activity)))
            actualTimeDate = it
        }

        timeSlotDetailsViewModel.advertisement.observe(this.viewLifecycleOwner) {
            actAdv = it
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

        /* Code fragment to generate time picker for duration */

        val duration = TimePickerDialog.OnTimeSetListener { _, hour, minute ->

            val tempCal = Calendar.getInstance()

            tempCal.set(Calendar.HOUR_OF_DAY, hour)
            tempCal.set(Calendar.MINUTE, minute)

            // If the selected date is today, make sure the time selected is not passed
            timeSlotDetailsViewModel.setDuration(tempCal.fromTimeToString(true))
        }

        // When the edit text is clicked, pop-up the date picker instead
        editTextDuration.setOnClickListener {
            val tpd = TimePickerDialog(
                this.requireContext(),
                duration,
                0,
                0,
                true
            )
            tpd.show()
        }

        /* End of code fragment to generate time picker for duration */


        // Cancel and confirm button listeners
        confirmAdvButton.setOnClickListener {
            if(validateFields(true)) {
                clickedButton = "confirm"
                activity?.onBackPressed()
            }
        }
        cancelAdvButton.setOnClickListener {
            clickedButton = "cancel"
            activity?.onBackPressed()
        }
    }


    private fun initializeSkillSuggestion(view: View) {
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line, suggestedSkills.toList()
        )
        val actv = view.findViewById<AutoCompleteTextView>(R.id.editTextAddSkills)
        actv.setAdapter(adapter)
        actv.setOnItemClickListener { adapterView, _, i, _ ->
            val selected: String = adapterView.getItemAtPosition(i) as String
            if (timeSlotDetailsViewModel.tryToAddSkill(selected.lowercase())) {
                // PillView is added to the PillGroup thanks to the observer
                // Reset editText field for new skills
                ivSkills.setText("")
            } else {
                Toast.makeText(context, "Skill already present", Toast.LENGTH_SHORT).show()
                ivSkills.setText("")
            }
        }
    }

    override fun onDetach() {
        // If fragment is being detached because it is rotating, then we need to save temp data
        // inside the viewmodel.
        // Preferred way instead of register onChangeListener
        timeSlotDetailsViewModel.setTitle(editTextTitle.text.toString())
        timeSlotDetailsViewModel.setDuration(editTextDuration.text.toString())
        timeSlotDetailsViewModel.setDescription(editTextDescription.text.toString())
        timeSlotDetailsViewModel.setLocation(editTextLocation.text.toString())
        //timeSlotDetailsViewModel.setSkills() is changed everytime
        timeSlotDetailsViewModel.setDateTime(actualTimeDate)

        super.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateFields(isConfirmedPressed: Boolean) : Boolean {
        var valid = true

        val fieldsToValidate = listOf(textInputTitle,
            textInputDuration,
            textInputLocation,
            textInputDescription)

        fieldsToValidate.forEach{
            if (it.editText!!.text.isBlank()) {
                if(isConfirmedPressed) it.error = REQUIRED
                valid = false
            } else {
                if(isConfirmedPressed) it.error = null
            }
        }

        return valid
    }

    private fun confirm() {
        val a = AdvertisementDetails(
            id = actualAdvId,
            title = editTextTitle.text.toString(),
            location = editTextLocation.text.toString(),
            calendar = actualTimeDate.time,
            duration = editTextDuration.text.toString(),
            description = editTextDescription.text.toString(),
            uid = Firebase.auth.currentUser!!.uid,
            skills = currentSkills.toMutableList()
        )
        advListViewModel.addOrUpdateElement(a)
        timeSlotDetailsViewModel.addOrUpdateSkills(a.skills)
        timeSlotDetailsViewModel.setAdvertisement(a)
    }

    private fun myOnBackPressedCallback() {
        var text: CharSequence
        when (clickedButton) {
            "confirm" -> {
                //If i'm here data is already validated, since onBackPressed is called only
                // if validateFields() == true (see confirm onClickListener callback)
                text = "Successfully saved changes"
                view?.let { Snackbar.make(it, text, Snackbar.LENGTH_LONG).show() }
                confirm()
                clickedButton = ""
            }
            "cancel" -> {
                text = "Changes discarded"
                view?.let { Snackbar.make(it, text, Snackbar.LENGTH_LONG).show() }
                clickedButton = ""
            }
            else -> {
                if (validateFields(false)) {
                    text = "Successfully saved changes"
                    view?.let { Snackbar.make(it, text, Snackbar.LENGTH_LONG).show() }
                    confirm()
                } else {
                    text = "Fields not valid. Changes not saved"
                    if (arguments?.getBoolean(HASTOBEEMPTY) == true)
                        text = "Fields not valid. Advertisement not added"

                    view?.let { Snackbar.make(it, text, Snackbar.LENGTH_LONG).show() }
//                    val toast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
//                    toast.show()
                }
            }
        }
        requireView().findNavController().popBackStack()
    }

}