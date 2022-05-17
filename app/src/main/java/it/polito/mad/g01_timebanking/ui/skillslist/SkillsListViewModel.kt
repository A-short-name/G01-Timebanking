package it.polito.mad.g01_timebanking.ui.skillslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.g01_timebanking.adapters.SkillDetails

class SkillsListViewModel(a: Application) : AndroidViewModel(a)  {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var skillsListener: ListenerRegistration

    private var mSkillsList : MutableList<SkillDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<SkillDetails>>().also {
        it.value = mSkillsList
        getSkillsList()
    }

    val skillList : LiveData<List<SkillDetails>> = pvtList

    private fun getSkillsList() {
        skillsListener = db.collection("suggestedSkills")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("Skills_Listener", "Error retrieving data.")
                } else if (value!!.isEmpty) {
                    Log.d("Skills_Listener", "No skills on database.")
                    mSkillsList = mutableListOf()
                    pvtList.value = mutableListOf()
                } else {
                    val skills = mutableListOf<SkillDetails>()

                    for (doc in value) {
                        skills.add(doc.toObject(SkillDetails::class.java))
                    }
                    mSkillsList = skills
                    pvtList.value = skills
                }
            }
    }
}