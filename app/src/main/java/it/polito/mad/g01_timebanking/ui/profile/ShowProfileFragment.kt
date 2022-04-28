package it.polito.mad.g01_timebanking.ui.profile

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import it.polito.mad.g01_timebanking.*
import it.polito.mad.g01_timebanking.ui.timeslotdetails.TimeSlotDetailsViewModel

class ShowProfileFragment : Fragment() {
    private val profileViewModel : ProfileViewModel by activityViewModels()

    companion object {
        private const val TAG = "ShowProfileActivity"
    }
    private lateinit var scrollView: ScrollView
    private lateinit var frameView: FrameLayout
    private lateinit var tvFullName: TextView
    private lateinit var tvNickname: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvBiography: TextView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var skillGroup: ChipGroup
    private lateinit var noSkills: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_show_profile, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView(view)
        //the only way to set height image to 1/3 of the screen is programmatically
        //This is ue to the fact that we use a scroll view with a bio with variable length
        arrangeViewByRatio(view)
        initializeData()

        if(!FileHelper.isExternalStorageWritable())
            Log.e(TAG, "No external volume mounted")
    }

    private fun initializeView(view: View) {
        Toast.makeText(context,"Caricando informazioni da file", Toast.LENGTH_SHORT).show()
        // Fetch views
        scrollView = view.findViewById(R.id.sv)
        frameView = view.findViewById(R.id.frameView1)
        tvFullName = view.findViewById(R.id.fullname)
        tvNickname = view.findViewById(R.id.nickname)
        tvEmail = view.findViewById(R.id.email)
        tvLocation = view.findViewById(R.id.location)
        tvBiography = view.findViewById(R.id.biography)
        ivProfilePicture = view.findViewById(R.id.profilePicture)
        skillGroup = view.findViewById(R.id.skillgroup)
        noSkills = view.findViewById(R.id.noSkillsTextView)

        profileViewModel.fullName.observe(this.viewLifecycleOwner) {
            tvFullName.text = it
        }
        profileViewModel.nickname.observe(this.viewLifecycleOwner) {
            tvNickname.text = it
        }
        profileViewModel.email.observe(this.viewLifecycleOwner) {
            tvEmail.text = it
        }
        profileViewModel.location.observe(this.viewLifecycleOwner) {
            tvLocation.text = it
        }
        profileViewModel.biography.observe(this.viewLifecycleOwner) {
            tvBiography.text = it
        }
        profileViewModel.profilePicturePath.observe(this.viewLifecycleOwner) {
            if (it != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
                FileHelper.readImage(it, ivProfilePicture)
            }
        }
        profileViewModel.skills.observe(this.viewLifecycleOwner) {
            skillGroup.removeAllViews()

            if(it.isEmpty())
                noSkills.isVisible = true
            else
                it.forEach{ content ->
                    val chip = Chip(context)
                    chip.text = content
                    chip.isCheckable = false
                    chip.isClickable = true
                    skillGroup.addView(chip)
                }.also{noSkills.isVisible = false}
        }
    }

    private fun arrangeViewByRatio(view: View) {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            scrollView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    frameView.post {
                        frameView.layoutParams =
                            LinearLayout.LayoutParams(scrollView.width, scrollView.height / 3)
                    }
                    scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    //Resize consequently cardView with image
                    val cardView = view.findViewById<CardView>(R.id.imageCard)
                    val relativeDimension = scrollView.height / 3 - 32
                    //I want a square box for the image that doesn't fit all the space in the parent frameView
                    cardView.layoutParams.width = relativeDimension
                    cardView.layoutParams.height = relativeDimension
                    //different from before because cardView doesn't work with LinearLayout.LayoutP....
                }
            })
        }
    }

    private fun initializeData() {
        //initialize the variables reading from file
        val gson = Gson()
        val sharedPref = context?.getSharedPreferences(
            getString(R.string.preference_file_key), AppCompatActivity.MODE_PRIVATE
        )
        val s: String = sharedPref?.getString(getString(R.string.user_info), "" ) ?: ""

        val u =  if(s!="") gson.fromJson(s, UserInfo::class.java) else UserInfo()

        profileViewModel.setFullname(u.fullName)
        profileViewModel.setNickname(u.nickname)
        profileViewModel.setEmail(u.email)
        profileViewModel.setLocation(u.location)
        profileViewModel.setBiography(u.biography)
        profileViewModel.setProfilePicturePath(u.profilePicturePath)
        profileViewModel.setSkills(u.skills)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.user_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

/*    private fun editProfile() {
        val gson = Gson()
        val serializedSkills: String = gson.toJson(skills)

        val i = Intent(context, EditProfileActivity::class.java)
        val b : Bundle = bundleOf(
            UserKey.FULL_NAME_EXTRA_ID to tvFullName.text,
            UserKey.NICKNAME_EXTRA_ID to tvNickname.text,
            UserKey.EMAIL_EXTRA_ID to tvEmail.text,
            UserKey.LOCATION_EXTRA_ID to tvLocation.text,
            UserKey.BIOGRAPHY_EXTRA_ID to tvBiography.text,
            UserKey.PROFILE_PICTURE_PATH_EXTRA_ID to profilePicturePath,
            UserKey.SKILLS_EXTRA_ID to serializedSkills
        )
        i.putExtras(b)

        startActivityForResult(i, UserKey.EDIT_ACTIVITY_REQUEST)

    }*/
}

