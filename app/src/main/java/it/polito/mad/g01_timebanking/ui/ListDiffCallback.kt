package it.polito.mad.g01_timebanking.ui

import androidx.recyclerview.widget.DiffUtil
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import it.polito.mad.g01_timebanking.adapters.SkillDetails
import java.util.*

class AdvDiffCallback (private val adv: List<AdvertisementDetails>, private val newAdv: List<AdvertisementDetails>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = adv.size
    override fun getNewListSize(): Int = newAdv.size
    override fun areItemsTheSame(oldP: Int, newP: Int): Boolean {
        return adv[oldP].id === newAdv[newP].id
    }
    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        val (_, title, location, calendar, duration, description, uid) = adv[oldPosition]
        val (_, title1, location1, calendar1, duration1, description1, uid1) = newAdv[newPosition]
        return title == title1 && location == location1 &&
                calendar == calendar1 && duration == duration1 &&
                 description == description1 && uid == uid1
    }
}

class SkillDiffCallback (private val skill: List<SkillDetails>, private val newSkill: List<SkillDetails>): DiffUtil.Callback() {
    override fun getOldListSize(): Int = skill.size
    override fun getNewListSize(): Int = newSkill.size
    override fun areItemsTheSame(oldP: Int, newP: Int): Boolean {
        return skill[oldP].name === newSkill[newP].name
    }
    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        val (_, usageInAdv, usageInUser) = skill[oldPosition]
        val (_, usageInAdv1, usageInUser1) = newSkill[newPosition]
        return usageInAdv == usageInAdv1 && usageInUser == usageInUser1
    }
}