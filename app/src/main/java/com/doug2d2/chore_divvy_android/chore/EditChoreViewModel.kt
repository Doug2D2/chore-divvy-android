package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import com.doug2d2.chore_divvy_android.repository.FrequencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

enum class EditChoreStatus { LOADING, SUCCESS, CONNECTION_ERROR, OTHER_ERROR }

class EditChoreViewModel(application: Application): AndroidViewModel(application), AdapterView.OnItemSelectedListener {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val freqDao = getDatabase(application).frequencyDao
    private val freqRepository = FrequencyRepository(freqDao)

    private val catDao = getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    private val choreDao = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(choreDao)

    private val _editChoreStatus = MutableLiveData<EditChoreStatus>()
    val editChoreStatus: LiveData<EditChoreStatus>
        get() = _editChoreStatus

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        TODO("Not yet implemented")
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}