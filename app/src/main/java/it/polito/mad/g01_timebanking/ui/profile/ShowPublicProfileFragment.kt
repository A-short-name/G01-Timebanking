package it.polito.mad.g01_timebanking.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.UserInfo
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.helpers.FileHelper
import it.polito.mad.g01_timebanking.ui.reviewslist.ReviewsListViewModel

class ShowPublicProfileFragment : Fragment() {
    private val profileViewModel : ProfileViewModel by activityViewModels()

    private val reviewsListViewModel : ReviewsListViewModel by activityViewModels()

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
    private lateinit var rbRatingBuyer: RatingBar
    private lateinit var rbRatingSeller: RatingBar
    private lateinit var skillGroup: ChipGroup
    private lateinit var noSkills: TextView
    private lateinit var reviewsButton : CardView
    private lateinit var tvCurrentBalanceLayout: CardView

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
        rbRatingSeller = view.findViewById(R.id.profileSellerReviewRatingBar)
        rbRatingBuyer = view.findViewById(R.id.profileBuyerReviewRatingBar)
        ivProfilePicture = view.findViewById(R.id.profilePicture)
        skillGroup = view.findViewById(R.id.skillgroup)
        noSkills = view.findViewById(R.id.noSkillsTextView)
        reviewsButton = view.findViewById(R.id.openReviewsButton)
        tvCurrentBalanceLayout = view.findViewById(R.id.currentBalanceLayout)

        tvCurrentBalanceLayout.visibility = View.GONE

        profileViewModel.advOwnerBuyerRating.observe(this.viewLifecycleOwner) {
            rbRatingBuyer.rating = it
        }

        profileViewModel.advOwnerSellerRating.observe(this.viewLifecycleOwner) {
            rbRatingSeller.rating = it
        }

        profileViewModel.pubUserTmpPath.observe(this.viewLifecycleOwner){
            if (it != UserKey.PROFILE_PICTURE_PATH_PLACEHOLDER)
                FileHelper.readImage(it, ivProfilePicture)
            else {
                ivProfilePicture.setImageResource(R.drawable.avatar)
            }
        }

        profileViewModel.pubUser.observe(this.viewLifecycleOwner) {
            actUserInfo = it
            tvFullName.text = it.fullName
            tvNickname.text = it.nickname
            tvEmail.text = it.email
            tvLocation.text = it.location
            tvBiography.text = it.biography

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
                        chip.elevation = 5F
                        skillGroup.addView(chip)
                    }.also{ noSkills.isVisible = false }
            val fullName = it.fullName
            reviewsButton.setOnClickListener {
                reviewsListViewModel.setReviews(profileViewModel.pubUserId.value!!)
                findNavController().navigate(R.id.action_showPublicProfileFragment_to_nav_reviews_list, bundleOf("topAppBarName" to "$fullName review's") )
            }
        }



        //the only way to set height image to 1/3 of the screen is programmatically
        //This is ue to the fact that we use a scroll view with a bio with variable length

        if(!FileHelper.isExternalStorageWritable())
            Log.e(TAG, "No external volume mounted")
    }

}