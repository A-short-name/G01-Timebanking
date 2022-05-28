package it.polito.mad.g01_timebanking.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.databinding.FragmentWriteReviewBinding

class ReviewFragment : Fragment() {
    private val reviewViewModel : ReviewViewModel by activityViewModels()
    private var _binding: FragmentWriteReviewBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWriteReviewBinding.inflate(inflater, container, false)

        setHasOptionsMenu(false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val ratingBar = view.findViewById<RatingBar>(R.id.writeRatingBar)
        val reviewText = view.findViewById<EditText>(R.id.writtenReviewEditText)
        val sendReviewButton = view.findViewById<Button>(R.id.sendReviewButton)

        reviewViewModel.review.observe(this.viewLifecycleOwner) { review ->
            val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
            toolbar?.title = "Review ${review.reviewerToName}"

            val advTitle = view.findViewById<TextView>(R.id.advTitleReview)
            advTitle.text = review.advTitle

            sendReviewButton.setOnClickListener {
                reviewViewModel.sendReview(review, reviewText.text.toString(), ratingBar.rating.toInt() )
                activity?.onBackPressed()
            }
        }
    }
}