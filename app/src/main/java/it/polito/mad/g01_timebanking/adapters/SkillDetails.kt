package it.polito.mad.g01_timebanking.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.PropertyName
import it.polito.mad.g01_timebanking.R
import it.polito.mad.g01_timebanking.ui.timeslotlistbyskill.TimeSlotListBySkillViewModel

data class SkillDetails (
    var name: String = "",
    @set:PropertyName("usage_in_adv")
    @get:PropertyName("usage_in_adv")
    var usageInAdv: Long = 0L,
    @set:PropertyName("usage_in_user")
    @get:PropertyName("usage_in_user")
    var usageInUser: Long = 0L
){
    override fun equals(other: Any?): Boolean {
        other as SkillDetails
        return name == other.name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + usageInAdv.hashCode()
        result = 31 * result + usageInUser.hashCode()
        return result
    }

}

class SkillAdapter(
    private val data:List<SkillDetails>,
    private val tsListBySkillViewModel: TimeSlotListBySkillViewModel)
    : RecyclerView.Adapter<SkillAdapter.SkillViewHolder>() {

    class SkillViewHolder(v:View): RecyclerView.ViewHolder(v) {
        private val title: TextView = v.findViewById(R.id.singleSkillTitle)
        private val cardView: CardView = v.findViewById(R.id.skillCardView)

        fun bind(skill: SkillDetails, cardAction: (v: View) -> Unit) {
            title.text = skill.name
            cardView.setOnClickListener(cardAction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        // Get a reference to the context of recyclerview (current activity)
        val v : View = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.single_skill_layout, parent,false)
        return SkillViewHolder(v)
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        val skill = data[position]
        val action = R.id.action_nav_skills_list_to_timeSlotListBySkillFragment

        // This callback will be called when the cardCallback of the list is clicked
        holder.bind(skill) {
            val pos = data.indexOf(skill)
            if (pos != -1) {
                tsListBySkillViewModel.setAdvertisementsBySkill(skill)
                Navigation.findNavController(it).navigate(action)
            }
        }

    }


    override fun getItemCount(): Int = data.size
}