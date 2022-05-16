package it.polito.mad.g01_timebanking.ui.timeslotdetails

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.g01_timebanking.Skill
import it.polito.mad.g01_timebanking.UserKey
import it.polito.mad.g01_timebanking.adapters.AdvertisementDetails
import java.util.*

class TimeSlotDetailsViewModel(a:Application) : AndroidViewModel(a) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var suggestedSkillsListener: ListenerRegistration

    private val _adv = AdvertisementDetails(
        UserKey.ID_PLACEHOLDER,
        "Placeholder title",
        "Placeholder location",
        Calendar.getInstance().time,
        "Placeholder duration",
        "Placeholder description",
        "Placeholder uid"
    )

    /* This will be the valid advertisement used by Show */
    private val pvtAdvertisement = MutableLiveData<AdvertisementDetails>().also {
        it.value = _adv
    }

    val advertisement : LiveData<AdvertisementDetails> = pvtAdvertisement



    /* Ephemeral variables used from the Edit fragment to handle temporary save */
    private var tmpSkills: MutableSet<String> = _adv.skills.toMutableSet()

    private val pvtSkills = MutableLiveData<MutableSet<String>>().also {
        it.value = tmpSkills
    }
    val skills: LiveData<MutableSet<String>> = pvtSkills

    private var tmpSuggestedSkills: MutableSet<Skill> = _adv.skills.map{Skill(name = it)}.toMutableSet()

    private val pvtSuggestedSkills = MutableLiveData<MutableSet<Skill>>().also {
        it.value = tmpSuggestedSkills
        getSuggestedSkills()
    }
    val suggestedSkills: LiveData<MutableSet<Skill>> = pvtSuggestedSkills

    private val pvtId = MutableLiveData<String>().also {
        it.value = _adv.id
    }
    val id : LiveData<String> = pvtId

    private val pvtTitle = MutableLiveData<String>().also {
        it.value = _adv.title
    }
    val title : LiveData<String> = pvtTitle

    private val pvtDescription = MutableLiveData<String>().also {
        it.value = _adv.description
    }
    val description : LiveData<String> = pvtDescription

    private val pvtLocation = MutableLiveData<String>().also {
        it.value = _adv.location
    }
    val location : LiveData<String> = pvtLocation

    private val pvtCalendar = MutableLiveData<Calendar>().also {
        val calendar = Calendar.getInstance()
        calendar.time = _adv.calendar
        it.value = calendar
    }
    val calendar : LiveData<Calendar> = pvtCalendar

    private val pvtDuration = MutableLiveData<String>().also {
        it.value = _adv.duration
    }
    val duration : LiveData<String> = pvtDuration

    fun setTitle(title: String) {
        pvtTitle.value = title
    }

    fun setLocation(location: String) {
        pvtLocation.value = location
    }

    fun setDescription(description: String) {
        pvtDescription.value = description
    }

    fun setDuration(duration: String) {
        pvtDuration.value = duration
    }

    fun setDateTime(calendar: Calendar) {
        pvtCalendar.value = Calendar.getInstance().apply {
            set(Calendar.YEAR, calendar.get(Calendar.YEAR))
            set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        }
    }


    private fun getSuggestedSkills(){

        suggestedSkillsListener = db.collection("suggestedSkills")
            .addSnapshotListener { value, error ->
                if (error == null && value != null){
                    pvtSuggestedSkills.value = value.documents.map { it.toSkill() }.toMutableSet()
                }
            }
    }

    fun removeSkill(skillText: String) {
        tmpSkills.remove(skillText)
        pvtSkills.value = tmpSkills
    }

    fun tryToAddSkill(skillText: String): Boolean {

        return if (tmpSkills.add(skillText)) {
            pvtSkills.value = tmpSkills; true
        } else false
    }

    fun setAdvertisement(adv: AdvertisementDetails) {
        pvtAdvertisement.value = adv
        pvtId.value = adv.id
        pvtTitle.value = adv.title
        pvtLocation.value = adv.location
        pvtDescription.value = adv.description
        pvtDuration.value = adv.duration
        val calendar = Calendar.getInstance()
        calendar.time = adv.calendar
        pvtCalendar.value = calendar
        pvtSkills.value = adv.skills.toMutableSet()
        tmpSkills = adv.skills.toMutableSet()
    }

    fun addOrUpdateSkills(newAdvSkillsName :MutableList<String>){
        val oldAdv = _adv

        val skillUnion = oldAdv.skills union newAdvSkillsName.toSet()
        val skillIntersection = oldAdv.skills intersect newAdvSkillsName.toSet()
        var newAdvUsage: Long
        val changedSkills = skillUnion - skillIntersection
        /* take all users */
        changedSkills.forEach { changedSkill -> db.collection("advertisements")
            .addSnapshotListener { value, error ->
                /* select only advertisement who changed their skills */
                if(value != null && error == null){

                    newAdvUsage = value.count { _adv -> _adv.toAdvDetails().skills.contains(changedSkill) }.toLong()

                    db.collection("suggestedSkills").document(changedSkill).get().addOnSuccessListener { oldSkillFromDb ->
                        val tmpSkill = oldSkillFromDb.toSkill()
                        tmpSkill.usageInAdv = newAdvUsage
                        db.collection("suggestedSkills").document(changedSkill).set(tmpSkill).addOnSuccessListener {
                            Log.d("UpdateSkillUsageUser", "Success: $it")
                        }
                            .addOnFailureListener {
                                Log.d("UpdateSkillUsageUser", "Exception: ${it.message}")
                            }
                    }

                }
            }
        }

        /*//TODO: scrivere una query unica
        for (oldUserSkillName in oldUser.skills) {
            if(! newUserSkillsName.contains(oldUserSkillName))
                decrementUsageInUserSkill(oldUserSkillName)

        }
        for (newUserSkillName in newUserSkillsName) {
            if(! oldUser.skills.contains(newUserSkillName))
                insertOrIncrementUsageInUserSkill(newUserSkillName)
        }*/


    }

    fun prepareNewAdvertisement() {
        val expTime = Calendar.getInstance()
        expTime.add(Calendar.HOUR_OF_DAY,+2)
        expTime.set(Calendar.MINUTE,0)

        pvtId.value = UserKey.ID_PLACEHOLDER
        pvtTitle.value = ""
        pvtLocation.value = ""
        pvtDescription.value = ""
        pvtDuration.value = ""
        pvtCalendar.value = expTime
    }
}

private fun DocumentSnapshot.toSkill(): Skill {
    return this.toObject(Skill::class.java) ?: Skill()
}

private fun DocumentSnapshot.toAdvDetails(): AdvertisementDetails {
    return this.toObject(AdvertisementDetails::class.java) ?: AdvertisementDetails()
}