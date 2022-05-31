package it.polito.mad.g01_timebanking.adapters

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat.getColor
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
import it.polito.mad.g01_timebanking.ui.timeslotdetails.TimeSlotDetailsViewModel
import java.util.*

data class AdvertisementDetails (
    var id: String = "",
    var title: String = "",
    var location: String = "",
    var calendar: Date = Calendar.getInstance().time,
    var duration: String = "",
    var description: String = "",
    var uid: String = "",
    var sold: Boolean = false,
    var soldToUid: String = "",
    var skills: MutableList<String> = mutableListOf(),
    var skillsCleaned: Boolean = false,
    var savedBy: MutableList<String> = mutableListOf()
    ){
    override fun equals(other: Any?): Boolean {
        other as AdvertisementDetails
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + calendar.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + uid.hashCode()
        result = 32 * result + skills.hashCode()
        return result
    }
}

class AdvertisementAdapter(
    private var data:List<AdvertisementDetails>,
    private val tsDetailsViewModel: TimeSlotDetailsViewModel,
    private val isAdvForVisualizationOnly: Boolean,
    //private var filterList: List<AdvertisementDetails>
    )
        : RecyclerView.Adapter<AdvertisementAdapter.AdvertisementViewHolder>() {
    private val auth = Firebase.auth

    class AdvertisementViewHolder(private val parent: ViewGroup, v:View, private val isAdvForVisualizationOnly: Boolean, private val uid: String): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.advTitle)
        private val date: TextView = v.findViewById(R.id.advCalendar)
        private val duration: TextView = v.findViewById(R.id.advClock)
        private val button: ImageButton = v.findViewById(R.id.editAdvButton)
        private val cardView: CardView = v.findViewById(R.id.advCardView)
        private val advInfoButton: ImageButton = v.findViewById(R.id.advInfoButton)
        private val advSellingInfo: Chip = v.findViewById(R.id.sellingInfoTextView)



        @SuppressLint("SetTextI18n")
        fun bind(adv: AdvertisementDetails, buttonAction: (v: View) -> Unit, cardAction: (v: View) -> Unit) {
            title.text = adv.title
            val calendar = Calendar.getInstance()
            calendar.time = adv.calendar
            date.text = "${calendar.fromDateToString()} | ${calendar.fromTimeToString(DateFormat.is24HourFormat(parent.context))}"
            duration.text = adv.duration


            if(!isAdvForVisualizationOnly) {
                button.setOnClickListener(buttonAction)
                button.visibility = View.VISIBLE
            } else
                button.visibility = View.GONE
            if(adv.sold) {
                if (uid.equals(adv.soldToUid)) {
                    advSellingInfo.text = "PURCHASED"
                    advSellingInfo.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(parent.context, R.color.colorPurchased))
                    advSellingInfo.visibility = View.VISIBLE
                } else if (uid.equals(adv.uid)) {
                    advSellingInfo.text = "SOLD"
                    advSellingInfo.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(parent.context, R.color.colorSold))
                    advSellingInfo.visibility = View.VISIBLE
                }
            }

            cardView.setOnClickListener(cardAction)
            advInfoButton.setOnClickListener(cardAction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementViewHolder {
        // Get a reference to the context of recyclerview (current activity)
        val v : View = LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.single_advertisement_layout, parent,false)
        return AdvertisementViewHolder(parent,v,isAdvForVisualizationOnly, auth.currentUser!!.uid)
    }

    override fun onBindViewHolder(holder: AdvertisementViewHolder, position: Int) {
        val adv = data[position]

        // This callback will be called when the editButton of the list is clicked
        val buttonCallback: (v: View) -> Unit = defineCallbacks(adv,"button")

        // This callback will be called when the cardCallback of the list is clicked
        val cardCallback : (v: View) -> Unit = defineCallbacks(adv, "cardView")

        holder.bind(adv, buttonCallback, cardCallback)
    }

    private fun defineCallbacks(adv: AdvertisementDetails, destination: String): (v: View) -> Unit {
        val action = when (destination) {
            "button" -> R.id.action_nav_your_offers_to_nav_edit_time_slot
            "cardView" -> if(!isAdvForVisualizationOnly) R.id.action_nav_your_offers_to_nav_show_time_slot else R.id.nav_show_time_slot
            else -> -1
        }

        val callback: (v: View) -> Unit = {
            val pos = data.indexOf(adv)
            if (pos != -1) {
                tsDetailsViewModel.setAdvertisement(adv)

                val b = bundleOf()
                b.putBoolean("HideOptionMenu", isAdvForVisualizationOnly)
                b.putBoolean("isOwner",adv.uid == Firebase.auth.currentUser!!.uid)
                b.putBoolean("isAssigned", adv.sold)

                Navigation.findNavController(it).navigate(action, b)

            }
        }
        return callback
    }


    fun setAdvertisements(newAdvs: List<AdvertisementDetails>) {
        val diffs = DiffUtil.calculateDiff( AdvDiffCallback(data, newAdvs) )
        data = newAdvs.toList() //update data
        diffs.dispatchUpdatesTo(this) //animate UI
    }

    override fun getItemCount(): Int = data.size
}