package it.polito.mad.g01_timebanking.ui.skillslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.mad.g01_timebanking.adapters.SkillDetails

class SkillsListViewModel(a: Application) : AndroidViewModel(a)  {

    private var mSkillsList : MutableList<SkillDetails> = mutableListOf()

    private val pvtList = MutableLiveData<List<SkillDetails>>().also {
        it.value = mSkillsList
    }

    val skillList : LiveData<List<SkillDetails>> = pvtList

}