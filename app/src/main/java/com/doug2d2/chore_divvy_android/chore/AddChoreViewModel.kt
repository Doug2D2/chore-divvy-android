package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.network.AddChoreRequest
import com.doug2d2.chore_divvy_android.repository.CategoryRepository
import com.doug2d2.chore_divvy_android.repository.ChoreRepository
import com.doug2d2.chore_divvy_android.repository.FrequencyRepository
import fr.ganfra.materialspinner.MaterialSpinner
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

enum class AddChoreStatus { LOADING, SUCCESS, CONNECTION_ERROR, OTHER_ERROR }

class AddChoreViewModel(application: Application): AndroidViewModel(application), AdapterView.OnItemSelectedListener {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val freqDao = getDatabase(application).frequencyDao
    private val freqRepository = FrequencyRepository(freqDao)

    val catDao = getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    val choreDao = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(choreDao)

    private val _addChoreStatus = MutableLiveData<AddChoreStatus>()
    val addChoreStatus: LiveData<AddChoreStatus>
        get() = _addChoreStatus

    val choreName = MutableLiveData<String>()
    val selectedFreq = MutableLiveData<Int>()
    val selectedCat = MutableLiveData<Int>()
    val selectedDiff = MutableLiveData<String>()
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
        if (!choreName.value.isNullOrBlank() && selectedFreq.value != -1 &&
            selectedCat.value != -1 && !selectedDiff.value.isNullOrBlank()) {

            val choreToAdd = AddChoreRequest(
                choreName = choreName.value ?: "",
                status = "To Do",
                frequencyId = selectedFreq.value ?: -1,
                categoryId = selectedCat.value ?: -1,
                difficulty = selectedDiff.value ?: "",
                notes = notes.value
            )

            // Add Chore
            uiScope.launch {
                try {
                    _addChoreStatus.value = AddChoreStatus.LOADING

                    choreRepository.addChore(choreToAdd)

                    _addChoreStatus.value = AddChoreStatus.SUCCESS
                } catch (e: HttpException) {
                    Timber.i("addChore HttpException: " + e.message)
                    _addChoreStatus.value = AddChoreStatus.CONNECTION_ERROR
                } catch (e: Exception) {
                    Timber.i("addChore Exception: " + e.message)
                    _addChoreStatus.value = AddChoreStatus.OTHER_ERROR
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.frequencyDropDown -> {
                if (position > -1 && position < freqs.value?.size?:-1) {
                    selectedFreq.value = freqs.value?.get(position)?.id
                }
            }
            R.id.categoryDropDown -> {
                if (position > -1 && position < cats.value?.size?:-1) {
                    selectedCat.value = cats.value?.get(position)?.id
                }
            }
            R.id.difficultyDropDown -> {
                if (position > -1 && position < diffs.value?.size?:-1) {
                    selectedDiff.value = diffs.value?.get(position)
                }
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
                selectedDiff.value = ""
            }
        }

        // Enable Add Chore button if all required fields have a value
        checkEnableAddChoreButton()
    }

    // Checks if all required fields have a value and enables addChore button if they do
    fun checkEnableAddChoreButton() {
        if (!choreName.value.isNullOrBlank() && selectedFreq.value != -1 &&
            selectedCat.value != -1 && !selectedDiff.value.isNullOrBlank()) {
            addChoreButtonEnabled.value = true
        } else {
            addChoreButtonEnabled.value = false
        }
    }
}
