package it.polito.mad.g01_timebanking.adapters

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.g01_timebanking.R

data class SkillDetails (
    var id: Int,
    var title: String
){
    override fun equals(other: Any?): Boolean {
        other as SkillDetails
        return id == other.id
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        return result
    }
}

class SkillAdapter(
    private val data:List<SkillDetails>)
    : RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {

    class SkillViewHolder(private val parent: ViewGroup, v:View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.skillTitle)
        private val cardView: CardView = v.findViewById(R.id.skillCardView)

        fun bind(skill: SkillDetails, cardAction: (v: View) -> Unit) {
            title.text = skill.title
            cardView.setOnClickListener(cardAction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        // Get a reference to the context of recyclerview (current activity)
        val v : View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.single_skill_layout, parent,false)
        return SkillViewHolder(parent,v)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = data[position]

        // This callback will be called when the cardCallback of the list is clicked
        val cardCallback : (v: View) -> Unit = defineCallbacks(skill, "cardView")

        holder.bind(skill, cardCallback)
    }

    private fun defineCallbacks(adv: SkillDetails, destination: String): (v: View) -> Unit {
        val action = when (destination) {
            "button" -> R.id.action_nav_your_offers_to_nav_edit_time_slot
            "cardView" -> R.id.action_nav_your_offers_to_nav_show_time_slot
            else -> -1
        }

        val callback: (v: View) -> Unit = {
            val pos = data.indexOf(adv)
            if (pos != -1) {
                //tsDetailsViewModel.setAdvertisement(adv)
                Navigation.findNavController(it).navigate(action)
            }
        }
        return callback
    }

    override fun getItemCount(): Int = data.size
}