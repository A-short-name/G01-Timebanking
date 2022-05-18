package it.polito.mad.g01_timebanking.adapters

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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
    var skills: MutableList<String> = mutableListOf()
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
    var data:List<AdvertisementDetails>,
    private val tsDetailsViewModel: TimeSlotDetailsViewModel,
    private val isAdvBySkill: Boolean,
    private var filterList: List<AdvertisementDetails>
    )
        : RecyclerView.Adapter<AdvertisementAdapter.AdvertisementViewHolder>() {

    class AdvertisementViewHolder(private val parent: ViewGroup, v:View, private val isAdvBySkill: Boolean): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.advTitle)
        private val date: TextView = v.findViewById(R.id.advDate)
        private val button: ImageButton = v.findViewById(R.id.editAdvButton)
        private val cardView: CardView = v.findViewById(R.id.advCardView)

        @SuppressLint("SetTextI18n")
        fun bind(adv: AdvertisementDetails, buttonAction: (v: View) -> Unit, cardAction: (v: View) -> Unit) {
            title.text = adv.title
            val calendar = Calendar.getInstance()
            calendar.time = adv.calendar

            date.text = "${calendar.fromDateToString()} ${calendar.fromTimeToString(
                DateFormat.is24HourFormat(parent.context))}"

            if(!isAdvBySkill) {
                button.setOnClickListener(buttonAction)
                button.visibility = View.VISIBLE
            } else
                button.visibility = View.GONE

            cardView.setOnClickListener(cardAction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementViewHolder {
        // Get a reference to the context of recyclerview (current activity)
        val v : View = LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.single_advertisement_layout, parent,false)
        return AdvertisementViewHolder(parent,v,isAdvBySkill)
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
            "cardView" -> if(!isAdvBySkill) R.id.action_nav_your_offers_to_nav_show_time_slot else R.id.action_nav_adv_list_by_skill_to_nav_show_time_slot
            else -> -1
        }

        val callback: (v: View) -> Unit = {
            val pos = data.indexOf(adv)
            if (pos != -1) {
                tsDetailsViewModel.setAdvertisement(adv)

                if(isAdvBySkill) {
                    val b = bundleOf("HideOptionMenu" to true)
                    Navigation.findNavController(it).navigate(action, b)
                } else
                    Navigation.findNavController(it).navigate(action)
            }
        }
        return callback
    }

    fun setAdvertisements(newAdvs: List<AdvertisementDetails>) {
        val diffs = DiffUtil.calculateDiff( AdvDiffCallback(data, newAdvs) )
        data = newAdvs //update data
        diffs.dispatchUpdatesTo(this) //animate UI
    }

    override fun getItemCount(): Int = data.size
}