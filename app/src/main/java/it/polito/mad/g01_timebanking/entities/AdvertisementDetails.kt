package it.polito.mad.g01_timebanking.entities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import java.util.*

data class AdvertisementDetails (
    var title: String,
    var location: String,
    var calendar: Calendar,
    var duration: String,
    var description: String
    )

class AdvertisementAdapter(private val data:List<AdvertisementDetails>): RecyclerView.Adapter<AdvertisementAdapter.AdvertisementViewHolder>() {
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
        val buttonCallback : (v: View) -> Unit = {
            val pos = data.indexOf(adv)
            if (pos!=-1) {
                Navigation.findNavController(it).navigate(R.id.action_nav_your_offers_to_nav_edit_time_slot)
            }
        }

        val cardCallback : (v: View) -> Unit = {
            val pos = data.indexOf(adv)
            if (pos!=-1) {
                Navigation.findNavController(it).navigate(R.id.action_nav_your_offers_to_nav_show_time_slot)
            }
        }

        holder.bind(adv, buttonCallback, cardCallback)
    }

    override fun getItemCount(): Int = data.size
}