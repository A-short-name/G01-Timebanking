package it.polito.mad.g01_timebanking.ui.review

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.databinding.FragmentWriteReviewBinding

class ReviewFragment : Fragment() {

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

        sendReviewButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}