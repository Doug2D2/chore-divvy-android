package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.network.AddChoreRequest
import com.doug2d2.chore_divvy_android.repository.*
import kotlinx.coroutines.*
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

class AddChoreViewModel(application: Application): AndroidViewModel(application), AdapterView.OnItemSelectedListener {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val freqDao = getDatabase(application).frequencyDao
    private val freqRepository = FrequencyRepository(freqDao)

    private val catDao = getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    private val userDao = getDatabase(application).userDao
    private val userRepository = UserRepository(userDao)

    private val diffRepository = DifficultyRepository()

    private val choreDao = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(choreDao)

    private val _addChoreStatus = MutableLiveData<ApiStatus>()
    val apiChoreStatus: LiveData<ApiStatus>
        get() = _addChoreStatus

    val choreName = MutableLiveData<String>()
    val selectedFreq = MutableLiveData<Int>()
    val selectedCat = MutableLiveData<Int>()
    val selectedDiff = MutableLiveData<String>()
    val assignTo = MutableLiveData<String>()
    val notes = MutableLiveData<String>()
    val addChoreButtonEnabled = MutableLiveData<Boolean>()

    var freqs = MutableLiveData<List<Frequency>>()
    var cats = MutableLiveData<List<Category>>()
    var diffs = MutableLiveData<List<String>>()

    val ctx = getApplication<Application>().applicationContext

    init {
        addChoreButtonEnabled.value = false

        getFrequencies()
        getCategories()
        getDifficulties()
    }

    // getFrequencies gets frequencies to populate frequency spinner
    private fun getFrequencies() {
        uiScope.launch {
            try {
                freqs.value = freqRepository.getFrequencies()
            } catch (e: HttpException) {
                Timber.i("getFrequencies Http Exception: " + e.message)
            } catch (e: Exception) {
                Timber.i("getFrequencies Exception: " + e.message)
            }
        }
    }

    // getCategories gets categories to populate category spinner
    private fun getCategories() {
        uiScope.launch {
            try {
                cats.value = catRepository.getCategories(ctx)
            } catch (e: HttpException) {
                Timber.i("getCategories Http Exception: " + e.message)
            } catch (e: Exception) {
                Timber.i("getCategories Exception: " + e.message)
            }
        }
    }

    // getDifficulties gets difficulties to populate difficulty spinner
    private fun getDifficulties() {
        diffs.value = diffRepository.getDifficulties()
    }

    // onAddChore is called when the Add button is clicked
    fun onAddChore() {
        if (!choreName.value.isNullOrBlank() && selectedFreq.value != -1 &&
            selectedCat.value != -1 && !selectedDiff.value.isNullOrBlank()) {

            uiScope.launch {
                try {
                    _addChoreStatus.value = ApiStatus.LOADING

                    val assigneeId = userRepository.getUserIdFromEmail(assignTo.value?:"")

                    val choreToAdd = AddChoreRequest(
                        choreName = choreName.value ?: "",
                        status = "To Do",
                        frequencyId = selectedFreq.value ?: -1,
                        categoryId = selectedCat.value ?: -1,
                        difficulty = selectedDiff.value ?: "",
                        assigneeId = assigneeId,
                        notes = notes.value
                    )

                    // Add Chore
                    uiScope.launch {
                        try {
                            choreRepository.addChore(ctx, choreToAdd)

                            _addChoreStatus.value = ApiStatus.SUCCESS
                        } catch (e: HttpException) {
                            Timber.i("addChore HttpException: " + e.message)
                            _addChoreStatus.value = ApiStatus.CONNECTION_ERROR
                        } catch (e: Exception) {
                            Timber.i("addChore Exception: " + e.message)
                            _addChoreStatus.value = ApiStatus.OTHER_ERROR
                        }
                    }
                } catch (e: Exception) {
                    Timber.i("addChore getUserIdFromEmail Exception: " + e.message)
                    _addChoreStatus.value = ApiStatus.OTHER_ERROR
                }
            }
        }
    }

    // onItemSelected is called when an item is selected from any spinner
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

    // onNothingSelected sets default values for spinners
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
