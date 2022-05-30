package it.polito.mad.g01_timebanking.ui.reviewslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.databinding.FragmentTimeSlotListBinding
import it.polito.mad.g01_timebanking.adapters.AdvertisementAdapter
import it.polito.mad.g01_timebanking.adapters.ReviewAdapter
import it.polito.mad.g01_timebanking.databinding.FragmentReviewsListBinding
import it.polito.mad.g01_timebanking.ui.timeslotdetails.TimeSlotDetailsViewModel


class ReviewsListFragment : Fragment() {
    private val reviewsListViewModel : ReviewsListViewModel by activityViewModels()

    private var _binding: FragmentReviewsListBinding? = null

    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewsListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerViewRev = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerViewRev.layoutManager = LinearLayoutManager(context)

        val emptyRevText = view.findViewById<TextView>(R.id.emptyReviewsText)

        val adapter = ReviewAdapter(listOf())
        recyclerViewRev.adapter = adapter

        reviewsListViewModel.revList.observe(this.viewLifecycleOwner){
            if (it.isEmpty()) {
                recyclerViewRev.visibility = View.GONE
                emptyRevText.visibility = View.VISIBLE
            } else {
                recyclerViewRev.visibility = View.VISIBLE
                emptyRevText.visibility = View.GONE
            }

            adapter.setReviewers(it)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}