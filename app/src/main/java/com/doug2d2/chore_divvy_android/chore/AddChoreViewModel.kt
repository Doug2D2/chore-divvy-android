package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import com.doug2d2.chore_divvy_android.repository.FrequencyRepository
import fr.ganfra.materialspinner.MaterialSpinner
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

class AddChoreViewModel(application: Application): AndroidViewModel(application), AdapterView.OnItemSelectedListener {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val freqDao = getDatabase(application).frequencyDao
    private val freqRepository = FrequencyRepository(freqDao)

    val catDao = getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    val choreName = MutableLiveData<String>()
    val selectedFreq = MutableLiveData<Int>()
    val selectedCat = MutableLiveData<Int>()
    val selectedDiff = MutableLiveData<Int>()
    val notes = MutableLiveData<String>()
    val addChoreButtonEnabled = MutableLiveData<Boolean>()

    var freqs = MutableLiveData<List<Frequency>>()
    var cats = MutableLiveData<List<Category>>()
    var diffs = MutableLiveData<List<String>>()

    init {
        addChoreButtonEnabled.value = false
        getFrequencies()
        getCategories()
        getDifficulties()
    }

    fun getFrequencies() {
        uiScope.launch {
            try {
                freqs.value = freqRepository.getFrequencies()
            } catch (e: HttpException) {
                Timber.i("Http Exception: " + e.message())
            } catch (e: Exception) {
                Timber.i("Exception: " + e.message)
            }
        }
    }

    fun getCategories() {
        uiScope.launch {
            try {
                cats.value = catRepository.getCategories()
                Timber.i("ViewModel: " + cats)
            } catch (e: HttpException) {
                Timber.i("Http Exception: " + e.message())
            } catch (e: Exception) {
                Timber.i("Exception: " + e.message)
            }
        }
    }

    fun getDifficulties() {
        diffs.value = listOf<String>("Easy", "Medium", "Hard")
    }

    fun onAddChore() {
        Timber.i("ADD CHORE")
        Timber.i("Selected Freq: " + selectedFreq.value)
        Timber.i("Selected Cat: " + selectedCat.value)
        Timber.i("Selected Diff: " + selectedDiff.value)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.frequencyDropDown -> {
                selectedFreq.value = position
            }
            R.id.categoryDropDown -> {
                selectedCat.value = position
            }
            R.id.difficultyDropDown -> {
                selectedDiff.value = position
            }
        }

        // Enable Add Chore button if all required fields have a value
        checkEnableAddChoreButton()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        when (parent?.id) {
            R.id.frequencyDropDown -> {
                selectedFreq.value = -1
            }
            R.id.categoryDropDown -> {
                selectedCat.value = -1
            }
            R.id.difficultyDropDown -> {
                selectedDiff.value = -1
            }
        }

        // Enable Add Chore button if all required fields have a value
        checkEnableAddChoreButton()
    }

    // Checks if all required fields have a value and enables addChore button if they do
    fun checkEnableAddChoreButton() {
        if (!choreName.value.isNullOrBlank() && selectedFreq.value != -1 &&
            selectedCat.value != -1 && selectedDiff.value != -1) {
            addChoreButtonEnabled.value = true
        } else {
            addChoreButtonEnabled.value = false
        }
    }
}
