package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotDetailsBinding
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromDateToString
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromTimeToString
import it.polito.mad.g01_timebanking.helpers.FileHelper
import it.polito.mad.g01_timebanking.ui.chat.ChatViewModel
import it.polito.mad.g01_timebanking.ui.profile.ProfileViewModel
import java.util.*


class TimeSlotDetailsFragment : Fragment() {
    private val timeSlotDetailsViewModel : TimeSlotDetailsViewModel by activityViewModels()

    private val profileViewModel : ProfileViewModel by activityViewModels()

    private val chatViewModel : ChatViewModel by activityViewModels()
    // Views to be handled
    private lateinit var textViewTitle: EditText
    private lateinit var textViewLocation: EditText
    private lateinit var textViewDuration: EditText
    private lateinit var textViewDate: EditText
    private lateinit var textViewTime: EditText
    private lateinit var textViewDescription: EditText
    private lateinit var imageViewAdvProfilePicture: ImageView
    private lateinit var textViewAdvUserName : EditText
    private lateinit var openChatButton : Button
    private lateinit var skillGroup: ChipGroup
    private lateinit var noSkills: TextView
    private lateinit var profilePictureButton : ImageButton
    private lateinit var actualAdvertisement : AdvertisementDetails

    var favorite = true

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
        textViewAdvUserName = view.findViewById(R.id.advUserFullNameShowText)
        imageViewAdvProfilePicture = view.findViewById(R.id.advProfilePicture)
        profilePictureButton = view.findViewById(R.id.advProfilePictureTransparentButton)
        openChatButton = view.findViewById(R.id.openChatButton)


        profileViewModel.pubUserTmpPath.observe(this.viewLifecycleOwner){
            if (it != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER)
                FileHelper.readImage(it, imageViewAdvProfilePicture)
            else
                imageViewAdvProfilePicture.setImageResource(R.drawable.avatar)

        }

        timeSlotDetailsViewModel.advertisement.observe(this.viewLifecycleOwner) { advDet ->
            textViewTitle.setText(advDet.title)
            textViewLocation.setText(advDet.location)
            textViewDuration.setText(advDet.duration)
            textViewDescription.setText(advDet.description)
            var favorite : Boolean = true
            if(advDet.uid == Firebase.auth.currentUser!!.uid) {
                openChatButton.visibility = View.GONE
            } else {
                openChatButton.visibility = View.VISIBLE
                openChatButton.setOnClickListener {
                    chatViewModel.setChat(advDet)
                    findNavController().navigate(R.id.action_nav_show_time_slot_to_nav_chat_list)
                }
            }

            val calendar = Calendar.getInstance()
            calendar.time = advDet.calendar
            textViewDate.setText(calendar.fromDateToString())
            textViewTime.setText(calendar.fromTimeToString(DateFormat.is24HourFormat(activity)))
            actualAdvertisement = advDet

            skillGroup.removeAllViews()

            if(advDet.skills.isEmpty())
                noSkills.isVisible = true
            else
                advDet.skills
                    .forEach{ content ->
                        val chip = Chip(context)
                        chip.text = content
                        chip.isCheckable = false
                        chip.isClickable = true
                        skillGroup.addView(chip)
                    }.also{ noSkills.isVisible = false }

            profilePictureButton.setOnClickListener { it2 ->
                Log.d("TimeSlotDetail","going into profile of ${actualAdvertisement.uid}")

                Navigation.findNavController(it2).navigate(R.id.action_nav_show_time_slot_to_showPublicProfileFragment)


            }
            //search the user from db
            timeSlotDetailsViewModel.getUserInfoFromDb(actualAdvertisement.uid).get()
                .addOnSuccessListener { userDocFromDb ->
                    val userFromDb = userDocFromDb.toUserInfo()
                    textViewAdvUserName.setText(userFromDb.fullName)

                    profileViewModel.setPublicUserInfo(userFromDb, userDocFromDb.id)
                    //if(userFromDb.profilePicturePath != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER)
                        profileViewModel.downloadPublicPhoto(actualAdvertisement.uid)


                }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if(arguments?.getBoolean("HideOptionMenu") == true) {
            inflater.inflate(R.menu.favourite_menu, menu)
            if(favorite)
                menu.findItem(R.id.app_bar_switch).setIcon(R.drawable.ic_favorite)
        }
        else
            inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /**
         * The following code displays the fragment that
         * has the same ID as the menu item. (This code only works if the menu item
         * and the fragment have identical ID values.)
         */
        timeSlotDetailsViewModel.clearTmpSkills()
        when(item.itemId){
            R.id.app_bar_switch -> {
                favorite = !favorite
                if(favorite)
                    item.setIcon(R.drawable.ic_favorite)
                else
                    item.setIcon(R.drawable.ic_non_favorite)
                Log.d("FAVORITE", "SWITCH CLICKED: $favorite")
            }
            R.id.nav_edit_time_slot -> NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        // This updates ViewModel if the show is disappearing because the edit is being opened
        timeSlotDetailsViewModel.setAdvertisement(actualAdvertisement)
        super.onPause()
    }




    private fun DocumentSnapshot.toUserInfo(): UserInfo {
        return this.toObject(UserInfo::class.java) ?: UserInfo()
    }

}