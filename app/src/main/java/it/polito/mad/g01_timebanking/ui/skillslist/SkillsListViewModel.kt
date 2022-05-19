package it.polito.mad.g01_timebanking.ui.skillslist

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import it.polito.mad.g01_timebanking.adapters.SkillDetails

class SkillsListViewModel(a: Application) : AndroidViewModel(a)  {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var skillsListener: ListenerRegistration

    private lateinit var skillCleanerListener: ListenerRegistration

    private var mSkillsList : MutableList<SkillDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<SkillDetails>>().also {
        it.value = mSkillsList
        getSkillsList()
        skillCleaner()
    }

    val skillList : LiveData<List<SkillDetails>> = pvtList

    private fun skillCleaner(){
        skillCleanerListener = db.collection("suggestedSkills")
            .whereEqualTo("usage_in_user",0L)
            .whereEqualTo("usage_in_adv",0L)
            //.whereNotEqualTo("default",true) // this will exclude the documents without default field
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.d("SkillUsage", "Error searching not used skills. err:${e.message}")
                } else if (value!!.isEmpty) {
                    Log.d("SkillUsage", "No skill without usage")
                } else {
                    for (doc in value) {

                        Log.d("SkillUsage","found a skill without usage, doc[default] ${doc["default"]}")
                        //if default is absent in doc doc["default"] == null
                        if(doc["default"] == true )
                            Log.d("SkillUsage","no one is using the default skill")
                        else{
                            val skillToEliminate = doc.toObject(SkillDetails::class.java)
                            Log.d(
                                "SkillUsage",
                                "cleaning skill ${doc.toObject(SkillDetails::class.java)}"
                            )
                            db.collection("suggestedSkills")
                                .document(skillToEliminate.name).delete()
                        }
                    }
                }
            }
    }

    private fun getSkillsList() {
        skillsListener = db.collection("suggestedSkills")
            .whereGreaterThan("usage_in_adv",0)
            .orderBy("usage_in_adv", Query.Direction.DESCENDING)
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
    override fun onCleared() {
        skillCleanerListener.remove()
        skillsListener.remove()
    }
}