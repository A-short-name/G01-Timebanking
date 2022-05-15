package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotDetailsBinding
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromDateToString
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromTimeToString
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

    private lateinit var skillGroup: ChipGroup
    private lateinit var noSkills: TextView

    private lateinit var actualAdvertisement : AdvertisementDetails

    private var _binding: FragmentTimeSlotDetailsBinding? = null

    // This property is only valid between onCreateView and onDestroyView
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
        skillGroup = view.findViewById(R.id.skillgroup)
        noSkills = view.findViewById(R.id.noSkillsTextView)

        timeSlotDetailsViewModel.advertisement.observe(this.viewLifecycleOwner) {
            textViewTitle.setText(it.title)
            textViewLocation.setText(it.location)
            textViewDuration.setText(it.duration)
            textViewDescription.setText(it.description)

            val calendar = Calendar.getInstance()
            calendar.time = it.calendar
            textViewDate.setText(calendar.fromDateToString())
            textViewTime.setText(calendar.fromTimeToString(DateFormat.is24HourFormat(activity)))
            actualAdvertisement = it

            skillGroup.removeAllViews()

            if(it.skills.isEmpty())
                noSkills.isVisible = true
            else
                it.skills
                    .forEach{ content ->
                        val chip = Chip(context)
                        chip.text = content
                        chip.isCheckable = false
                        chip.isClickable = true
                        skillGroup.addView(chip)
                    }.also{ noSkills.isVisible = false }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if(arguments?.getBoolean("HideOptionMenu") == true)
            return
        else
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

    override fun onPause() {
        // This updates ViewModel if the show is disappearing because the edit is being opened
        timeSlotDetailsViewModel.setAdvertisement(actualAdvertisement)
        super.onPause()
    }

}