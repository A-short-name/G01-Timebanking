package it.polito.mad.g01_timebanking.ui.profile

import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.*
import it.polito.mad.g01_timebanking.helpers.FileHelper
import it.polito.mad.g01_timebanking.ui.reviewslist.ReviewsListViewModel

class ShowProfileFragment : Fragment() {
    private val profileViewModel : ProfileViewModel by activityViewModels()
    private val reviewsListViewModel : ReviewsListViewModel by activityViewModels()

    private val auth = Firebase.auth


    companion object {
        private const val TAG = "ShowProfileActivity"
    }
    private lateinit var scrollView: ScrollView
    private lateinit var tvFullName: TextView
    private lateinit var tvNickname: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvBiography: TextView
    private lateinit var ivProfilePicture: ImageView
    private lateinit var skillGroup: ChipGroup
    private lateinit var noSkills: TextView
    private lateinit var buyerRatingBar: RatingBar
    private lateinit var sellerRatingBar: RatingBar
    private lateinit var tvCurrentBalance: TextView
    private lateinit var tvCurrentBalanceLayout: CardView
    private lateinit var reviewsButton : CardView

    private lateinit var actUserInfo : UserInfo

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
        // Fetch views
        scrollView = view.findViewById(R.id.sv)
        tvFullName = view.findViewById(R.id.fullname)
        tvNickname = view.findViewById(R.id.nickname)
        tvEmail = view.findViewById(R.id.email)
        tvLocation = view.findViewById(R.id.location)
        tvBiography = view.findViewById(R.id.biography)
        ivProfilePicture = view.findViewById(R.id.profilePicture)
        skillGroup = view.findViewById(R.id.skillgroup)
        noSkills = view.findViewById(R.id.noSkillsTextView)
        buyerRatingBar = view.findViewById(R.id.profileBuyerReviewRatingBar)
        sellerRatingBar = view.findViewById(R.id.profileSellerReviewRatingBar)
        tvCurrentBalance = view.findViewById(R.id.currentBalance)
        tvCurrentBalanceLayout = view.findViewById(R.id.currentBalanceLayout)
        reviewsButton = view.findViewById(R.id.openReviewsButton)

        tvCurrentBalanceLayout.visibility = View.VISIBLE

        profileViewModel.buyerRating.observe(this.viewLifecycleOwner) {
            buyerRatingBar.rating = it
        }

        profileViewModel.sellerRating.observe(this.viewLifecycleOwner) {
            sellerRatingBar.rating = it
        }

        profileViewModel.user.observe(this.viewLifecycleOwner) {
            actUserInfo = it
            tvFullName.text = it.fullName
            tvNickname.text = it.nickname
            tvEmail.text = it.email
            tvLocation.text = it.location
            tvBiography.text = it.biography
            tvCurrentBalance.text = it.balance.printBalance()

            skillGroup.removeAllViews()

            if(it.skills.isEmpty())
                noSkills.isVisible = true
            else
                it.skills
                    .forEach{ content ->
                        val chip = Chip(context)
                        chip.text = content[0].uppercase() + content.substring(1,content.length)
                        chip.isCheckable = false
                        chip.isClickable = true
                        chip.textAlignment = View.TEXT_ALIGNMENT_CENTER
                        chip.elevation = 5F
                        skillGroup.addView(chip)
                    }.also{ noSkills.isVisible = false }

            reviewsButton.setOnClickListener {
                reviewsListViewModel.setReviews(auth.currentUser!!.uid)
                findNavController().navigate(R.id.action_nav_show_profile_to_nav_reviews_list, bundleOf("topAppBarName" to "My Reviews") )
            }
        }

        profileViewModel.profilePicturePath.observe(this.viewLifecycleOwner) {
            if (it != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER) {
                FileHelper.readImage(it, ivProfilePicture)
            }
        }
        //the only way to set height image to 1/3 of the screen is programmatically
        //This is ue to the fact that we use a scroll view with a bio with variable length
        if(!FileHelper.isExternalStorageWritable())
            Log.e(TAG, "No external volume mounted")


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return if(item.itemId == R.id.nav_edit_profile) {
            requireView().findNavController().navigate(R.id.action_nav_show_profile_to_nav_edit_profile)
            true
        } else {
            super.onOptionsItemSelected(item)
        }

//        return NavigationUI.onNavDestinationSelected(
//            item,
//            requireView().findNavController())
//                || super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        // This updates ViewModel if the show is disappearing because the edit is being opened
        profileViewModel.setUserInfo(actUserInfo)
        super.onPause()
    }


    private fun CharSequence.printBalance() : String {
        val split = this.split(":")
        val result = 0

        if(split.size != 2)
            return "$result min"
        try {
            val hours = split[0].toInt()
            val minutes = split[1].toInt()
            if(hours == 0)
                return "$minutes m"
            return if(minutes == 0)
                "$hours h"
            else
                "$hours h $minutes m"

        } catch(e: Exception) {
            return "$result min"
        }
    }
}

