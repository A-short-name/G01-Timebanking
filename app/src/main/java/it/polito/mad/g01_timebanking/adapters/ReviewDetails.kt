package it.polito.mad.g01_timebanking.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromDateToString
import it.polito.mad.g01_timebanking.helpers.CalendarHelper.Companion.fromTimeToString
import it.polito.mad.g01_timebanking.ui.AdvDiffCallback
import it.polito.mad.g01_timebanking.ui.RevDiffCallback
import it.polito.mad.g01_timebanking.ui.timeslotdetails.TimeSlotDetailsViewModel
import java.util.*

data class ReviewDetails (
    var chatId : String = "",
    var reviewId : String = "",
    var rating : Int = -1,
    var text : String = "",
    var fromUid : String = "",
    var reviewerFromName : String = "",
    var toUid : String = "",
    var advId : String = "",
    var reviewerToName : String = "",
    var advTitle : String = "",
    var reviewerIsTheOwner : Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReviewDetails

        if (chatId != other.chatId) return false
        if (reviewId != other.reviewId) return false
        if (rating != other.rating) return false
        if (text != other.text) return false
        if (fromUid != other.fromUid) return false
        if (reviewerFromName != other.reviewerFromName) return false
        if (toUid != other.toUid) return false
        if (advId != other.advId) return false
        if (reviewerToName != other.reviewerToName) return false
        if (advTitle != other.advTitle) return false
        if (reviewerIsTheOwner != other.reviewerIsTheOwner) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chatId.hashCode()
        result = 31 * result + reviewId.hashCode()
        result = 31 * result + rating
        result = 31 * result + text.hashCode()
        result = 31 * result + fromUid.hashCode()
        result = 31 * result + reviewerFromName.hashCode()
        result = 31 * result + toUid.hashCode()
        result = 31 * result + advId.hashCode()
        result = 31 * result + reviewerToName.hashCode()
        result = 31 * result + advTitle.hashCode()
        result = 31 * result + reviewerIsTheOwner.hashCode()
        return result
    }

}

class ReviewAdapter(
    private var data:List<ReviewDetails>,
    //private val tsDetailsViewModel: TimeSlotDetailsViewModel,
    //private val isAdvForVisualizationOnly: Boolean,
    //private var filterList: List<AdvertisementDetails>
)
    : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {
    private val auth = Firebase.auth

    class ReviewViewHolder(private val parent: ViewGroup, v: View): RecyclerView.ViewHolder(v) {
        private val advTitle: TextView = v.findViewById(R.id.reviewTitle)
        private val reviewerName: TextView = v.findViewById(R.id.reviewerName)
        private val rating: RatingBar = v.findViewById(R.id.reviewRating)
        private val reviewContent: TextView = v.findViewById(R.id.reviewContent)
        private val cardView: CardView = v.findViewById(R.id.reviewCardView)



        @SuppressLint("SetTextI18n")
        fun bind(rev: ReviewDetails) {
            advTitle.text = rev.advTitle
            reviewerName.text = rev.reviewerFromName
            rating.rating = rev.rating.toFloat()
            reviewContent.text = rev.text
            //cardView.setOnClickListener(cardAction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        // Get a reference to the context of recyclerview (current activity)
        val v : View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.single_review_layout, parent,false)
        return ReviewViewHolder(parent,v)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val rev = data[position]
        holder.bind(rev)
    }

    fun setReviewers(newRevs: List<ReviewDetails>) {
        val diffs = DiffUtil.calculateDiff( RevDiffCallback(data, newRevs) )
        data = newRevs.toList() //update data
        diffs.dispatchUpdatesTo(this) //animate UI
    }

    override fun getItemCount(): Int = data.size
}