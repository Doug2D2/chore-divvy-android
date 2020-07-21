package com.doug2d2.chore_divvy_android.chore

import android.app.Application
import android.view.View
import android.widget.AdapterView
import androidx.annotation.Nullable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import com.doug2d2.chore_divvy_android.ApiStatus
import com.doug2d2.chore_divvy_android.R
import com.doug2d2.chore_divvy_android.database.Category
import com.doug2d2.chore_divvy_android.database.Chore
import com.doug2d2.chore_divvy_android.database.ChoreDivvyDatabase.Companion.getDatabase
import com.doug2d2.chore_divvy_android.database.Frequency
import com.doug2d2.chore_divvy_android.database.FullChore
import com.doug2d2.chore_divvy_android.repository.*
import com.squareup.moshi.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Exception

class EditChoreViewModel(application: Application): AndroidViewModel(application), AdapterView.OnItemSelectedListener {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val freqDao = getDatabase(application).frequencyDao
    private val freqRepository = FrequencyRepository(freqDao)

    private val catDao = getDatabase(application).categoryDao
    private val catRepository = CategoryRepository(catDao)

    private val diffRepository = DifficultyRepository()

    private val choreDao = getDatabase(application).choreDao
    private val choreRepository = ChoreRepository(choreDao)

    private val userDao = getDatabase(application).userDao
    private val userRepository = UserRepository(userDao)

    private val _editChoreStatus = MutableLiveData<ApiStatus>()
    val editChoreStatus: LiveData<ApiStatus>
        get() = _editChoreStatus

    val choreToEdit = MutableLiveData<FullChore>()
    val choreName = MutableLiveData<String>()

    val saveButtonEnabled = MutableLiveData<Boolean>()

    var freqs = MutableLiveData<List<Frequency>>()
    var cats = MutableLiveData<List<Category>>()
    var diffs = MutableLiveData<List<String>>()

    val ctx = getApplication<Application>().applicationContext

    init {
        saveButtonEnabled.value = true

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
                Timber.i("getFrequencies Http Exception: " + e.message())
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
                Timber.i("getCategories Http Exception: " + e.message())
            } catch (e: Exception) {
                Timber.i("getCategories Exception: " + e.message)
            }
        }
    }

    // getDifficulties gets difficulties to populate difficulty spinner
    private fun getDifficulties() {
        diffs.value = diffRepository.getDifficulties()
    }

    // onItemSelected is called when an item is selected from any spinner
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            R.id.frequencyDropDown -> {
                if (position > -1 && position < freqs.value?.size?:-1) {
                    choreToEdit.value?.frequencyId = freqs.value?.get(position)?.id!!
                }
            }
            R.id.categoryDropDown -> {
                if (position > -1 && position < cats.value?.size?:-1) {
                    choreToEdit.value?.categoryId = cats.value?.get(position)?.id!!
                }
            }
            R.id.difficultyDropDown -> {
                if (position > -1 && position < diffs.value?.size?:-1) {
                    choreToEdit.value?.difficulty = diffs.value?.get(position)!!
                }
            }
        }
    }

    // onNothingSelected sets default values for spinners
    override fun onNothingSelected(parent: AdapterView<*>?) {
        when (parent?.id) {
            R.id.frequencyDropDown -> {
                choreToEdit.value?.frequencyId = -1
            }
            R.id.categoryDropDown -> {
                choreToEdit.value?.categoryId = -1
            }
            R.id.difficultyDropDown -> {
                choreToEdit.value?.difficulty = ""
            }
        }
    }

    // onSave is called when the Save button is clicked
    fun onSave() {
        // chore name edit text value is stored in choreName
        choreToEdit.value?.choreName = choreName.value!!

        if (!choreToEdit.value?.choreName.isNullOrBlank() && choreToEdit.value?.frequencyId != -1 &&
            choreToEdit.value?.categoryId != -1 && !choreToEdit.value?.difficulty.isNullOrBlank()) {

            uiScope.launch {
                try {
                    _editChoreStatus.value = ApiStatus.LOADING

                    val assigneeId = userRepository.getUserIdFromEmail(choreToEdit.value?.username?:"")

                    val updatedChore = Chore(
                        id = choreToEdit.value?.id!!,
                        choreName = choreToEdit.value?.choreName!!,
                        status = choreToEdit.value?.status!!,
                        dateComplete = choreToEdit.value?.dateComplete,
                        frequencyId = choreToEdit.value?.frequencyId!!,
                        categoryId = choreToEdit.value?.categoryId!!,
                        assigneeId = assigneeId,
                        difficulty = choreToEdit.value?.difficulty!!,
                        notes = choreToEdit.value?.notes,
                        createdAt = choreToEdit.value?.createdAt!!,
                        updatedAt = choreToEdit.value?.updatedAt!!
                    )

                    // Update Chore
                    uiScope.launch {
                        try {
                            choreRepository.updateChore(ctx, updatedChore)

                            _editChoreStatus.value = ApiStatus.SUCCESS
                        } catch (e: HttpException) {
                            Timber.i("editChore HttpException: " + e.message)
                            _editChoreStatus.value = ApiStatus.CONNECTION_ERROR
                        } catch (e: Exception) {
                            Timber.i("editChore Exception: " + e.message)
                            _editChoreStatus.value = ApiStatus.OTHER_ERROR
                        }
                    }

                } catch (e: Exception) {
                    Timber.i("addChore getUserIdFromEmail Exception: " + e.message)
                    _editChoreStatus.value = ApiStatus.OTHER_ERROR
                }
            }
        }
    }
}