package it.polito.mad.g01_timebanking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.ui.timeslotdetails.TimeSlotDetailsViewModel
import java.util.*

data class AdvertisementDetails (
    var id: Int,
    var title: String,
    var location: String,
    var calendar: Calendar,
    var duration: String,
    var description: String
    ){
    override fun equals(other: Any?): Boolean {
        other as AdvertisementDetails
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + location.hashCode()
        result = 31 * result + calendar.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }
}

class AdvertisementAdapter(
    private val data:List<AdvertisementDetails>,
    private val tsDetailsViewModel: TimeSlotDetailsViewModel)
        : RecyclerView.Adapter<AdvertisementAdapter.AdvertisementViewHolder>() {

    class AdvertisementViewHolder(v:View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.advTitle)
        private val date: TextView = v.findViewById(R.id.advDate)
        private val button: ImageButton = v.findViewById(R.id.editAdvButton)
        private val cardView: CardView = v.findViewById(R.id.advCardView)

        fun bind(adv: AdvertisementDetails, buttonAction: (v: View) -> Unit, cardAction: (v: View) -> Unit) {
            title.text = adv.title
            date.text = adv.calendar.time.toString()
            button.setOnClickListener(buttonAction)
            cardView.setOnClickListener(cardAction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdvertisementViewHolder {
        // Get a reference to the context of recyclerview (current activity)
        val v : View = LayoutInflater
                        .from(parent.context)
                        .inflate(R.layout.single_advertisement_layout, parent,false)
        return AdvertisementViewHolder(v)
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
            "cardView" -> R.id.action_nav_your_offers_to_nav_show_time_slot
            else -> -1
        }

        val callback: (v: View) -> Unit = {
            val pos = data.indexOf(adv)
            if (pos != -1) {
                tsDetailsViewModel.setAdvertisement(adv)
                Navigation.findNavController(it).navigate(action)
            }
        }
        return callback
    }

    override fun getItemCount(): Int = data.size
}