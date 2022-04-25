package it.polito.mad.g01_timebanking.entities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R
import org.w3c.dom.Text
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
        val title: TextView = v.findViewById(R.id.advTitle)
        val date: TextView = v.findViewById(R.id.advDate)

        fun bind(adv: AdvertisementDetails) {
            title.text = adv.title
            date.text = adv.calendar.time.toString()
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
        holder.bind(adv)
    }

    override fun getItemCount(): Int = data.size
}